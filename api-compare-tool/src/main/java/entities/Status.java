package entities;

import java.util.HashMap;
import java.util.Map;

public class Status {
	private static Map<Integer, String> status = null;	
	private Boolean success = null;
	private Integer errorCode = null;
	private String message = null;
	static {
		// Define errorcode for common errors.
		status = new HashMap<Integer, String>();
		status.put(0, "Custom error.");
		status.put(1, "Success.");
		status.put(2, "Not creator, permission not allowed.");
		status.put(3, "No such data.");
	}
	
	public Status(Boolean success) {
		setSuccess(success);
		if (success) {
			setErrorCode(1);
		} else {
			setErrorCode(3);
		}
	}
	
	public Status(Boolean success, String message) {
		setSuccess(success);
		setMessage(message);
	}

	public Status(Boolean success, Integer errorCode) {
		setSuccess(success);
		setErrorCode(errorCode);
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
		// Set correspond message.
		this.message = status.get(errorCode);
	}

	public void setMessage(String message) {
		this.message = message;
		// Set custom error code.
		this.errorCode = 0;
	}

	public Boolean getSuccess() {
		return success;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public String getMessage() {
		return message;
	}
}
