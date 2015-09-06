package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import comparator.JsonComparator;
import comparator.XmlComparator;
import configs.Config;
import entities.Task;
import entities.Task.TaskMapper;
import operators.Database;
import operators.ErrorOperator;
import operators.TaskOperator;
import utils.Utils;

/**
 * Fetch service type task from db and execute.
 * 
 * @author ruochen.xu
 */
public class TaskExecutor implements Runnable {
	// ========================= Inner class begin =========================
	/**
	 * A single compare task with a uri between two service.
	 */
	private class CompareTask {
		private String firstOutput = null;
		private String uri = null;

		/**
		 * Record uri of the task.
		 * 
		 * @param uri
		 */
		public CompareTask(String uri) {
			this.uri = uri;
		}

		/**
		 * Compare the two output.
		 * 
		 * @param sa
		 * @param sb
		 */
		private void compare(String sa, String sb) {
			String output = null;
			if (task.getIsXml()) {
				// Get Xmls.
				Document a = null;
				try {
					a = DocumentHelper.parseText(sa);
				} catch (DocumentException e) {
					addError("Xml1 invalid.", uri, output);
					return;
				}
				Document b = null;
				try {
					b = DocumentHelper.parseText(sb);
				} catch (DocumentException e) {
					addError("Xml2 invalid.", uri, output);
					return;
				}
				try {
					output = XmlComparator.compare(a.getRootElement(),
							b.getRootElement());
				} catch (DocumentException e) {
					addError("Xml invalid, need handle special case.", uri,
							output);
					System.out.println(e.getMessage());
					return;
				}
			} else {
				// Get jsons.
				JsonNode a = null;
				try {
					a = objectMapper.readTree(sa);
				} catch (IOException e) {
					addError("Json1 invalid.", uri, output);
					return;
				}
				JsonNode b = null;
				try {
					b = objectMapper.readTree(sb);
				} catch (IOException e) {
					addError("Json2 invalid", uri, output);
					return;
				}
				output = JsonComparator.compare(a, b);
			}
			// Record different text.
			if (output.startsWith("*")) {
				addError("Different text.", uri, output);
				return;
			}
		}

		/**
		 * Set output to the task, need synchronization.
		 * 
		 * @param id
		 *            Mark it's the first or second request.
		 * @param output
		 */
		synchronized void setOutput(int id, String output) {
			if (firstOutput == null) {
				firstOutput = output;
			} else {
				// Arrange compare order according to the id.
				if (id == 2) {
					compare(firstOutput, output);
				} else {
					compare(output, firstOutput);
				}
				// Finish a task, release the lock.
				releaseLock(false);
			}
		}
	}

	/**
	 * Callback function of a request.
	 * 
	 * @author ruochen.xu
	 */
	private class HttpRequestCallBack implements FutureCallback<HttpResponse> {
		private CompareTask compareTask = null;
		private int id = 0;

		/**
		 * Record shared compare task and service id.
		 * 
		 * @param compareTask
		 * @param id
		 */
		public HttpRequestCallBack(CompareTask compareTask, int id) {
			this.compareTask = compareTask;
			this.id = id;
		}

		@Override
		public void completed(HttpResponse response) {
			compareTask.setOutput(id, Utils.getHttpResponseBody(response));
		}

		@Override
		public void failed(Exception ex) {
			compareTask.setOutput(id, "");
		}

		@Override
		public void cancelled() {
			// Unexpected.
			System.err.println("Http request cancelled unexpectly.");
			compareTask.setOutput(id, "");
		}
	}

	// ========================= Inner class end =========================
	private static final ErrorOperator ERROR = ErrorOperator.INSTANCE;
	private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
			.setSocketTimeout(10000).setConnectTimeout(10000).build();

	private Connection con = null;
	private ObjectMapper objectMapper = null;
	private TaskMapper taskMapper = null;

	private Task task = null;
	private CloseableHttpAsyncClient httpClient = null;
	private CountDownLatch latch = null;
	private Semaphore requestSemaphore = null;
	private long lastRequestTime = 0;

	/**
	 * Initialize Connection and tools.
	 */
	public TaskExecutor() throws SQLException {
		con = Database.getConneciont();
		// Use transaction.
		con.setAutoCommit(false);
		taskMapper = new Task.TaskMapper(true);
		objectMapper = new ObjectMapper();
	}

	/**
	 * Sleep for qps limit.
	 */
	private void waitForQpsLimit() {
		// 0 for no limitation.
		if (task.getQps() == 0) {
			return;
		}
		// Caculate time.
		long currentTime = System.currentTimeMillis();
		long targetTime = (long) (lastRequestTime + 1000 / task.getQps());
		if (currentTime < targetTime) {
			try {
				Thread.sleep(targetTime - currentTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			lastRequestTime = targetTime;
		} else {
			lastRequestTime = currentTime;
		}
	}

	/**
	 * Compare one uri of two apis.
	 */
	private void compare(String uri) {
		// Ensure api address end with /.
		if (!uri.startsWith("/")) {
			uri = uri + "/";
		}
		// Get urls.
		URI ua = null;
		try {
			ua = new URI(task.getParam1() + uri);
		} catch (URISyntaxException e1) {
			addError("Address1 invalid.", uri, null);
			return;
		}
		URI ub = null;
		try {
			ub = new URI(task.getParam2() + uri);
		} catch (URISyntaxException e) {
			addError("Address2 invalid.", uri, null);
			return;
		}
		waitForQpsLimit();
		try {
			// Get lock.
			requestSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		CompareTask cTask = new CompareTask(uri);
		httpClient.execute(new HttpGet(ua), new HttpRequestCallBack(cTask, 1));
		httpClient.execute(new HttpGet(ub), new HttpRequestCallBack(cTask, 2));
	}

	/**
	 * Release lock and waiting for requests.
	 * 
	 * @param finish
	 *            Whether finished sending all requests.
	 */
	private void releaseLock(boolean finish) {
		if (finish) {
			latch = new CountDownLatch(1);
		} else {
			requestSemaphore.release();
		}
		if ((requestSemaphore.availablePermits() == Config.REQUESTS_LIMIT)
				&& (latch != null)) {
			latch.countDown();
		}
	}

	/**
	 * Check whether can record more error.
	 * 
	 * @param get
	 *            Whether consume a count.
	 * @return Whether get enough errors.
	 */
	synchronized private boolean moreError(boolean get) {
		if (task.getErrorsCount() < task.getErrorsLimit()) {
			if (get) {
				task.setErrorsCount(task.getErrorsCount() + 1);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Add a error if can.
	 * 
	 * @param text
	 * @param uri
	 * @param output
	 */
	private void addError(String text, String uri, String output) {
		if (moreError(true)) {
			ERROR.newError(task.getId(), text, task.getIsXml(), uri, output);
		}
	}

	/**
	 * Execute a task.
	 */
	private void work() throws SQLException {
		// Initialize lock, client.
		requestSemaphore = new Semaphore(Config.REQUESTS_LIMIT);
		task.setErrorsCount(0);
		httpClient = HttpAsyncClients.custom()
				.setDefaultRequestConfig(REQUEST_CONFIG).build();
		httpClient.start();
		if (task.getUseFile()) {
			// Use file in hdfs.
			String fileId = task.getFileId();
			if (fileId == null) {
				fileId = task.getId().toString();
			}
			try {
				URL url = new URL(Config.HDFS_PATH.replaceFirst(":id", fileId));
				BufferedReader br = new BufferedReader(new InputStreamReader(
						url.openStream()));
				String uri = null;
				while ((uri = br.readLine()) != null) {
					if (!moreError(false)) {
						break;
					}
					compare(uri);
				}
			} catch (IOException e) {
				TaskOperator.INSTANCE.updateTask(task.getId(), 0,
						Task.Status.PREPARING);
				return;
			}
		} else {
			// Use text.
			String[] requests = task.getRequests().split("\n");
			for (int i = 0; i < requests.length; i++) {
				if (!moreError(false)) {
					break;
				}
				compare(requests[i]);
			}
		}
		// Create latch.
		releaseLock(true);
		try {
			// Waiting for requests.
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		TaskOperator.INSTANCE.updateTask(task.getId(), task.getErrorsCount(),
				Task.Status.FINISHED);
	}

	@Override
	public void run() {
		try {
			while (true) {
				// Fetch a task and mark status. Use transaction.
				Statement st = con.createStatement();
				String q = "SELECT * FROM tasks WHERE status = "
						+ Task.Status.WATING + " AND type = "
						+ Task.Type.SERVICE + " LIMIT 1;";
				ResultSet rs = st.executeQuery(q);
				task = null;
				if (rs.next()) {
					task = taskMapper.mapRow(rs, 0);
					st.execute("UPDATE tasks SET status = "
							+ Task.Status.RUNNING + " WHERE id = "
							+ task.getId());
				}
				rs.close();
				st.close();
				con.commit();
				if (task != null) {
					work();
				} else {
					Thread.sleep(Config.TASK_SLEEP_TIME);
				}
			}
		} catch (SQLException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
