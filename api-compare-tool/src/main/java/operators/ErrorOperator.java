package operators;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import entities.Error;

/**
 * @author ruochen.xu
 */
public enum ErrorOperator {
	INSTANCE;
	private static final String BASIC_ERROR = "id, task_id, time, message, is_xml, input";
	private static final String FULL_ERROR = BASIC_ERROR + ", output, header";
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
		} catch (EmptyResultDataAccessException e) {
		}
		return error;
	}

	public void newError(int taskId, String message, Boolean isXml, String input, String output, String header) {
		String q = "INSERT INTO errors(task_id, time, message, is_xml, input, output, header) VALUES (?, CURRENT_TIMESTAMP(), ?, ?, ?, ?, ?);";
		template.update(q, taskId, message, isXml, input, output, header);
	}

	public void deleteErrorOfTask(int taskId) {
		String q = "DELETE FROM errors WHERE task_id = ?;";
		template.update(q, taskId);
	}
}
