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
import pl.asap.asapbe.repositories.ProjectRepository;
import pl.asap.asapbe.repositories.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TaskServiceImpl implements TaskService{

    private ProjectServiceImpl projectServiceImpl;
    private UserServiceImpl userServiceImpl;
    private UserAuthDetailsServiceImpl userAuthDetailsServiceImpl;
    private AuthServiceImpl authServiceImpl;
    private ProjectRepository projectRepository;
    private TaskRepository taskRepository;

    @Autowired
    public TaskServiceImpl(ProjectServiceImpl projectServiceImpl, UserServiceImpl userServiceImpl, UserAuthDetailsServiceImpl userAuthDetailsServiceImpl, AuthServiceImpl authServiceImpl, ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectServiceImpl = projectServiceImpl;
        this.userServiceImpl = userServiceImpl;
        this.userAuthDetailsServiceImpl = userAuthDetailsServiceImpl;
        this.authServiceImpl = authServiceImpl;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public List<TaskEntity> getAllTasksFromProject(String authToken, Long projectId) {
        ProjectEntity projectToGetTasksFrom = projectServiceImpl.getProjectFromDbById(projectId);
        UserAuthDetailsEntity requestingUser = authServiceImpl.authenticateUserByToken(authToken);
        if (projectServiceImpl.isUserPartOfProject(requestingUser, projectToGetTasksFrom)) {
            return new ArrayList<>(projectToGetTasksFrom.getTasks());
        } else
            throw new InsufficientPermissionException();
    }

    public TaskEntity getTaskById(String authToken, Long taskId) {
        authServiceImpl.authenticateUserByToken(authToken);
        return getTaskFromDbById(taskId);
    }

    public TaskEntity performTaskCreation(String authToken, TaskEntity taskEntity, Long projectId) {
        UserAuthDetailsEntity requestingUser = authServiceImpl.authenticateUserByToken(authToken);
        UserEntity userThatCreatedTask = userServiceImpl.getUserEntityFromUserAuthDetailsEntity(requestingUser);
        ProjectEntity projectEntity = projectServiceImpl.getProjectFromDbById(projectId);
        if (!isTaskAlreadyCreatedInProject(projectEntity.getTasks(), taskEntity)) {//check if there is task with the same title already in this project
            Set<TaskEntity> tasks = projectEntity.getTasks();
            taskEntity.setAssignee(userThatCreatedTask);//user that triggered creation is assigned by default
            tasks.add(taskEntity);
            projectEntity.setTasks(tasks);
            ProjectEntity savedProject = projectRepository.save(projectEntity);
            taskEntity.setProject(savedProject);
            return taskRepository.save(taskEntity);
        } else
            throw new TaskAlreadyExistsInProjectException();
    }

    public TaskEntity performTaskModification(String authToken, TaskEntity modifiedTask, Long taskId) {
        authServiceImpl.authenticateUserByToken(authToken);
        TaskEntity taskToChange = getTaskFromDbById(taskId);
        ProjectEntity correspondingProject = projectServiceImpl.getProjectFromDbById(taskToChange.getProject().getId());
        if (!isTaskAlreadyCreatedInProject(correspondingProject.getTasks(), modifiedTask)) {//check if there is task with the same title already in this project
            //refreshing task's data (without assignee change, this is handled elsewhere)
            taskToChange.setTitle(modifiedTask.getTitle());
            taskToChange.setDescription(modifiedTask.getDescription());
            taskToChange.setPriority(modifiedTask.getPriority());
            taskToChange.setStatus(modifiedTask.getStatus());
            return taskRepository.save(taskToChange);
        } else
            throw new TaskAlreadyExistsInProjectException();
    }

    public void performTaskDeletion(String authToken, Long taskId) {//only project supervisor allowed
        UserAuthDetailsEntity requestingUser = authServiceImpl.authenticateUserByToken(authToken);
        TaskEntity taskToDelete = getTaskFromDbById(taskId);
        ProjectEntity projectToUpdate = taskToDelete.getProject();
        if (projectToUpdate.getSupervisor().getId().equals(requestingUser.getUserId())) {//user that is deleting must be project's supervisor
            taskRepository.delete(taskToDelete);
        } else
            throw new InsufficientPermissionException();
    }

    public TaskEntity performTaskAssignment(String authToken, Long taskId, Long userId){
        authServiceImpl.authenticateUserByToken(authToken);
        TaskEntity taskToUpdate = getTaskFromDbById(taskId);
        UserEntity newAssignee = userServiceImpl.getUserFromDbById(userId);
        ProjectEntity projectEntity = taskToUpdate.getProject();
        if (projectServiceImpl.isUserPartOfProject(userAuthDetailsServiceImpl.getUserAuthDetailsFromUserEntity(newAssignee), projectEntity)){
            taskToUpdate.setAssignee(newAssignee);
            projectServiceImpl.updateProjectWithModifiedTaskData(projectEntity, taskToUpdate.getTitle(), taskToUpdate);
            return taskToUpdate;
        } else {
            throw new UserNotPartOfProjectException();
        }
    }

    public boolean isTaskAlreadyCreatedInProject(Set<TaskEntity> tasksInProject, TaskEntity taskEntity) {//validating by title
        return tasksInProject
                .stream()
                .anyMatch(projectTask -> projectTask.getTitle().equals(taskEntity.getTitle()));
    }

    public TaskEntity getTaskFromDbById(Long id) {
        Optional<TaskEntity> taskEntity = taskRepository.findById(id);
        if (taskEntity.isPresent()) {
            return taskEntity.get();
        } else
            throw new NoSuchTaskException();
    }
}
