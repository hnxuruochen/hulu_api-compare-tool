package application;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Semaphore;

import operators.Database;
import configs.Config;
import entities.Task;
import entities.Task.TaskMapper;

/**
 * Fetch task and create task executor.
 * 
 * @author ruochen.xu
 */
public class TaskManager {
	private Connection con = null;
	private Task task = null;
	private TaskMapper taskMapper = null;
	private Semaphore semaphore = null;

	public TaskManager() throws SQLException {
		con = Database.getConneciont();
		// Use transaction.
		con.setAutoCommit(false);
		taskMapper = new Task.TaskMapper(true);
		// Limit executor number.
		semaphore = new Semaphore(Config.TASK_EXECUTOR_NUM);
	}

	public void work() throws InterruptedException, SQLException {
		while (true) {
			semaphore.acquire();
			// Fetch a task and mark status. Use transaction.
			Statement st = con.createStatement();
			String q = "SELECT * FROM tasks WHERE status = "
					+ Task.Status.WATING + " AND type = " + Task.Type.SERVICE
					+ " LIMIT 1;";
			ResultSet rs = st.executeQuery(q);
			task = null;
			if (rs.next()) {
				task = taskMapper.mapRow(rs, 0);
				st.execute("UPDATE tasks SET status = " + Task.Status.RUNNING
						+ " WHERE id = " + task.getId());
			}
			rs.close();
			st.close();
			con.commit();
			if (task != null) {
				Thread executor = new Thread(new TaskExecutor(task, semaphore));
				executor.run();
			} else {
				Thread.sleep(Config.TASK_SLEEP_TIME);
				semaphore.release();
			}
		}
	}

	protected void finalize() throws SQLException {
		con.close();
	}
}
