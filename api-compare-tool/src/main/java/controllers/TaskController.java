package controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import operators.ErrorOperator;
import operators.TaskOperator;

import org.dom4j.DocumentException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import responses.TasksResponse;
import utils.Utils;
import entities.Status;
import entities.Task;

/**
 * Tasks apis.
 * 
 * @author ruochen.xu
 */
@RestController
public class TaskController {
	private static final TaskOperator TASK = TaskOperator.INSTANCE;

	/**
	 * Create a new task
	 * 
	 * @param request
	 * @param task
	 * @return Status and the created task's id in message.
	 * @throws DocumentException
	 */
	@RequestMapping(value = "/api/tasks/new", method = RequestMethod.POST)
	public Status newTask(HttpServletRequest request, @RequestBody Task task)
			throws DocumentException {
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
	 * @param request
	 * @param task
	 * @return Status and updated task.
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
		t = TASK.getTask(task.getId());
		return new Status(true);
	}

	/**
	 * Get creator of a task. This api dosen't require authentication.
	 * 
	 * @param id
	 * @return The creator.
	 */
	@RequestMapping(value = "/api/tasks/get_creator", method = RequestMethod.GET)
	@ResponseBody
	public String getTaskCreator(@RequestParam(value = "id") String id) {
		String creator = null;
		try {
			creator = TASK.getCreator(Integer.parseInt(id));
		} catch (NumberFormatException e) {
		}
		if (creator == null) {
			return "";
		} else {
			return creator;
		}
	}

	/**
	 * Restart a task.
	 * 
	 * @param request
	 * @param id
	 * @return Status.
	 * @throws DocumentException
	 */
	@RequestMapping(value = "/api/tasks/restart", method = RequestMethod.GET)
	public Status restartTask(HttpServletRequest request,
			@RequestParam(value = "id") Integer id) throws DocumentException {
		String user = Utils.getUserName(request);
		Task task = TASK.getTask(id);
		if (task == null) {
			return new Status(false, 3);
		}
		if (!user.equals(task.getCreator())) {
			return new Status(false, 2);
		}
		if (task.getStatus() == Task.Status.RUNNING) {
			return new Status(false, "Task is running.");
		}
		if (task.getStatus() == Task.Status.WATING) {
			return new Status(false, "Task is waiting.");
		}
		ErrorOperator.INSTANCE.deleteErrorOfTask(task.getId());
		TASK.updateTask(id, 0, Task.Status.WATING);
		if (task.getType() == Task.Type.TEXT) {
			TASK.executeTextTask(task);
		}
		return new Status(true);
	}

	/**
	 * Search tasks. Null for all.
	 * 
	 * @param creator
	 * @param tags
	 * @param status
	 * @return Tasks meet requirements.
	 */
	@RequestMapping(value = "/api/tasks/search", method = RequestMethod.GET)
	public TasksResponse searchTasks(
			@RequestParam(value = "creator") String creator,
			@RequestParam(value = "tags") String tags,
			@RequestParam(value = "status") String status) {
		if (creator.isEmpty()) {
			creator = null;
		}
		tags = Utils.jsonArrayToIntSet(tags);
		status = Utils.jsonArrayToIntSet(status);
		List<Task> tasks = TASK.searchTasks(creator, tags, status);
		return new TasksResponse(new Status(true), tasks);
	}

	/**
	 * Get the specified task.
	 * 
	 * @param id
	 * @return Task.
	 */
	@RequestMapping(value = "/api/tasks/{id}", method = RequestMethod.GET)
	public TasksResponse getTask(@PathVariable(value = "id") Integer id) {
		Task task = TASK.getTask(id);
		return new TasksResponse(new Status(task != null), task);
	}
}
