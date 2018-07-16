package pl.asap.asapbe.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.asap.asapbe.entities.ProjectEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.services.ProjectService;
import pl.asap.asapbe.services.ProjectServiceImpl;

import java.util.List;

@RestController
public class ProjectController {
    private ProjectService projectService;

    @Autowired
    public ProjectController(ProjectServiceImpl projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/projects")
    public List<ProjectEntity> getAllProjects(@RequestHeader("token") String authToken) {
        return projectService.getListOfAllProjects(authToken);
    }

    @GetMapping("/projects/users")
    public List<UserEntity> getAllUsersInProject(@RequestHeader("token") String authToken, @RequestParam("projectId") Long projectId) {
        return projectService.getAllUsersFromSpecificProject(authToken, projectId);
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectEntity> createProject(@RequestHeader("token") String authToken,
                                        @RequestBody ProjectEntity project) {
        return ResponseEntity.ok(projectService.performProjectCreation(authToken, project));
    }

    @PutMapping("/projects")
    public ResponseEntity<ProjectEntity> modifyProject(@RequestHeader("token") String authToken,
                                        @RequestParam("id") Long projectId,
                                        @RequestBody ProjectEntity project) {

        return ResponseEntity.ok(projectService.performProjectModification(authToken, projectId, project));
    }

    @PutMapping("/projects/addUser")
    public ResponseEntity<List<UserEntity>> addUserToProject(@RequestHeader("token") String authToken,
                                           @RequestParam("projectId") Long projectId,
                                           @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(projectService.performAddingUserToProjectOperation(authToken, projectId, userId));
    }

    @PutMapping("/projects/deleteUser")
    public ResponseEntity<List<UserEntity>> deleteUserFromProject(@RequestHeader("token") String authToken,
                                                @RequestParam("projectId") Long projectId,
                                                @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(projectService.performDeletingUserFromProjectOperation(authToken, projectId, userId));
    }

    @DeleteMapping("/projects")
    public ResponseEntity deleteProject(@RequestHeader("token") String authToken,
                                        @RequestParam("id") Long projectId) {
        projectService.performProjectDeletion(authToken, projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
