package pl.asap.asapbe.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.asap.asapbe.entities.ProjectEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.services.ProjectService;

import java.util.List;

@RestController
public class ProjectController {
    private ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
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
    public ResponseEntity createProject(@RequestHeader("token") String authToken,
                                        @RequestBody ProjectEntity project) {
        projectService.performProjectCreation(authToken, project);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/projects")
    public ResponseEntity modifyProject(@RequestHeader("token") String authToken,
                                        @RequestParam("id") Long projectId,
                                        @RequestBody ProjectEntity project) {
        projectService.performProjectModification(authToken, projectId, project);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/projects/addUser")
    public ResponseEntity addUserToProject(@RequestHeader("token") String authToken,
                                           @RequestParam("projectId") Long projectId,
                                           @RequestParam("userId") Long userId) {
        projectService.performAddingUserToProjectOperation(authToken, projectId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/projects/deleteUser")
    public ResponseEntity deleteUserFromProject(@RequestHeader("token") String authToken,
                                                @RequestParam("projectId") Long projectId,
                                                @RequestParam("userId") Long userId) {
        projectService.performDeletingUserFromProjectOperation(authToken, projectId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/projects")
    public ResponseEntity deleteProject(@RequestHeader("token") String authToken,
                                        @RequestParam("id") Long projectId) {
        projectService.performProjectDeletion(authToken, projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
