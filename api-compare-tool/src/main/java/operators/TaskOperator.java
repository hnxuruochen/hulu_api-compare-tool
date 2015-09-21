package operators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import comparator.JsonComparator;
import comparator.XmlComparator;
import entities.Task;

/**
 * @author ruochen.xu
 */
public enum TaskOperator {
	INSTANCE;
	private static final String BASIC_TASK = "id, name, creator, tag_id, time, type, is_xml, errors_limit, errors_count, status";
	private static final String FULL_TASK = BASIC_TASK
			+ ", param1, param2, use_file, file_id, qps, requests, headers, include_headers";
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
		String q = "INSERT INTO tasks(name, creator, tag_id, time, type, param1, param2, use_file, file_id, qps, requests, include_headers, headers, is_xml, errors_limit, status) VALUES (?, ?, ?, CURRENT_TIMESTAMP(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		// Get auto generated primary key.
		KeyHolder id = new GeneratedKeyHolder();
		template.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				PreparedStatement ps = con.prepareStatement(q,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, task.getName());
				ps.setString(2, task.getCreator());
				ps.setInt(3, task.getTagId());
				ps.setInt(4, task.getType());
				ps.setString(5, task.getParam1());
				ps.setString(6, task.getParam2());
				ps.setBoolean(7, task.getUseFile());
				ps.setString(8, task.getFileId());
				ps.setDouble(9, task.getQps());
				ps.setString(10, task.getRequests());
				ps.setBoolean(11, task.getIncludeHeaders());
				ps.setString(12, task.getHeaders());
				ps.setBoolean(13, task.getIsXml());
				ps.setInt(14, task.getErrorsLimit());
				ps.setInt(15, Task.Status.WATING);
				return ps;
			}
		}, id);
		return id.getKey().intValue();
	}

	public void updateTask(Task task) {
		String q = "UPDATE tasks SET name = ?, tag_id = ?, time = CURRENT_TIMESTAMP(), type = ?, param1 = ?, param2 = ?, use_file = ?, file_id = ?, qps = ?, requests = ?, include_headers = ?, headers = ?, is_xml = ?, errors_limit = ? WHERE id = ?;";
		template.update(q, task.getName(), task.getTagId(), task.getType(),
				task.getParam1(), task.getParam2(), task.getUseFile(),
				task.getFileId(), task.getQps(), task.getRequests(),
				task.getIncludeHeaders(), task.getHeaders(), task.getIsXml(),
				task.getErrorsLimit(), task.getId());
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

	public void executeTextTask(Task task) throws DocumentException {
		String output = null;
		if (task.getIsXml()) {
			output = XmlComparator.compare(task.getParam1(), task.getParam2());
		} else {
			output = JsonComparator.compare(task.getParam1(), task.getParam2());
		}

		if ((output != null) && (!"*+-".contains(output.charAt(0) + ""))) {
			updateTask(task.getId(), 0, Task.Status.FINISHED);
		} else {
			String message = "Different text.";
			if (output == null) {
				message = "Invalid input.";
			}
			ErrorOperator.INSTANCE.newError(task.getId(), message,
					task.getIsXml(), "Text compare.", output, null);
			updateTask(task.getId(), 1, Task.Status.FINISHED);
		}
	}
}
