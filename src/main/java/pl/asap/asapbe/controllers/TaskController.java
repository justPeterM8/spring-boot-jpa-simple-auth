package pl.asap.asapbe.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.asap.asapbe.entities.TaskEntity;
import pl.asap.asapbe.services.TaskServiceImpl;

import java.util.List;

@RestController
public class TaskController {

    private TaskServiceImpl taskServiceImpl;

    @Autowired
    public TaskController(TaskServiceImpl taskServiceImpl) {
        this.taskServiceImpl = taskServiceImpl;
    }

    @GetMapping("tasks")
    public ResponseEntity<List<TaskEntity>> getAllTasksFromSpecificProject(@RequestHeader("token") String authToken,
                                                           @RequestParam("projectId") Long projectId) {
        return ResponseEntity.ok(taskServiceImpl.getAllTasksFromProject(authToken, projectId));
    }

    @GetMapping("tasks/task")
    public ResponseEntity<TaskEntity> getTask(@RequestHeader("token") String authToken,
                              @RequestParam("id") Long taskId) {
        return ResponseEntity.ok(taskServiceImpl.getTaskById(authToken, taskId));
    }

    @PostMapping("tasks")
    public ResponseEntity createTask(@RequestHeader("token") String authToken,
                                     @RequestBody TaskEntity taskEntity,
                                     @RequestParam("projectId") Long projectId) {
        return ResponseEntity.ok(taskServiceImpl.performTaskCreation(authToken, taskEntity, projectId));
    }

    @PutMapping("tasks")
    public ResponseEntity modifyTask(@RequestHeader("token") String authToken,
                                     @RequestBody TaskEntity taskEntity,
                                     @RequestParam("taskId") Long taskId) {
        return ResponseEntity.ok(taskServiceImpl.performTaskModification(authToken, taskEntity, taskId));
    }

    @PutMapping("tasks/assign")
    public ResponseEntity<TaskEntity> assignToTask(@RequestHeader("token") String authToken,
                                       @RequestParam("taskId") Long taskId,
                                       @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(taskServiceImpl.performTaskAssignment(authToken, taskId, userId));
    }

    @DeleteMapping("tasks")
    public ResponseEntity deleteTask(@RequestHeader("token") String authToken,
                                     @RequestParam("taskId") Long taskId) {
        taskServiceImpl.performTaskDeletion(authToken, taskId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
