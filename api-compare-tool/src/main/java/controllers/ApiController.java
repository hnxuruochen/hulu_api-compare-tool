package controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import operators.ErrorOperator;
import operators.TagOperator;
import operators.TaskOperator;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import responses.ErrorsResponse;
import responses.TagResponse;
import responses.TasksResponse;
import utils.Utils;
import entities.Error;
import entities.Status;
import entities.Tag;
import entities.Task;

/**
 * Rest apis.
 * 
 * @author ruochen.xu
 *
 */
@RestController
public class ApiController {
	private static final TagOperator TAG = TagOperator.INSTANCE;
	private static final TaskOperator TASK = TaskOperator.INSTANCE;
	private static final ErrorOperator ERROR = ErrorOperator.INSTANCE;

	/**
	 * Get user's login info.
	 * 
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
	 * Add a new tag
	 * 
	 * @return Status and id, set id in the message.
	 */
	@RequestMapping("/api/tags/new")
	public TagResponse newTag(HttpServletRequest request,
			@RequestParam(value = "name") String name) {
		String user = Utils.getUserName(request);
		if (name.equals("")) {
			return new TagResponse(new Status(false,
					"Name can't be empty string."), null);
		}
		Integer id = TAG.addTag(name, user);
		if (id == null) {
			return new TagResponse(
					new Status(false, "Tag name already exist."), null);
		}
		Tag tag = TAG.getTagById(id);
		return new TagResponse(new Status(true), tag);
	}

	/**
	 * Update a tag.
	 * 
	 * @return Status.
	 */
	@RequestMapping("/api/tags/update")
	public TagResponse modifyTag(HttpServletRequest request,
			@RequestParam(value = "id") Integer id,
			@RequestParam(value = "name") String name) {
		String user = Utils.getUserName(request);
		Tag tag = TAG.getTagById(id);
		if (tag == null) {
			return new TagResponse(new Status(false, 3), null);
		}
		if (!tag.getCreator().equals(user)) {
			return new TagResponse(new Status(false, 2), null);
		}
		if (name.equals("")) {
			return new TagResponse(new Status(false,
					"Name can't be empty string."), null);
		}
		if (!TAG.updateTag(id, name)) {
			return new TagResponse(
					new Status(false, "Tag name already exist."), null);
		}
		tag = TAG.getTagById(id);
		return new TagResponse(new Status(true), tag);
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

	/**
	 * Create a new task
	 * 
	 * @return The created task's id.
	 */
	@RequestMapping(value = "/api/tasks/new", method = RequestMethod.POST)
	public Status newTask(HttpServletRequest request, @RequestBody Task task) {
		String user = Utils.getUserName(request);
		task.setCreator(user);
		int id = TASK.newTask(task);
		task.setId(id);
		if (task.getType() == Task.Type.TEXT) {
			TASK.executeTextTask(task);
		}
		return new Status(true, id + "");
	}

	/**
	 * Update a task.
	 * 
	 * @return Status.
	 */
	@RequestMapping(value = "/api/tasks/update", method = RequestMethod.POST)
	public Status updateTask(HttpServletRequest request, @RequestBody Task task) {
		String user = Utils.getUserName(request);
		Task t = TASK.getTask(task.getId());
		if (t == null) {
			return new Status(false, 3);
		}
		if (!user.equals(t.getCreator())) {
			return new Status(false, 2);
		}
		TASK.updateTask(task);
		return new Status(true);
	}

	/**
	 * Restart a task.
	 * 
	 * @return Status.
	 */
	@RequestMapping("/api/tasks/restart")
	public Status restartTask(HttpServletRequest request,
			@RequestParam(value = "id") Integer id) {
		String user = Utils.getUserName(request);
		Task task = TASK.getTask(id);
		if (task == null) {
			return new Status(false, 3);
		}
		if (!user.equals(task.getCreator())) {
			return new Status(false, 2);
		}
		if (task.getStatus() != Task.Status.FINISHED) {
			return new Status(false, "Task hasn't finished.");
		}
		ERROR.deleteErrorOfTask(task.getId());
		if (task.getType() == Task.Type.TEXT) {
			TASK.executeTextTask(task);
		} else {
			TASK.restartTask(id);
		}
		return new Status(true);
	}

	/**
	 * Search tasks. Null for all.
	 * 
	 * @return Tasks meet requirement.
	 */
	@RequestMapping("/api/tasks/search")
	public TasksResponse searchTasks(
			@RequestParam(value = "creator") String creator,
			@RequestParam(value = "tags") String tags,
			@RequestParam(value = "status") String status) {
		if (creator.isEmpty()) {
			creator = null;
		}
		tags = Utils.jsonArrayToIntSet(tags);
		status = Utils.jsonArrayToIntSet(status);
		List<Task> tasks = TASK.searchTasks(creator, tags, status, null);
		return new TasksResponse(new Status(true), tasks);
	}

	/**
	 * Get the specified task.
	 * 
	 * @return Task.
	 */
	@RequestMapping("/api/tasks/{id}")
	public TasksResponse getTask(@PathVariable(value = "id") Integer id) {
		Task task = TASK.getTask(id);
		Status status = new Status(task != null);
		return new TasksResponse(status, task);
	}

	/**
	 * Get specified errors.
	 * 
	 * @return Error.
	 */
	@RequestMapping("/api/errors/{id}")
	public ErrorsResponse getError(@PathVariable(value = "id") Integer id) {
		Error error = ERROR.getError(id);
		Status status = new Status(error != null);
		return new ErrorsResponse(status, error);
	}
}
