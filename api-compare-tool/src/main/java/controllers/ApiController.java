package controllers;

import java.io.IOException;
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

import com.fasterxml.jackson.core.JsonProcessingException;

import comparator.DefaultJsonComparator;
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
	 * Modify a old tag to new tag.
	 * 
	 * @param request
	 * @param id
	 *            Null for create a new tag.
	 * @param tag
	 *            Null for delete the exist tag.
	 * @return Operation status and updated tag.
	 */
	@RequestMapping("/api/tags/modify")
	public TagResponse modifyTag(HttpServletRequest request,
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "name", required = false) String name) {
		String user = Utils.getUserName(request);
		// Check permission.
		if (id != null) {
			String creator = TAG.getTagCreator(id);
			if ((user == null) || (!creator.equals(user))) {
				return new TagResponse(new Status(false,
						"Not creator, operation not allowed."), null);
			}
		}
		Tag tag = null;
		if (name != null) {
			// Check new tag.
			if (name.equals("")) {
				return new TagResponse(new Status(false,
						"Tag name can't be empty string."), null);
			}
			if (TAG.existName(name)) {
				return new TagResponse(new Status(false,
						"Tag name already exist."), null);
			}
			if (id == null) {
				tag = TAG.addTag(name, user);
			} else {
				tag = TAG.updateTag(id, name);
			}
		} else {
			TAG.deleteTag(id);
		}
		return new TagResponse(new Status(true, "Success."), tag);
	}

	/**
	 * Create a new task
	 * 
	 * @return The created task.
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/tasks/new", method = RequestMethod.POST)
	public TasksResponse newTask(HttpServletRequest request,
			@RequestBody Task task) throws JsonProcessingException, IOException {
		String user = Utils.getUserName(request);
		int id = TASK.newTask(user, task);
		task.setId(id);
		if (task.getType() == 0) {
			TASK.executeTextTask(task);
		}
		return new TasksResponse(new Status(true), TASK.searchTasks(null, null,
				null, id).get(0));
	}

	@RequestMapping("/api/tasks/restart")
	public Status restartTask(HttpServletRequest request,
			@RequestParam(value = "id") Integer id) {
		String user = Utils.getUserName(request);
		Task task = TASK.getTask(id);
		if (!user.equals(task.getCreator())) {
			return new Status(false, "Not creator.");
		}
		if (task.getStatus() != 2) {
			return new Status(false, "Task hasn't finished.");
		}
		ERROR.deleteErrorOfTask(task.getId());
		if (task.getType() == 0) {
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

	@RequestMapping("/api/tasks/{id}")
	public TasksResponse getTask(@PathVariable(value = "id") Integer id) {
		Task task = TASK.getTask(id);
		Status status = new Status(task != null);
		return new TasksResponse(status, task);
	}

	@RequestMapping("/api/errors/{id}")
	public ErrorsResponse getError(@PathVariable(value = "id") Integer id) {
		Error error = ERROR.getError(id);
		Status status = new Status(error != null);
		return new ErrorsResponse(status, error);
	}
}
