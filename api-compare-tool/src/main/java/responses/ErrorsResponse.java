package responses;

import entities.Error;
import entities.Status;


public class ErrorsResponse {
	private Status status = null;
	private Error error = null;

	public ErrorsResponse(Status status, Error error) {
		setStatus(status);
		setError(error);
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public Status getStatus() {
		return status;
	}

	public Error getError() {
		return error;
	}
}
