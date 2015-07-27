package controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import operators.TagOperator;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import responses.TagResponse;
import utils.Utils;
import entities.Status;
import entities.Tag;

/**
 * Rest apis.
 * 
 * @author ruochen.xu
 *
 */
@RestController
public class ApiController {
	private static final TagOperator TAG = TagOperator.INSTANCE;

	/**
	 * Get user's login info.
	 * 
	 * @param request
	 * @return Json string of user info.
	 */
	@RequestMapping("/api/user/info")
	public String getUserInfo(HttpServletRequest request) {
		return request.getAttribute("user_data").toString();
	}

	/**
	 * Get all tags.
	 * 
	 * @return List of tags.
	 */
	@RequestMapping("/api/tags")
	public List<Tag> getTags() {
		return TAG.getAllTags();
	}

	/**
	 * Modify a old tag to new tag.
	 * 
	 * @param request
	 * @param oldTag
	 *            Null for create a new tag.
	 * @param newTag
	 *            Null for delete a exist tag.
	 * @return Operation status and updated tag.
	 */
	@RequestMapping("/api/tags/modify")
	public TagResponse modifyTag(HttpServletRequest request,
			@RequestParam(value = "oldTag", required = false) String oldTag,
			@RequestParam(value = "newTag", required = false) String newTag) {
		String user = Utils.getUserName(request);
		// Check permission.
		if (oldTag != null) {
			String creator = TAG.getTagCreator(oldTag);
			if ((user == null) || (!creator.equals(user))) {
				return new TagResponse(new Status(false,
						"Not creator, operation not allowed."), null);
			}
		}
		Tag tag = null;
		if (newTag != null) {
			// Check new tag.
			if (newTag.equals("")) {
				return new TagResponse(new Status(false,
						"Tag name can't be empty string."), null);
			}
			if (TAG.getTagCreator(newTag) != null) {
				return new TagResponse(new Status(false,
						"Tag name already exist."), null);
			}
			if (oldTag == null) {
				tag = TAG.addTag(newTag, user);
			} else {
				tag = TAG.updateTag(oldTag, newTag);
			}
		} else {
			TAG.deleteTag(oldTag);
		}
		return new TagResponse(new Status(true, "Success."), tag);
	}
}
