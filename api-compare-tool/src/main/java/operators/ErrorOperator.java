package operators;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import entities.Error;

public enum ErrorOperator {
	INSTANCE;
	private static final String BASIC_ERROR = "id, task_id, time, message, input";
	private static final String FULL_ERROR = BASIC_ERROR + ", output";
	private JdbcTemplate template = null;

	ErrorOperator() {
		template = Database.getTemplate();
	}

	public List<Error> getErrors(int taskId) {
		String q = "SELECT " + BASIC_ERROR + " FROM  errors WHERE task_id = ?;";
		return template.query(q, new Error.ErrorMapper(false), taskId);
	}

	public Error getError(int id) {
		String q = "SELECT " + FULL_ERROR + " FROM  errors WHERE id = ?;";
		Error error = null;
		try {
			error = template.queryForObject(q, new Error.ErrorMapper(true), id);
		} catch (DataAccessException e) {
		}
		return error;
	}
	
	public void newError(int taskId, String message, String input, String output) {
		String q = "INSERT INTO errors(task_id, time, message, input, output) VALUES (?, CURRENT_TIMESTAMP(), ?, ?, ?);";
		template.update(q, taskId, message, input, output);				
	}
	
	public void deleteErrorOfTask(int taskId) {
		String q = "DELETE FROM errors WHERE task_id = ?;";
		template.update(q, taskId);
	}
}
