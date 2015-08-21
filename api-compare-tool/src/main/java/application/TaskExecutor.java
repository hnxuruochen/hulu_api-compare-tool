package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import comparator.DefaultJsonComparator;
import configs.Config;
import entities.Task;
import entities.Task.TaskMapper;
import operators.Database;
import operators.ErrorOperator;
import operators.TaskOperator;

public class TaskExecutor implements Runnable {
	private static final int SLEEP_TIME = 10 * 1000;

	Connection con = null;
	ObjectMapper objectMapper = null;
	TaskMapper taskMapper = null;
	ErrorOperator ERROR = ErrorOperator.INSTANCE;

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
	 * Compare one uri of two apis.
	 */
	private boolean compare(Task task, String uri) {
		// api address dosen't end with /.
		if (!uri.startsWith("/")) {
			uri = uri + "/";
		}
		// Get urls.
		URL ua = null;
		try {
			ua = new URL(task.getParam1() + uri);
		} catch (MalformedURLException e) {
			ERROR.newError(task.getId(), "Address1 invalid.", uri, null);
			return false;
		}
		URL ub = null;
		try {
			ub = new URL(task.getParam2() + uri);
		} catch (MalformedURLException e) {
			ERROR.newError(task.getId(), "Address2 invalid.", uri, null);
			return false;
		}
		// Get jsons.
		JsonNode a = null;
		try {
			a = objectMapper.readTree(ua);
		} catch (IOException e) {
			ERROR.newError(task.getId(), "Json1 invalid.", uri, null);
			return false;
		}
		JsonNode b = null;
		try {
			b = objectMapper.readTree(ub);
		} catch (IOException e) {
			ERROR.newError(task.getId(), "Json2 invalid", uri, null);
			return false;
		}
		// Compare.
		String output = DefaultJsonComparator.compare(a, b);
		if (output.startsWith("*")) {
			ERROR.newError(task.getId(), "Different json text.", uri, output);
			return false;
		}
		return true;
	}

	/**
	 * Execute a task.
	 */
	private void work(Task task) throws SQLException {
		int errorsCount = 0;
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
				String uri = "";
				while ((uri = br.readLine()) != null) {
					if (!compare(task, uri)) {
						errorsCount++;
						if (errorsCount >= task.getErrorsLimit()) {
							break;
						}
					}
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
				if (!compare(task, requests[i])) {
					errorsCount++;
					if (errorsCount >= task.getErrorsLimit()) {
						break;
					}
				}
			}
		}
		TaskOperator.INSTANCE.updateTask(task.getId(), errorsCount,
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
				Task task = null;
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
					work(task);
				} else {
					Thread.sleep(SLEEP_TIME);
				}
			}
		} catch (SQLException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
