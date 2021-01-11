package org.rygn.kanban;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.rygn.kanban.domain.Developer;
import org.rygn.kanban.domain.Task;
import org.rygn.kanban.service.DeveloperService;
import org.rygn.kanban.service.TaskService;
import org.rygn.kanban.utils.Constants;
import org.rygn.kanban.utils.TaskMoveAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public class TaskControllerTest {
	
	@Autowired
    protected MockMvc mvc;
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private DeveloperService developerService;
	

	
	@Test
	public void testGetTasks() throws Exception {
		
		mvc.perform(get("/tasks")
				.contentType(MediaType.APPLICATION_JSON))
			    .andExpect(status().isOk())
			    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    .andExpect(jsonPath("$[0].title", is("task1")))
			    .andExpect(jsonPath("$[0].developers[0].email", is("dev1@dev.dev")))
			    //.andExpect(jsonPath("$[0].status.label", is("TODO")))
			    .andExpect(jsonPath("$[0].type.label", is("FEATURE")))
			    .andExpect(jsonPath("$[0].nbHoursForecast", is(0)))
			    .andExpect(jsonPath("$[0].nbHoursReal", is(0)));
	}
	
	
	
	
	@Test
	public void testMoveTask() throws Exception {
		// Create a test Task that we will move
		Developer developer = this.developerService.findAllDevelopers().get(0);

		Task taskToMove = new Task();
		taskToMove.setTitle("task2");
		taskToMove.addDeveloper(developer);
		taskToMove.setNbHoursForecast(34);
		taskToMove.setNbHoursReal(34);
		this.taskService.createTask(taskToMove);

		
		// We are going to move the task "task2" that we've just created
		Collection<Task> tasks = this.taskService.findAllTasks();
			
		for (Task currentTask : tasks) {
			// Search for the task created in the test, it is initially at the TODO STATUS
			if (currentTask.getTitle().equals("task2")) {
				// We create a MoveAction that we will post for this task (move right is the only possiiblity for TODO Status)
				
				TaskMoveAction moveRightAction = new TaskMoveAction();
				moveRightAction.setAction(Constants.MOVE_RIGHT_ACTION);
						
				// Getting the action as bytes to include it in the request body
				ObjectMapper objectMapper = new ObjectMapper();
		        byte[] moveRightAsBytes = objectMapper.writeValueAsBytes(moveRightAction);

				mvc.perform(patch("/tasks/" + currentTask.getId())
						.contentType(MediaType.APPLICATION_JSON).content(moveRightAsBytes))				
					    .andExpect(status().isOk())
					    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
					    ;
				// Check if the task had been effectively moved to the right when updated
				taskToMove = this.taskService.findTask(currentTask.getId());
				Assert.assertEquals(Constants.TASK_STATUS_DOING_LABEL, taskToMove.getStatus().getLabel());
				
				// We are now going to test the MoveLeftAction , so the task status will be TODO, once again, after that
				TaskMoveAction moveLeftAction = new TaskMoveAction();
				moveLeftAction.setAction(Constants.MOVE_LEFT_ACTION);
				
				// Perform the request with the good body
				byte[] moveLeftAsBytes = objectMapper.writeValueAsBytes(moveLeftAction);
				mvc.perform(patch("/tasks/" + currentTask.getId())
						.contentType(MediaType.APPLICATION_JSON).content(moveLeftAsBytes))				
					    .andExpect(status().isOk())
					    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
					    ;
				
				// We get the task after the change and check if it had been effectively moved to the left
				taskToMove = this.taskService.findTask(currentTask.getId());
				Assert.assertEquals(Constants.TASK_STATUS_TODO_LABEL, taskToMove.getStatus().getLabel());

			}
		}
		this.taskService.deleteTask(taskToMove);
	}
	
	
	
	@Test
	public void testCreateTask() throws Exception {
		// get a developer to assign it to a task
		Developer developer = this.developerService.findAllDevelopers().get(0);
		
		// Creating the task that we will post
		Task taskToPost= new Task();
		taskToPost.addDeveloper(developer);
		taskToPost.setTitle("TaskToCreate");
		taskToPost.setType(this.taskService.findTaskType(Constants.TASK_TYPE_FEATURE_ID));
		taskToPost.setNbHoursForecast(32);
		taskToPost.setNbHoursReal(32);

		// We write the value of the task into bytes so we can post it in the future request
		ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytesValueOfTask = objectMapper.writeValueAsBytes(taskToPost);
		mvc.perform(post("/tasks")
				.contentType(MediaType.APPLICATION_JSON).content(bytesValueOfTask))				
			    .andExpect(status().isOk())
			    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    ;
		// After posting the task, the number of tasks should be 2 because there was already one registered with the Runner
		// ( Databaser Loader )
		Collection<Task> tasks = this.taskService.findAllTasks();
		Assert.assertEquals(2, tasks.size());
		
		// We look for the task we posted so we can check if it is created with the good inputs 
		boolean createdTaskIsFound = false;
		for (Task currentTask : tasks) {
			// Found it so we can change to boolean we test, and check the input values
			if (currentTask.getTitle().equals("TaskToCreate")) {
				createdTaskIsFound = true;
				// Check that the task is created with the right input values
				Assert.assertEquals(Constants.TASK_TYPE_FEATURE_LABEL, currentTask.getType().getLabel());
				Assert.assertEquals(Constants.TASK_STATUS_TODO_LABEL, currentTask.getStatus().getLabel());
				Assert.assertTrue(currentTask.getDevelopers().contains(developer));
				Assert.assertTrue(currentTask.getNbHoursForecast().equals(32));
				Assert.assertTrue(currentTask.getNbHoursReal().equals(32));
				this.taskService.deleteTask(currentTask);
			}
		}
		Assert.assertTrue(createdTaskIsFound);
	}
	
	@Test
	public void testCreateTaskWithErrors() throws Exception {
		
		// Creating the task with errors (empty title, NbHoursReal less than 0 and Empty Developers because we don't add one)
		Task taskToPost = new Task();
		taskToPost.setTitle("");
		taskToPost.setNbHoursForecast(64);
		taskToPost.setNbHoursReal(-5);
		taskToPost.setType(this.taskService.findTaskType(Constants.TASK_TYPE_BUG_ID));
		
		
		// We perform the request with the good body, and we expect to have 3 errors in the result
		ObjectMapper objectMapper = new ObjectMapper();
        byte[] taskAsBytes = objectMapper.writeValueAsBytes(taskToPost);
		
		mvc.perform(post("/tasks")
				.contentType(MediaType.APPLICATION_JSON).content(taskAsBytes))				
			    .andExpect(status().is(400))
			    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    // 3 errors created
			    .andExpect(jsonPath("errors", hasSize(3)))
			    // Check the messages of each
			    .andExpect(jsonPath("errors", hasItem("Error : Developers is empty ! Please insert developers")))
			    .andExpect(jsonPath("errors", hasItem("Error : Please insert a Title")))
			    .andExpect(jsonPath("errors", hasItem("Error : NbHoursReal must be >= 0")));
		
		// The task collection size must still be 1 ( the one created in the runner ) because no task should be created
		// if errors are detected in the post request, so we check if it is effective here
		Collection<Task> tasks = this.taskService.findAllTasks();
		Assert.assertEquals(1, tasks.size());
	}

}
