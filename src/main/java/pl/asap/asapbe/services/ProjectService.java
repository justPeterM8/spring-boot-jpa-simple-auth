package pl.asap.asapbe.services;
import pl.asap.asapbe.entities.ProjectEntity;
import pl.asap.asapbe.entities.TaskEntity;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;

import java.util.*;

public interface ProjectService {

    List<ProjectEntity> getListOfAllProjects(String authToken);

    List<UserEntity> getAllUsersFromSpecificProject(String authToken, Long projectId);

    ProjectEntity performProjectCreation(String authToken, ProjectEntity projectEntity);

    ProjectEntity performProjectModification(String authToken, Long projectId, ProjectEntity modifiedProject);

    void performProjectDeletion(String authToken, Long projectId);

    ProjectEntity getProjectFromDbById(Long projectId);

    List<UserEntity> performAddingUserToProjectOperation(String authToken, Long projectId, Long userId);

    List<UserEntity> performDeletingUserFromProjectOperation(String authToken, Long projectId, Long userId);

    void updateProjectWithModifiedTaskData(ProjectEntity projectEntity, String title, TaskEntity taskAfterModification);

    void updateProjectTasksSetByRemovingDeletedItem(ProjectEntity projectEntity, String titleOfTaskToDelete);

    ProjectEntity updateUsersSetByRemovingDeletedItem(ProjectEntity projectEntity, UserEntity userEntity);

    boolean isUserPartOfProject(UserAuthDetailsEntity userAuthDetailsEntity, ProjectEntity projectEntity);
}