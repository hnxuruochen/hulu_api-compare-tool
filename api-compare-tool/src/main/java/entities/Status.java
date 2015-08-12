package entities;

public class Status {
	private Boolean success = null;
	private String message = null;

	public Status(Boolean success) {
		setSuccess(success);
		if (success) {
			setMessage("Success.");
		} else {
			setMessage("Error! No data.");
		}
	}
	public Status(Boolean success, String message) {
		setSuccess(success);
		setMessage(message);
	}
	
	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}
}
