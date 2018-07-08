package pl.asap.asapbe.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.asap.asapbe.entities.ProjectEntity;
import pl.asap.asapbe.entities.TaskEntity;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.exceptions.InsufficientPermissionException;
import pl.asap.asapbe.exceptions.NoSuchProjectException;
import pl.asap.asapbe.exceptions.ProjectAlreadyExistsInDatabaseException;
import pl.asap.asapbe.exceptions.UserAuthenticationException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService extends BaseService {
    private UserService userService;

    @Autowired
    public ProjectService(UserService userService) {
        this.userService = userService;
    }

    public List<ProjectEntity> getListOfAllProjects(String authToken) {
        if (authService.authenticateUserByToken(authToken) != null) //user authenticated
            return projectRepository.findAll();
        else
            throw new UserAuthenticationException();
    }

    public List<UserEntity> getAllUsersFromSpecificProject(String authToken, Long projectId) {
        ProjectEntity projectToGetUsersFrom = getProjectFromDbById(projectId);
        UserAuthDetailsEntity userRequesting = authService.authenticateUserByToken(authToken);//possible users from project and outside project
        if (isUserPartOfProject(userRequesting, projectToGetUsersFrom)) {
            return new ArrayList<>(projectToGetUsersFrom.getUsers());
        } else {
            throw new InsufficientPermissionException();
        }
    }

    public void performProjectCreation(String authToken, ProjectEntity projectEntity) {
        if (projectRepository.findByTitle(projectEntity.getTitle()).isPresent())//project already exsits
            throw new ProjectAlreadyExistsInDatabaseException();
        else {//project doesn't exist
            UserAuthDetailsEntity userAuthDetailsEntity = authService.authenticateUserByToken(authToken);
            UserEntity supervisor = userService.getUserEntityFromUserAuthDetailsEntity(userAuthDetailsEntity);
            Set<UserEntity> usersInProject = new HashSet<>();
            usersInProject.add(supervisor);
            projectEntity.setSupervisor(supervisor);
            projectEntity.setUsers(usersInProject);
            projectRepository.save(projectEntity);
        }
    }

    public void performProjectModification(String authToken, Long projectId, ProjectEntity modifiedProject) {
        ProjectEntity projectToModify = getProjectFromDbById(projectId);
        UserAuthDetailsEntity modifier = authService.authenticateUserByToken(authToken);//possible supervisor or basic user
        if (modifier.getUserId().equals(projectToModify.getSupervisor().getId())) {//user is authorized to modify (only supervisor allowed to change project)
            projectToModify.setTitle(modifiedProject.getTitle());
            projectRepository.save(projectToModify);
        } else
            throw new InsufficientPermissionException();
    }

    public void performProjectDeletion(String authToken, Long projectId) {
        ProjectEntity projectToDelete = getProjectFromDbById(projectId);
        UserAuthDetailsEntity modifier = authService.authenticateUserByToken(authToken);//possible supervisor or basic user
        if (modifier.getUserId().equals(projectToDelete.getSupervisor().getId())) //user is authorized to modify (only supervisor allowed to change project)
            projectRepository.delete(projectToDelete);
        else
            throw new InsufficientPermissionException();
    }

    ProjectEntity getProjectFromDbById(Long projectId) {
        Optional<ProjectEntity> project = projectRepository.findById(projectId);
        if (!project.isPresent())
            throw new NoSuchProjectException();
        else
            return project.get();
    }

    public void performAddingUserToProjectOperation(String authToken, Long projectId, Long userId) {
        UserAuthDetailsEntity requestingUser = authService.authenticateUserByToken(authToken);
        ProjectEntity projectEntity = getProjectFromDbById(projectId);
        if (requestingUser.getUserId().equals(projectEntity.getSupervisor().getId())) {//only supervisor can add new members to project
            UserEntity userAddedToProject = userService.getUserFromDbById(userId);
            Set<UserEntity> usersInProject = projectEntity.getUsers();
            usersInProject.add(userAddedToProject);
            projectEntity.setUsers(usersInProject);
            projectRepository.save(projectEntity);
        } else
            throw new InsufficientPermissionException();
    }

    public void performDeletingUserFromProjectOperation(String authToken, Long projectId, Long userId) {
        UserAuthDetailsEntity requestingUser = authService.authenticateUserByToken(authToken);
        ProjectEntity projectEntity = getProjectFromDbById(projectId);
        if (requestingUser.getUserId().equals(projectEntity.getSupervisor().getId())) {//only supervisor can add new members to project
            UserEntity userDeletedFromProject = userService.getUserFromDbById(userId);
            updateUsersSetByRemovingDeletedItem(projectEntity, userDeletedFromProject);
        } else
            throw new InsufficientPermissionException();
    }

    void updateProjectWithModifiedTaskData(ProjectEntity projectEntity, String title, TaskEntity taskAfterModification) {
        projectEntity
                .getTasks()
                .stream()
                .filter(task -> task.getTitle().equals(title))
                .forEach(task -> task = taskAfterModification);//replacing reference, with this already in database
        projectRepository.save(projectEntity);
    }

    void updateProjectTasksSetByRemovingDeletedItem(ProjectEntity projectEntity, String titleOfTaskToDelete) {
        Set<TaskEntity> updatedTasksSet = projectEntity
                .getTasks()
                .stream()
                .filter(task -> !(task.getTitle().equals(titleOfTaskToDelete)))
                .collect(Collectors.toSet());
        projectEntity.setTasks(updatedTasksSet);
        projectRepository.save(projectEntity);
    }

    private void updateUsersSetByRemovingDeletedItem(ProjectEntity projectEntity, UserEntity userEntity) {
        Set<UserEntity> updatedUsersSet = projectEntity
                .getUsers()
                .stream()
                .filter(user -> !(user.getId().equals(userEntity.getId())))
                .collect(Collectors.toSet());
        projectEntity.setUsers(updatedUsersSet);
        projectRepository.save(projectEntity);
    }

    boolean isUserPartOfProject(UserAuthDetailsEntity userAuthDetailsEntity, ProjectEntity projectEntity) {
        return projectEntity
                .getUsers()
                .stream()
                .anyMatch(teamMember -> userAuthDetailsEntity.getUserId().equals(teamMember.getId()));
    }
}