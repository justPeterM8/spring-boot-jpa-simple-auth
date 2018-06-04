package pl.asap.asapbe.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.asap.asapbe.entities.TaskEntity;
import pl.asap.asapbe.services.TaskService;

import java.util.List;

@RestController
public class TaskController {

    private TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("tasks")
    public List<TaskEntity> getAllTasksFromSpecificProject(@RequestHeader("token") String authToken,
                                                           @RequestParam("projectId") Long projectId) {
        return taskService.getAllTasksFromProject(authToken, projectId);
    }

    @GetMapping("tasks/task")
    public TaskEntity getTask(@RequestHeader("token") String authToken,
                              @RequestParam("id") Long taskId) {
        return taskService.getTaskById(authToken, taskId);
    }

    @PostMapping("tasks")
    public ResponseEntity createTask(@RequestHeader("token") String authToken,
                                     @RequestBody TaskEntity taskEntity,
                                     @RequestParam("projectId") Long projectId) {
        taskService.performTaskCreation(authToken, taskEntity, projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("tasks")
    public ResponseEntity modifyTask(@RequestHeader("token") String authToken,
                                     @RequestBody TaskEntity taskEntity,
                                     @RequestParam("taskId") Long taskId) {
        taskService.performTaskModification(authToken, taskEntity, taskId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("tasks/assign")
    public ResponseEntity assignToTask(@RequestHeader("token") String authToken,
                                       @RequestParam("taskId") Long taskId,
                                       @RequestParam("userId") Long userId) {
        taskService.performTaskAssignment(authToken, taskId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("tasks")
    public ResponseEntity modifyTask(@RequestHeader("token") String authToken,
                                     @RequestParam("taskId") Long taskId) {
        taskService.performTaskDeletion(authToken, taskId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
