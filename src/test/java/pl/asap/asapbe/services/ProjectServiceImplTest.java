package pl.asap.asapbe.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.asap.asapbe.entities.ProjectEntity;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.exceptions.UserAuthenticationException;
import pl.asap.asapbe.repositories.ProjectRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProjectServiceImplTest {
    @Mock
    AuthServiceImpl authServiceImpl;

    @Mock
    UserServiceImpl userServiceImpl;

    @Mock
    ProjectRepository projectRepository;

    ProjectServiceImpl projectServiceImpl;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        projectServiceImpl = new ProjectServiceImpl(userServiceImpl, authServiceImpl, projectRepository);
    }

    @Test
    public void testGetListOfAllProjectsSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");
        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);

        List<ProjectEntity> projects = Collections.singletonList(projectEntity1);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(projectRepository.findAll()).thenReturn(projects);

        List<ProjectEntity> projectsReturned = projectServiceImpl.getListOfAllProjects("1231-123-123");
        assertNotNull(projectsReturned);
        assertEquals(projects, projectsReturned);

        verify(projectRepository, times(1)).findAll();
        verify(projectRepository, never()).findById(anyLong());
    }

    @Test(expected = UserAuthenticationException.class)
    public void testGetListOfAllProjectsFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");
        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);

        List<ProjectEntity> projects = Collections.singletonList(projectEntity1);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(null);
        when(projectRepository.findAll()).thenReturn(projects);

        List<ProjectEntity> projectsReturned = projectServiceImpl.getListOfAllProjects("1231-123-123");
        //Should throw exception related to failure in authentication
    }

    @Test
    public void testGetAllUsersFromSpecificProject() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);

        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);

        when(projectServiceImpl.getProjectFromDbById(anyLong())).thenReturn(projectEntity1);

//        List<UserEntity>


    }

    @Test
    public void testPerformProjectCreation() {
    }

    @Test
    public void testPerformProjectModification() {
    }

    @Test
    public void testPerformProjectDeletion() {
    }

    @Test
    public void testGetProjectFromDbById() {
    }

    @Test
    public void testPerformAddingUserToProjectOperation() {
    }

    @Test
    public void testPerformDeletingUserFromProjectOperation() {
    }

    @Test
    public void testUpdateProjectWithModifiedTaskData() {
    }

    @Test
    public void testUpdateProjectTasksSetByRemovingDeletedItem() {
    }

    @Test
    public void testIsUserPartOfProject() {
    }
}