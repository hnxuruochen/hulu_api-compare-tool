package responses;

import java.util.List;

import entities.Status;
import entities.Task;

/**
 * @author ruochen.xu
 */
public class TasksResponse {
	private Status status = null;
	private Task task = null;
	private List<Task> tasks = null;

	public TasksResponse(Status status, Task task) {
		setStatus(status);
		setTask(task);
	}

	public TasksResponse(Status status, List<Task> tasks) {
		setStatus(status);
		setTasks(tasks);
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Status getStatus() {
		return status;
	}

	public Task getTask() {
		return task;
	}

	public List<Task> getTasks() {
		return tasks;
	}
}
