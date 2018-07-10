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
    public ResponseEntity<List<TaskEntity>> getAllTasksFromSpecificProject(@RequestHeader("token") String authToken,
                                                           @RequestParam("projectId") Long projectId) {
        return ResponseEntity.ok(taskService.getAllTasksFromProject(authToken, projectId));
    }

    @GetMapping("tasks/task")
    public ResponseEntity<TaskEntity> getTask(@RequestHeader("token") String authToken,
                              @RequestParam("id") Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(authToken, taskId));
    }

    @PostMapping("tasks")
    public ResponseEntity createTask(@RequestHeader("token") String authToken,
                                     @RequestBody TaskEntity taskEntity,
                                     @RequestParam("projectId") Long projectId) {
        return ResponseEntity.ok(taskService.performTaskCreation(authToken, taskEntity, projectId));
    }

    @PutMapping("tasks")
    public ResponseEntity modifyTask(@RequestHeader("token") String authToken,
                                     @RequestBody TaskEntity taskEntity,
                                     @RequestParam("taskId") Long taskId) {
        return ResponseEntity.ok(taskService.performTaskModification(authToken, taskEntity, taskId));
    }

    @PutMapping("tasks/assign")
    public ResponseEntity<TaskEntity> assignToTask(@RequestHeader("token") String authToken,
                                       @RequestParam("taskId") Long taskId,
                                       @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(taskService.performTaskAssignment(authToken, taskId, userId));
    }

    @DeleteMapping("tasks")
    public ResponseEntity deleteTask(@RequestHeader("token") String authToken,
                                     @RequestParam("taskId") Long taskId) {
        taskService.performTaskDeletion(authToken, taskId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
