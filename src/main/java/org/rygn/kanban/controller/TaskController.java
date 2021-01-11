package org.rygn.kanban.controller;

import java.util.Collection;

import javax.validation.Valid;

import org.rygn.kanban.domain.Task;
import org.rygn.kanban.service.TaskService;
import org.rygn.kanban.utils.Constants;
import org.rygn.kanban.utils.TaskMoveAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TaskController {

	@Autowired
	private TaskService taskService;

	@GetMapping("/tasks")
	Collection<Task> findAllTasks() {
		return this.taskService.findAllTasks();
	}

	@PatchMapping("/tasks/{id}")
	Task makeMmoveTask(@PathVariable Long id, @RequestBody TaskMoveAction taskDirection) {

		Task taskToMove = this.taskService.findTask(id);
		
		if (Constants.MOVE_RIGHT_ACTION.equals(taskDirection.getAction())) {

			taskToMove = this.taskService.moveRightTask(taskToMove);
		} 
		else if (Constants.MOVE_LEFT_ACTION.equals(taskDirection.getAction())) {

			taskToMove = this.taskService.moveLeftTask(taskToMove);
		} else {
			throw new IllegalStateException();
		}

		return taskToMove;
	}

	@PostMapping("/tasks")
	Task createTask(@Valid @RequestBody Task task) {

		return this.taskService.createTask(task);
	}

}