package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
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
		private String firstBody = null;
		private String firstHeader = null;
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
		private void compare(String sa, String sb, String ha, String hb) {
			String output = null;
			String header = null;
			// Compare header if it's not null.
			if (ha != null) {
				header = JsonComparator.compare(ha, hb);
			}
			if (task.getIsXml()) {
				// Get Xmls.
				Document a = null;
				try {
					a = DocumentHelper.parseText(sa);
				} catch (DocumentException e) {
					addError("Xml1 invalid.", uri, output, header);
					return;
				}
				Document b = null;
				try {
					b = DocumentHelper.parseText(sb);
				} catch (DocumentException e) {
					addError("Xml2 invalid.", uri, output, header);
					return;
				}
				try {
					output = XmlComparator.compare(a.getRootElement(),
							b.getRootElement());
				} catch (DocumentException e) {
					addError("Xml invalid, need handle special case.", uri,
							output, header);
					System.out.println(e.getMessage());
					return;
				}
			} else {
				// Get jsons.
				JsonNode a = null;
				try {
					a = objectMapper.readTree(sa);
				} catch (IOException e) {
					addError("Json1 invalid.", uri, output, header);
					return;
				}
				JsonNode b = null;
				try {
					b = objectMapper.readTree(sb);
				} catch (IOException e) {
					addError("Json2 invalid", uri, output, header);
					return;
				}
				output = JsonComparator.compare(a, b);
			}
			// Record different text.
			if (output.startsWith("*")
					|| ((header != null) && (header.startsWith("*")))) {
				addError("Different text.", uri, output, header);
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
		synchronized void setOutput(int id, String body, String header) {
			if (firstBody == null) {
				firstBody = body;
				firstHeader = header;
			} else {
				// Arrange compare order according to the id.
				if (id == 2) {
					compare(firstBody, body, firstHeader, header);
				} else {
					compare(body, firstBody, header, firstHeader);
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
			String header = null;
			// Get header if it also need compare.
			if (task.getIncludeHeaders()) {
				header = Utils.getHttpResponseHeaderToJson(response, headers);
			}
			compareTask.setOutput(id, Utils.getHttpResponseBody(response),
					header);
		}

		@Override
		public void failed(Exception ex) {
			compareTask.setOutput(id, "", null);
		}

		@Override
		public void cancelled() {
			// Unexpected.
			System.err.println("Http request cancelled unexpectly.");
			compareTask.setOutput(id, "", null);
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
	private Set<String> headers = null;
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
	 * 
	 * @throws InterruptedException
	 */
	private void compare(String uri) throws InterruptedException {
		// Ensure api address end with /.
		if (!uri.startsWith("/")) {
			uri = uri + "/";
		}
		waitForQpsLimit();
		// Get lock.
		requestSemaphore.acquire();
		CompareTask cTask = new CompareTask(uri);
		httpClient.execute(new HttpGet(task.getParam1() + uri),
				new HttpRequestCallBack(cTask, 1));
		httpClient.execute(new HttpGet(task.getParam2() + uri),
				new HttpRequestCallBack(cTask, 2));
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
	private void addError(String text, String uri, String output, String header) {
		if (moreError(true)) {
			ERROR.newError(task.getId(), text, task.getIsXml(), uri, output,
					header);
		}
	}

	/**
	 * Execute a task.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void work() throws SQLException, IOException, InterruptedException {
		// Initialize lock, client, headers need check.
		requestSemaphore = new Semaphore(Config.REQUESTS_LIMIT);
		task.setErrorsCount(0);
		headers = null;
		if (task.getHeaders() != null) {
			headers = new HashSet<String>();
			for (String h : task.getHeaders().split("\n")) {
				if (!h.trim().isEmpty()) {
					headers.add(h.toLowerCase());
				}
			}
		}
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
		// Wait for requests.
		latch.await();
		httpClient.close();
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
		} catch (SQLException | InterruptedException | IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
