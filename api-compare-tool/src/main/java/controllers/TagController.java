package controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import operators.TagOperator;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import responses.TagsResponse;
import utils.Utils;
import entities.Status;
import entities.Tag;

/**
 * Tags apis.
 * 
 * @author ruochen.xu
 *
 */
@RestController
public class TagController {
	private static final TagOperator TAG = TagOperator.INSTANCE;

	/**
	 * Get all tags.
	 * 
	 * @return List of tags.
	 */
	@RequestMapping("/api/tags")
	public TagsResponse getTags() {
		List<Tag> tags = TAG.getAllTags();
		return new TagsResponse(new Status(tags != null), tags);
	}

	/**
	 * Add a new tag
	 * 
	 * @return Status and tag.
	 */
	@RequestMapping("/api/tags/new")
	public TagsResponse newTag(HttpServletRequest request,
			@RequestParam(value = "name") String name) {
		String user = Utils.getUserName(request);
		if (name.equals("")) {
			return new TagsResponse(new Status(false,
					"Name can't be empty string."), (Tag) null);
		}
		Integer id = TAG.addTag(name, user);
		if (id == null) {
			return new TagsResponse(
					new Status(false, "Tag name already exist."), (Tag) null);
		}
		Tag tag = TAG.getTagById(id);
		return new TagsResponse(new Status(true), tag);
	}

	/**
	 * Update a tag.
	 * 
	 * @return Status and tag.
	 */
	@RequestMapping("/api/tags/update")
	public TagsResponse modifyTag(HttpServletRequest request,
			@RequestParam(value = "id") Integer id,
			@RequestParam(value = "name") String name) {
		String user = Utils.getUserName(request);
		Tag tag = TAG.getTagById(id);
		if (tag == null) {
			return new TagsResponse(new Status(false, 3), (Tag) null);
		}
		if (!tag.getCreator().equals(user)) {
			return new TagsResponse(new Status(false, 2), (Tag) null);
		}
		if (name.equals("")) {
			return new TagsResponse(new Status(false,
					"Name can't be empty string."), (Tag) null);
		}
		if (!TAG.updateTag(id, name)) {
			return new TagsResponse(
					new Status(false, "Tag name already exist."), (Tag) null);
		}
		tag = TAG.getTagById(id);
		return new TagsResponse(new Status(true), tag);
	}

	/**
	 * Delete a tag.
	 * 
	 * @return Status.
	 */
	@RequestMapping("/api/tags/delete")
	public Status deleteTag(HttpServletRequest request,
			@RequestParam(value = "id") Integer id) {
		String user = Utils.getUserName(request);
		Tag tag = TAG.getTagById(id);
		if (tag == null) {
			return new Status(false, 3);
		}
		if (!tag.getCreator().equals(user)) {
			return new Status(false, 2);
		}
		TAG.deleteTag(id);
		return new Status(true);
	}
}
