package entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

public class Task {
	public static class TaskMapper implements RowMapper<Task> {
		private boolean fullData = false;

		public TaskMapper(boolean full) {
			fullData = full;
		}

		@Override
		public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
			Task task = new Task();
			task.setId(rs.getInt("id"));
			task.setCreator(rs.getString("creator"));
			task.setTagId(rs.getInt("tag_id"));
			task.setTime(rs.getDate("time") + "  " + rs.getTime("time"));
			if (fullData) {
				task.setParam1(rs.getString("param1"));
				task.setParam2(rs.getString("param2"));
				task.setRequests(rs.getString("requests"));
			}
			task.setType(rs.getInt("type"));
			task.setErrorsLimit(rs.getInt("errors_limit"));
			task.setErrorsCount(rs.getInt("errors_count"));
			task.setStatus(rs.getInt("status"));
			return task;
		}
	}

	private Integer id = null;
	private String creator = null;
	private Integer tagId = null;
	private String tagName = null;
	private String time = null;
	private String param1 = null;
	private String param2 = null;
	private String requests = null;
	private Integer type = null;
	private Integer errorsLimit = null;
	private Integer errorsCount = null;
	private Integer status = null;
	private List<Error> errors = null;

	public void setId(Integer id) {
		this.id = id;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setParam1(String address1) {
		this.param1 = address1;
	}

	public void setParam2(String address2) {
		this.param2 = address2;
	}

	public void setRequests(String requests) {
		this.requests = requests;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public void setErrorsLimit(Integer errorsLimit) {
		this.errorsLimit = errorsLimit;
	}

	public void setErrorsCount(Integer errorsCount) {
		this.errorsCount = errorsCount;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}

	public Integer getId() {
		return id;
	}

	public String getCreator() {
		return creator;
	}

	public Integer getTagId() {
		return tagId;
	}

	public String getTagName() {
		return tagName;
	}

	public String getTime() {
		return time;
	}

	public String getParam1() {
		return param1;
	}

	public String getParam2() {
		return param2;
	}

	public String getRequests() {
		return requests;
	}

	public Integer getType() {
		return type;
	}

	public Integer getErrorsLimit() {
		return errorsLimit;
	}

	public Integer getErrorsCount() {
		return errorsCount;
	}

	public Integer getStatus() {
		return status;
	}
	
	public List<Error> getErrors() {
		return errors;
	}
}
