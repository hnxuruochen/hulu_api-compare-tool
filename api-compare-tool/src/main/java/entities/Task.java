package entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

public class Task {
	public class Status {
		public static final int PREPARING = 0;
		public static final int WATING = 1;
		public static final int RUNNING = 2;
		public static final int FINISHED = 3;
	}
	public class Type {
		public static final int TEXT = 0;
		public static final int SERVICE = 1;
	}

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
	private Integer tagId = 1;
	private String time = null;
	private String param1 = "";
	private String param2 = "";
	private String requests = "";
	private Integer type = 0;
	private Integer errorsLimit = 1;
	private Integer errorsCount = 0;
	private Integer status = Status.WATING;
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
