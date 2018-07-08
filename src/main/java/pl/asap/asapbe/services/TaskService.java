package pl.asap.asapbe.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.asap.asapbe.entities.ProjectEntity;
import pl.asap.asapbe.entities.TaskEntity;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.exceptions.InsufficientPermissionException;
import pl.asap.asapbe.exceptions.NoSuchTaskException;
import pl.asap.asapbe.exceptions.TaskAlreadyExistsInProjectException;
import pl.asap.asapbe.exceptions.UserNotPartOfProjectException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TaskService extends BaseService {

    private ProjectService projectService;
    private UserService userService;
    private UserAuthDetailsService userAuthDetailsService;

    @Autowired
    public TaskService(ProjectService projectService, UserService userService, UserAuthDetailsService userAuthDetailsService) {
        this.projectService = projectService;
        this.userService = userService;
        this.userAuthDetailsService = userAuthDetailsService;
    }

    public List<TaskEntity> getAllTasksFromProject(String authToken, Long projectId) {
        ProjectEntity projectToGetTasksFrom = projectService.getProjectFromDbById(projectId);
        UserAuthDetailsEntity requestingUser = authService.authenticateUserByToken(authToken);
        if (projectService.isUserPartOfProject(requestingUser, projectToGetTasksFrom)) {
            return new ArrayList<>(projectToGetTasksFrom.getTasks());
        } else
            throw new InsufficientPermissionException();
    }

    public TaskEntity getTaskById(String authToken, Long taskId) {
        authService.authenticateUserByToken(authToken);
        return getTaskFromDbById(taskId);
    }

    public void performTaskCreation(String authToken, TaskEntity taskEntity, Long projectId) {
        UserAuthDetailsEntity requestingUser = authService.authenticateUserByToken(authToken);
        UserEntity userThatCreatedTask = userService.getUserEntityFromUserAuthDetailsEntity(requestingUser);
        ProjectEntity projectEntity = projectService.getProjectFromDbById(projectId);
        if (!isTaskAlreadyCreatedInProject(projectEntity.getTasks(), taskEntity)) {//check if there is task with the same title already in this project
            Set<TaskEntity> tasks = projectEntity.getTasks();
            taskEntity.setAssignee(userThatCreatedTask);//user that triggered creation is assigned by default
            tasks.add(taskEntity);
            projectEntity.setTasks(tasks);
            ProjectEntity savedProject = projectRepository.save(projectEntity);
            taskEntity.setProject(savedProject);
            taskRepository.save(taskEntity);
        } else
            throw new TaskAlreadyExistsInProjectException();
    }

    public void performTaskModification(String authToken, TaskEntity modifiedTask, Long taskId) {
        authService.authenticateUserByToken(authToken);
        TaskEntity taskToChange = getTaskFromDbById(taskId);
        ProjectEntity correspondingProject = projectService.getProjectFromDbById(taskToChange.getProject().getId());
        if (!isTaskAlreadyCreatedInProject(correspondingProject.getTasks(), modifiedTask)) {//check if there is task with the same title already in this project
            String oldTitle = taskToChange.getTitle(); //keeping old task title, to find it in project data after saving task with new information
            //refreshing task's data (without assignee change, this is handled elsewhere)
            taskToChange.setTitle(modifiedTask.getTitle());
            taskToChange.setDescription(modifiedTask.getDescription());
            taskToChange.setPriority(modifiedTask.getPriority());
            taskToChange.setStatus(modifiedTask.getStatus());
            TaskEntity savedTask = taskRepository.save(taskToChange);
        } else
            throw new TaskAlreadyExistsInProjectException();
    }

    public void performTaskDeletion(String authToken, Long taskId) {//only project supervisor allowed
        UserAuthDetailsEntity requestingUser = authService.authenticateUserByToken(authToken);
        TaskEntity taskToDelete = getTaskFromDbById(taskId);
        ProjectEntity projectToUpdate = taskToDelete.getProject();
        if (projectToUpdate.getSupervisor().getId().equals(requestingUser.getUserId())) {//user that is deleting must be project's supervisor
            taskRepository.delete(taskToDelete);
        } else
            throw new InsufficientPermissionException();
    }

    public void performTaskAssignment(String authToken, Long taskId, Long userId){
        authService.authenticateUserByToken(authToken);
        TaskEntity taskToUpdate = getTaskFromDbById(taskId);
        UserEntity newAssignee = userService.getUserFromDbById(userId);
        ProjectEntity projectEntity = taskToUpdate.getProject();
        if (projectService.isUserPartOfProject(userAuthDetailsService.getUserAuthDetailsFromUserEntity(newAssignee), projectEntity)){
            taskToUpdate.setAssignee(newAssignee);
            projectService.updateProjectWithModifiedTaskData(projectEntity, taskToUpdate.getTitle(), taskToUpdate);
        } else {
            throw new UserNotPartOfProjectException();
        }
    }

    private boolean isTaskAlreadyCreatedInProject(Set<TaskEntity> tasksInProject, TaskEntity taskEntity) {//validating by title
        return tasksInProject
                .stream()
                .anyMatch(projectTask -> projectTask.getTitle().equals(taskEntity.getTitle()));
    }

    private TaskEntity getTaskFromDbById(Long id) {
        Optional<TaskEntity> taskEntity = taskRepository.findById(id);
        if (taskEntity.isPresent()) {
            return taskEntity.get();
        } else
            throw new NoSuchTaskException();
    }
}
