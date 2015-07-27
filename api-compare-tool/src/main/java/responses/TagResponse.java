package responses;

import entities.Status;
import entities.Tag;

public class TagResponse {
	private Status status = null;
	private Tag tag = null;

	public TagResponse(Status status, Tag tag) {
		setStatus(status);
		setTag(tag);
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public Status getStatus() {
		return status;
	}

	public Tag getTag() {
		return tag;
	}
}
