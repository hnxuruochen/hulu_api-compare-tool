package responses;

import java.util.List;

import entities.Status;
import entities.Tag;

public class TagsResponse {
	private Status status = null;
	private List<Tag> tags = null;
	private Tag tag = null;

	public TagsResponse(Status status, Tag tag) {
		setStatus(status);
		setTag(tag);
	}

	public TagsResponse(Status status, List<Tag> tags) {
		setStatus(status);
		setTags(tags);
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public Status getStatus() {
		return status;
	}

	public Tag getTag() {
		return tag;
	}

	public List<Tag> getTags() {
		return tags;
	}
}
