package operators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import comparator.DefaultJsonComparator;
import entities.Task;

public enum TaskOperator {
	INSTANCE;
	private static final String BASIC_TASK = "id, creator, tag_id, time, type, errors_limit, errors_count, status";
	private static final String FULL_TASK = BASIC_TASK
			+ ", param1, param2, use_file, file_id, requests";
	private JdbcTemplate template = null;

	TaskOperator() {
		template = Database.getTemplate();
	}

	/**
	 * Null for no limit.
	 */
	public List<Task> searchTasks(String creator, String tags, String status) {
		List<String> where = new ArrayList<String>();
		List<String> param = new ArrayList<String>();
		if (creator != null) {
			where.add("creator = ?");
			param.add(creator);
		}
		if (tags != null) {
			where.add("tag_id in " + tags);
		}
		if (status != null) {
			where.add("status in " + status);
		}
		String q = "SELECT " + BASIC_TASK + " FROM tasks";
		if (where.size() > 0) {
			q = q + " WHERE " + StringUtils.join(where, " AND ");
		}
		q = q + " ORDER BY id DESC;";
		return template.query(q, new Task.TaskMapper(false), param.toArray());
	}

	public Task getTask(int id) {
		String q = "SELECT " + FULL_TASK + " FROM tasks WHERE id = ?;";
		Task task = null;
		try {
			task = template.queryForObject(q, new Task.TaskMapper(true), id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		task.setErrors(ErrorOperator.INSTANCE.getErrors(id));
		return task;
	}

	public int newTask(Task task) {
		String q = "INSERT INTO tasks(creator, tag_id, time, type, param1, param2, use_file, file_id, requests, errors_limit, status) VALUES (?, ?, CURRENT_TIMESTAMP(), ?, ?, ?, ?, ?, ?, ?, ?);";
		// Get auto generated primary key.
		KeyHolder id = new GeneratedKeyHolder();
		template.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				PreparedStatement ps = con.prepareStatement(q,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, task.getCreator());
				ps.setInt(2, task.getTagId());
				ps.setInt(3, task.getType());
				ps.setString(4, task.getParam1());
				ps.setString(5, task.getParam2());
				ps.setBoolean(6, task.getUseFile());
				ps.setString(7, task.getFileId());
				ps.setString(8, task.getRequests());
				ps.setInt(9, task.getErrorsLimit());
				ps.setInt(10, Task.Status.WATING);
				return ps;
			}
		}, id);
		return id.getKey().intValue();
	}

	public void updateTask(Task task) {
		String q = "UPDATE tasks SET tag_id = ?, time = CURRENT_TIMESTAMP(), type = ?, param1 = ?, param2 = ?, use_file = ?, file_id = ?, requests = ?, errors_limit = ? WHERE id = ?;";
		template.update(q, task.getTagId(), task.getType(), task.getParam1(),
				task.getParam2(), task.getUseFile(), task.getFileId(),
				task.getRequests(), task.getErrorsLimit(), task.getId());
	}

	public void updateTask(int id, int errorsCount, int status) {
		String q = "UPDATE tasks SET errors_count = ?, status = ? WHERE id = ?";
		template.update(q, errorsCount, status, id);
	}

	public String getCreator(int id) {
		String q = "SELECT creator FROM tasks WHERE id = ?;";
		try {
			return template.queryForObject(q, String.class, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public void executeTextTask(Task task) {
		String output = DefaultJsonComparator.compare(task.getParam1(),
				task.getParam2());
		if ((output != null) && (!output.startsWith("*"))) {
			updateTask(task.getId(), 0, Task.Status.FINISHED);
		} else {
			String message = "Different json text.";
			if (output == null) {
				message = "Invalid json input.";
			}
			ErrorOperator.INSTANCE.newError(task.getId(), message,
					"Text compare.", output);
			updateTask(task.getId(), 1, Task.Status.FINISHED);
		}
	}
}
