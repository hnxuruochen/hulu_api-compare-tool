package application;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import comparator.DefaultJsonComparator;

import entities.Task;
import entities.Task.TaskMapper;
import operators.Database;
import operators.ErrorOperator;

public class TaskExecutor implements Runnable {
	private static final int SLEEP_TIME = 10 * 1000;

	Connection con = null;
	ObjectMapper objectMapper = null;
	TaskMapper taskMapper = null;
	ErrorOperator ERROR = ErrorOperator.INSTANCE;

	private boolean compare(Task task, String uri) {
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
		String output = DefaultJsonComparator.compare(a, b);
		if (output.startsWith("*")) {
			ERROR.newError(task.getId(), "Different json text.", uri, output);
			return false;
		}
		return true;
	}

	private void work(Task task) throws SQLException {
		int errorsCount = 0;
		String[] requests = task.getRequests().split("\n");
		for (int i = 0; i < requests.length; i++) {
			if (!compare(task, requests[i])) {
				errorsCount++;
				if (errorsCount >= task.getErrorsLimit()) {
					break;
				}
			}
		}
		Statement st = con.createStatement();
		st.execute("UPDATE tasks SET status = 2, errors_count = " + errorsCount
				+ " WHERE id = " + task.getId());
		st.close();
	}

	public TaskExecutor() throws SQLException {
		con = Database.getConneciont();
		con.setAutoCommit(false);
		taskMapper = new Task.TaskMapper(true);
		objectMapper = new ObjectMapper();
	}

	@Override
	public void run() {
		try {
			while (true) {
				Statement st = con.createStatement();
				String q = "SELECT * FROM tasks WHERE status = 0 AND type = 1 LIMIT 1;";
				ResultSet rs = st.executeQuery(q);
				Task task = null;
				if (rs.next()) {
					task = taskMapper.mapRow(rs, 0);
					st.execute("UPDATE tasks SET status = 1 WHERE id = "
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
