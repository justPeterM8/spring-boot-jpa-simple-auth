package pl.asap.asapbe.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.asap.asapbe.entities.*;
import pl.asap.asapbe.exceptions.InsufficientPermissionException;
import pl.asap.asapbe.exceptions.NoSuchProjectException;
import pl.asap.asapbe.exceptions.ProjectAlreadyExistsInDatabaseException;
import pl.asap.asapbe.exceptions.UserAuthenticationException;
import pl.asap.asapbe.repositories.ProjectRepository;

import java.util.*;

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

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findAll();
        verify(projectRepository, never()).findById(anyLong());
    }

    @Test(expected = UserAuthenticationException.class)
    public void testGetListOfAllProjectsFailure() {
        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        List<ProjectEntity> projects = Collections.singletonList(projectEntity1);

        when(projectRepository.findAll()).thenReturn(projects);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(null);

        List<ProjectEntity> projectsReturned = projectServiceImpl.getListOfAllProjects("1231-123-123");
        //Should throw exception related to failure in authentication

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
    }

    @Test
    public void testGetAllUsersFromSpecificProjectSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        Set<UserEntity> users = new HashSet<>();
        users.add(user1);
        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        projectEntity1.setUsers(users);
        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity1);
        List<UserEntity> usersListExpected = new ArrayList<>(users);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);

        List<UserEntity> usersReturned = projectServiceImpl.getAllUsersFromSpecificProject("1231-123-123", 1L);

        assertNotNull(usersReturned);
        assertEquals(usersReturned, usersListExpected);

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
    }

    @Test(expected = InsufficientPermissionException.class)
    public void testGetAllUsersFromSpecificProjectFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        Set<UserEntity> users = new HashSet<>();
        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        projectEntity1.setUsers(users);
        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity1);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);

        List<UserEntity> usersReturned = projectServiceImpl.getAllUsersFromSpecificProject("1231-123-123", 1L);
        //Should throw exception related to failure in permission validation

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
    }

    @Test
    public void testPerformProjectCreationSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");
        UserEntity user = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user.setId(1L);
        ProjectEntity projectEntity = new ProjectEntity("Test project");
        projectEntity.setId(1L);
        Optional<ProjectEntity> projectOptional = Optional.empty();

        when(projectRepository.findByTitle(anyString())).thenReturn(projectOptional);
        when(projectRepository.save(any())).thenReturn(projectEntity);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(userServiceImpl.getUserEntityFromUserAuthDetailsEntity(any())).thenReturn(user);

        ProjectEntity projectReturned = projectServiceImpl.performProjectCreation("1231-123-123", projectEntity);
        assertNotNull(projectReturned);
        assertEquals(projectEntity.getId(), projectReturned.getId());

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findByTitle(anyString());
        verify(projectRepository, never()).findAll();
        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
    }

    @Test(expected = ProjectAlreadyExistsInDatabaseException.class)
    public void testPerformProjectCreationFailure() {
        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity1);

        when(projectRepository.findByTitle(anyString())).thenReturn(projectOptional);
        projectServiceImpl.performProjectCreation("1231-123-123", projectEntity1);
        //should throw exception related to the state of project already existing

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
    }

    @Test
    public void testPerformProjectModificationSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");
        UserEntity user = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user.setId(1L);
        ProjectEntity projectEntity = new ProjectEntity("Test project");
        ProjectEntity projectBody = new ProjectEntity("Test project");
        projectEntity.setId(1L);
        projectEntity.setSupervisor(user);
        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity);

        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(projectRepository.save(any())).thenReturn(projectEntity);

        ProjectEntity projectReturned = projectServiceImpl.performProjectModification("1231-123-123", 1L, projectBody);
        assertNotNull(projectReturned);
        assertEquals(projectEntity, projectReturned);


        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
    }

    @Test(expected = InsufficientPermissionException.class)
    public void testPerformProjectModificationFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");
        UserEntity user = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user.setId(1L);
        ProjectEntity projectEntity = new ProjectEntity("Test project");
        ProjectEntity projectBody = new ProjectEntity("Test project");
        projectEntity.setId(1L);
        projectEntity.setSupervisor(user);
        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity);

        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(projectRepository.save(any())).thenReturn(projectEntity);

        ProjectEntity projectReturned = projectServiceImpl.performProjectModification("1231-123-123", 1L, projectBody);
        //Should throw exception related to user's insufficient permisions to modify this project

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
    }

    @Test
    public void testPerformProjectDeletionSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");
        UserEntity user = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user.setId(1L);
        ProjectEntity projectEntity = new ProjectEntity("Test project");
        projectEntity.setId(1L);
        projectEntity.setSupervisor(user);
        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity);

        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);

        projectServiceImpl.performProjectDeletion("1231-123-123", 1L);

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
        verify(projectRepository, times(1)).delete(any(ProjectEntity.class));
    }

    @Test(expected = InsufficientPermissionException.class)
    public void testPerformProjectDeletionFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");
        UserEntity user = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user.setId(1L);
        ProjectEntity projectEntity = new ProjectEntity("Test project");
        projectEntity.setId(1L);
        projectEntity.setSupervisor(user);
        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity);

        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);

        projectServiceImpl.performProjectDeletion("1231-123-123", 1L);
        //Should throw exception related to user's insufficient permisions to modify this project

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
    }


    @Test
    public void testGetProjectFromDbByIdSuccess() {
        ProjectEntity projectEntity = new ProjectEntity("Test project");
        projectEntity.setId(1L);
        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity);

        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);

        ProjectEntity projectEntityReturned = projectServiceImpl.getProjectFromDbById(1L);

        assertNotNull(projectEntityReturned);
        assertEquals(projectEntity, projectEntityReturned);

        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
    }

    @Test(expected = NoSuchProjectException.class)
    public void testGetProjectFromDbByIdFailure() {
        Optional<ProjectEntity> projectOptional = Optional.empty();

        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);

        ProjectEntity projectEntityReturned = projectServiceImpl.getProjectFromDbById(1L);
        //Should throw exception related to the state where there is no such project in database

        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
    }

    @Test
    public void testPerformAddingUserToProjectOperationSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user.setId(1L);
        UserEntity userAdded = new UserEntity("Tomasz", "Kostrzewa", "tomasz_kostrzewa@gmail.com", "pass123");
        userAdded.setId(2L);

        Set<UserEntity> usersSet = new HashSet<>();
        usersSet.add(user);
        usersSet.add(userAdded);

        ProjectEntity projectEntity = new ProjectEntity("Test project");
        projectEntity.setId(1L);
        projectEntity.setSupervisor(user);
        projectEntity.setUsers(usersSet);

        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity);

        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);
        when(userServiceImpl.getUserFromDbById(anyLong())).thenReturn(userAdded);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(projectRepository.save(any())).thenReturn(projectEntity);

        projectServiceImpl.performAddingUserToProjectOperation("1231-123-123", 1L, 2L);

        verify(authServiceImpl, times(2)).authenticateUserByToken(anyString());
        verify(projectRepository, times(2)).findById(anyLong());
        verify(projectRepository, never()).findAll();
        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
    }

    @Test(expected = InsufficientPermissionException.class)
    public void testPerformAddingUserToProjectOperationFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user.setId(2L);

        ProjectEntity projectEntity = new ProjectEntity("Test project");
        projectEntity.setId(1L);
        projectEntity.setSupervisor(user);

        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity);

        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);

        projectServiceImpl.performAddingUserToProjectOperation("1231-123-123", 1L, 2L);
        //Should throw exception related to state where user performing adding another user to project has insufficient permission

        verify(authServiceImpl, times(2)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    public void testPerformDeletingUserFromProjectOperationSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);//user1 is supervisor according to above
        UserEntity user2 = new UserEntity("Tomasz", "Kostrzewa", "tomasz_kostrzewa@gmail.com", "pass123");
        user2.setId(2L);

        Set<UserEntity> users = new HashSet<>();
        users.add(user1);
        users.add(user2);

        ProjectEntity projectEntity = new ProjectEntity("Test project");
        projectEntity.setId(1L);
        projectEntity.setSupervisor(user1);
        projectEntity.setUsers(users);
        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);
        when(projectRepository.save(any())).thenReturn(projectEntity);
        when(userServiceImpl.getUserFromDbById(anyLong())).thenReturn(user2);//user to delete

        projectServiceImpl.performDeletingUserFromProjectOperation("1231-123-123", 1L, 1L);

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
    }

    @Test(expected = InsufficientPermissionException.class)
    public void testPerformDeletingUserFromProjectOperationFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);//user1 is supervisor according to above
        UserEntity user2 = new UserEntity("Tomasz", "Kostrzewa", "tomasz_kostrzewa@gmail.com", "pass123");
        user2.setId(2L);

        ProjectEntity projectEntity = new ProjectEntity("Test project");
        projectEntity.setId(1L);
        projectEntity.setSupervisor(user2);
        Optional<ProjectEntity> projectOptional = Optional.of(projectEntity);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(projectRepository.findById(anyLong())).thenReturn(projectOptional);
        when(projectRepository.save(any())).thenReturn(projectEntity);
        when(userServiceImpl.getUserFromDbById(anyLong())).thenReturn(user2);//user to delete

        projectServiceImpl.performDeletingUserFromProjectOperation("1231-123-123", 1L, 1L);
        //Should throw exception related to state where user performing deleting another user to project has insufficient permission

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).findById(anyLong());
        verify(projectRepository, never()).findAll();
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    public void testUpdateProjectWithModifiedTaskData() {
        TaskEntity taskBeforeModification = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);

        Set<TaskEntity> tasksBeforeChange = new HashSet<>();
        tasksBeforeChange.add(taskBeforeModification);

        TaskEntity taskAfterModification = new TaskEntity("Test task", "New description after change", Status.IN_TESTS, Priority.NORMAL);

        ProjectEntity projectBeforeChange = new ProjectEntity("Test project");
        projectBeforeChange.setId(1L);
        projectBeforeChange.setTasks(tasksBeforeChange);

        projectServiceImpl.updateProjectWithModifiedTaskData(projectBeforeChange, taskBeforeModification.getTitle(), taskAfterModification);

        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
    }

    @Test
    public void testUpdateProjectTasksSetByRemovingDeletedItem() {
        TaskEntity taskToDelete = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);

        Set<TaskEntity> tasksBeforeChange = new HashSet<>();
        tasksBeforeChange.add(taskToDelete);

        ProjectEntity projectToUpdate = new ProjectEntity("Test project");
        projectToUpdate.setId(1L);
        projectToUpdate.setTasks(tasksBeforeChange);

        projectServiceImpl.updateProjectTasksSetByRemovingDeletedItem(projectToUpdate, taskToDelete.getTitle());

        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
    }

    @Test
    public void testUpdateUsersSetByRemovingDeletedItem(){
        UserEntity userToDelete = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        userToDelete.setId(1L);

        UserEntity user2 = new UserEntity("Tomasz", "Kostrzewa", "tomasz_kostrzewa@gmail.com", "pass123");
        user2.setId(2L);

        Set<UserEntity> usersBeforeChange = new HashSet<>();
        usersBeforeChange.add(userToDelete);
        usersBeforeChange.add(user2);

        Set<UserEntity> usersChanged = new HashSet<>();
        usersChanged.add(userToDelete);

        ProjectEntity projectToUpdate = new ProjectEntity("Test project");
        projectToUpdate.setId(1L);
        projectToUpdate.setUsers(usersBeforeChange);

        ProjectEntity projectUpdated = new ProjectEntity("Test project");
        projectUpdated.setId(1L);
        projectUpdated.setUsers(usersChanged);

        when(projectRepository.save(any())).thenReturn(projectUpdated);

        ProjectEntity projectReturned = projectServiceImpl.updateUsersSetByRemovingDeletedItem(projectToUpdate, userToDelete);
        assertNotNull(projectReturned);
        assertEquals(projectUpdated, projectReturned);

        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
    }

    @Test
    public void testIsUserPartOfProjectPositive() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        UserEntity user2 = new UserEntity("Tomasz", "Kostrzewa", "tomasz_kostrzewa@gmail.com", "pass123");
        user2.setId(2L);

        Set<UserEntity> users = new HashSet<>();
        users.add(user1);
        users.add(user2);

        ProjectEntity project = new ProjectEntity("Test project");
        project.setId(1L);
        project.setUsers(users);

        Boolean userParticipating = projectServiceImpl.isUserPartOfProject(userAuthDetailsEntity, project);

        assertNotNull(userParticipating);
        assertEquals(true, userParticipating);
    }

    @Test
    public void testIsUserPartOfProjectNegative() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user2 = new UserEntity("Tomasz", "Kostrzewa", "tomasz_kostrzewa@gmail.com", "pass123");
        user2.setId(2L);

        Set<UserEntity> users = new HashSet<>();
        users.add(user2);

        ProjectEntity project = new ProjectEntity("Test project");
        project.setId(1L);
        project.setUsers(users);

        Boolean userParticipating = projectServiceImpl.isUserPartOfProject(userAuthDetailsEntity, project);

        assertNotNull(userParticipating);
        assertEquals(false, userParticipating);
    }
}