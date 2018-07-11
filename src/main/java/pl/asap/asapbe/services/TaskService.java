package pl.asap.asapbe.services;

import pl.asap.asapbe.entities.TaskEntity;
import java.util.List;
import java.util.Set;

public interface TaskService {

    List<TaskEntity> getAllTasksFromProject(String authToken, Long projectId);

    TaskEntity getTaskById(String authToken, Long taskId);

    TaskEntity performTaskCreation(String authToken, TaskEntity taskEntity, Long projectId);

    TaskEntity performTaskModification(String authToken, TaskEntity modifiedTask, Long taskId);

    void performTaskDeletion(String authToken, Long taskId);

    TaskEntity performTaskAssignment(String authToken, Long taskId, Long userId);

    boolean isTaskAlreadyCreatedInProject(Set<TaskEntity> tasksInProject, TaskEntity taskEntity);

    TaskEntity getTaskFromDbById(Long id);
}