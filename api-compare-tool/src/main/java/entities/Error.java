package entities;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class Error {

	public static class ErrorMapper implements RowMapper<Error> {
		private boolean fullData = false;

		public ErrorMapper(boolean full) {
			fullData = full;
		}

		@Override
		public Error mapRow(ResultSet rs, int rowNum) throws SQLException {
			Error error = new Error();
			error.setId(rs.getInt("id"));
			error.setTaskId(rs.getInt("task_id"));
			error.setTime(rs.getDate("time") + "  " + rs.getTime("time"));
			error.setMessage(rs.getString("message"));
			error.setIsXml(rs.getBoolean("is_xml"));
			error.setInput(rs.getString("input"));
			if (fullData) {
				error.setOutput(rs.getString("output"));
			}
			return error;
		}
	}

	private Integer id = null;
	private Integer taskId = null;
	private String time = null;
	private String message = null;
	private Boolean isXml = null;
	private String input = null;
	private String output = null;

	public void setId(Integer id) {
		this.id = id;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setIsXml(Boolean isXml) {
		this.isXml = isXml;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public Integer getId() {
		return id;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public String getTime() {
		return time;
	}

	public String getMessage() {
		return message;
	}

	public Boolean getIsXml() {
		return isXml;
	}

	public String getInput() {
		return input;
	}

	public String getOutput() {
		return output;
	}
}
