package pl.asap.asapbe.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.asap.asapbe.entities.*;
import pl.asap.asapbe.exceptions.EmailAlreadyExistsInDatabaseException;
import pl.asap.asapbe.exceptions.UserAuthenticationException;
import pl.asap.asapbe.exceptions.UserNotFoundException;
import pl.asap.asapbe.repositories.TaskRepository;
import pl.asap.asapbe.repositories.UserAuthDetailsRepository;
import pl.asap.asapbe.repositories.UserRepository;
import pl.asap.asapbe.response_model.UserDetails;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @Mock
    UserAuthDetailsServiceImpl userAuthDetailsServiceImpl;

    @Mock
    AuthServiceImpl authServiceImpl;

    @Mock
    UserRepository userRepository;

    @Mock
    UserAuthDetailsRepository userAuthDetailsRepository;

    @Mock
    TaskRepository taskRepository;

    UserServiceImpl userServiceImpl;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userServiceImpl = new UserServiceImpl(userAuthDetailsServiceImpl, authServiceImpl, userRepository, userAuthDetailsRepository, taskRepository);
    }

    @Test
    public void testGetListOfAllUsersSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        UserEntity user2 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user2.setId(1L);
        List<UserEntity> users = Arrays.asList(user1, user2);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(userRepository.findAll()).thenReturn(users);

        List<UserEntity> returnedUsers = userServiceImpl.getListOfAllUsers("1231-123-123");

        assertNotNull(returnedUsers);
        assertEquals(users, returnedUsers);

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(userRepository, times(1)).findAll();
    }

    @Test(expected = UserAuthenticationException.class)
    public void testGetListOfAllUsersFailure() {
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        UserEntity user2 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user2.setId(1L);
        List<UserEntity> users = Arrays.asList(user1, user2);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(null);
        when(userRepository.findAll()).thenReturn(users);

        List<UserEntity> returnedUsers = userServiceImpl.getListOfAllUsers("1231-123-123");
        //should throw exception related to state in which user failed to authenticate

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testPerformUserLoginSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        String encryptedPassword = "3FC0A7ACF087F549AC2B266BAF94B8B1";

        when(authServiceImpl.encryptPassword(anyString())).thenReturn(encryptedPassword);
        when(userRepository.findByEmailAndPassword(anyString(), anyString())).thenReturn(user1);
        when(userAuthDetailsServiceImpl.getUserAuthDetailsFromUserEntity(any())).thenReturn(userAuthDetailsEntity);

        UserAuthDetailsEntity userDetailsReturned = userServiceImpl.performUserLogin("jan_kowalski@gmail.com", "qwerty123");

        assertNotNull(userDetailsReturned);
        assertEquals(userAuthDetailsEntity, userDetailsReturned);

        verify(userRepository, times(1)).findByEmailAndPassword(anyString(), anyString());
    }

    @Test(expected = UserAuthenticationException.class)
    public void testPerformUserLoginFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        String encryptedPassword = "3FC0A7ACF087F549AC2B266BAF94B8B1";

        when(authServiceImpl.encryptPassword(anyString())).thenReturn(encryptedPassword);
        when(userRepository.findByEmailAndPassword(anyString(), anyString())).thenReturn(null);
        when(userAuthDetailsServiceImpl.getUserAuthDetailsFromUserEntity(any())).thenReturn(userAuthDetailsEntity);

        UserAuthDetailsEntity userDetailsReturned = userServiceImpl.performUserLogin("jan_kowalski@gmail.com", "qwerty123");
        //should throw exception related to state in which user was not found in database

        verify(userRepository, times(1)).findByEmailAndPassword(anyString(), anyString());
    }

    @Test
    public void testPerformUserRegistrationSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        user1.setId(1L);

        String encryptedPassword = "3FC0A7ACF087F549AC2B266BAF94B8B1";
        String authToken = "5fa20d26-9982-4404-96d7-8ebcd5421ea6";

        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(authServiceImpl.encryptPassword(anyString())).thenReturn(encryptedPassword);
        when(authServiceImpl.generateToken()).thenReturn(authToken);
        when(userRepository.save(any())).thenReturn(user1);
        when(userAuthDetailsRepository.save(any())).thenReturn(userAuthDetailsEntity);

        UserAuthDetailsEntity userDetailsReturned = userServiceImpl.performUserRegistration("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");

        assertNotNull(userDetailsReturned);
        assertEquals(userAuthDetailsEntity, userDetailsReturned);

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, never()).findAll();
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(userAuthDetailsRepository, times(1)).save(any(UserAuthDetailsEntity.class));
    }

    @Test
    public void testPerformUserRegistrationFailureLogin() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        user1.setId(1L);

        String encryptedPassword = "3FC0A7ACF087F549AC2B266BAF94B8B1";
        String authToken = "5fa20d26-9982-4404-96d7-8ebcd5421ea6";

        when(userRepository.findByEmail(anyString())).thenReturn(user1);
        when(authServiceImpl.encryptPassword(anyString())).thenReturn(encryptedPassword);
        when(userRepository.findByEmailAndPassword(anyString(), anyString())).thenReturn(user1);
        when(userAuthDetailsServiceImpl.getUserAuthDetailsFromUserEntity(any())).thenReturn(userAuthDetailsEntity);
        when(authServiceImpl.generateToken()).thenReturn(authToken);

        UserAuthDetailsEntity userDetailsReturned = userServiceImpl.performUserRegistration("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");

        assertNotNull(userDetailsReturned);
        assertEquals(userAuthDetailsEntity, userDetailsReturned);

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, never()).findAll();
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(userAuthDetailsRepository, never()).save(any(UserAuthDetailsEntity.class));
        verify(userRepository, times(1)).findByEmailAndPassword(anyString(), anyString());
    }

    @Test(expected = EmailAlreadyExistsInDatabaseException.class)
    public void testPerformUserRegistrationFailureException() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        user1.setId(1L);

        String encryptedPassword = "3FC0A7ACF087F549AC2B266BAF94B8B1";
        String authToken = "5fa20d26-9982-4404-96d7-8ebcd5421ea6";

        when(userRepository.findByEmail(anyString())).thenReturn(user1);
        when(authServiceImpl.encryptPassword(anyString())).thenReturn(encryptedPassword);
        when(userRepository.findByEmailAndPassword(anyString(), anyString())).thenReturn(user1);
        when(userAuthDetailsServiceImpl.getUserAuthDetailsFromUserEntity(any())).thenReturn(userAuthDetailsEntity);
        when(authServiceImpl.generateToken()).thenReturn(authToken);

        UserAuthDetailsEntity userDetailsReturned = userServiceImpl.performUserRegistration("Janusz", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        //should throw exception related to state in which there is already email for different data set saved in database
    }

    @Test
    public void testPerformUserDeletion() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity userForTask = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        userForTask.setId(1L);

        ProjectEntity project = new ProjectEntity("Test project");
        project.setId(1L);
        project.setSupervisor(userForTask);

        TaskEntity task = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        task.setAssignee(userForTask);
        task.setProject(project);

        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(task);

        Set<ProjectEntity> projects = new HashSet<>();
        projects.add(project);

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        user1.setId(1L);
        user1.setTasks(tasks);
        user1.setProjects(projects);

        Optional<UserEntity> userEntityOptional = Optional.of(user1);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(userRepository.findById(anyLong())).thenReturn(userEntityOptional);

        userServiceImpl.performUserDeletion("1231-123-123");

        verify(taskRepository, times(1)).save(any(TaskEntity.class));
        verify(userRepository, times(1)).delete(any(UserEntity.class));
        verify(userAuthDetailsRepository, times(1)).delete(any(UserAuthDetailsEntity.class));
    }


    @Test
    public void testGetUserEntityFromUserAuthDetailsEntitySuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        user1.setId(1L);

        Optional<UserEntity> userEntityOptional = Optional.of(user1);

        when(userRepository.findById(any())).thenReturn(userEntityOptional);

        UserEntity userReturned = userServiceImpl.getUserEntityFromUserAuthDetailsEntity(userAuthDetailsEntity);

        assertNotNull(userReturned);
        assertEquals(user1, userReturned);

        verify(userRepository, never()).delete(any(UserEntity.class));
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetUserEntityFromUserAuthDetailsEntityFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        Optional<UserEntity> userEntityOptional = Optional.empty();

        when(userRepository.findById(any())).thenReturn(userEntityOptional);

        UserEntity userReturned = userServiceImpl.getUserEntityFromUserAuthDetailsEntity(userAuthDetailsEntity);
        //Should throw exception that relates to state in which user is not found in database

        verify(userRepository, never()).delete(any(UserEntity.class));
    }

    @Test
    public void testPerformUserPasswordChangeOperationSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "3FC0A7ACF087F549AC2B266BAF94B8B1");
        user1.setId(1L);

        Optional<UserEntity> userEntityOptional = Optional.of(user1);

        String encryptedPassword = "3FC0A7ACF087F549AC2B266BAF94B8B1";

        when(authServiceImpl.encryptPassword(anyString())).thenReturn(encryptedPassword);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(userRepository.findById(any())).thenReturn(userEntityOptional);

        userServiceImpl.performUserPasswordChangeOperation("1231-123-123", "qwerty123", "pass123");

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test(expected = UserAuthenticationException.class)
    public void testPerformUserPasswordChangeOperationFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "notMatchingPasswordToEncrypted");
        user1.setId(1L);

        Optional<UserEntity> userEntityOptional = Optional.of(user1);

        String encryptedPassword = "3FC0A7ACF087F549AC2B266BAF94B8B1";

        when(authServiceImpl.encryptPassword(anyString())).thenReturn(encryptedPassword);
        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(userRepository.findById(any())).thenReturn(userEntityOptional);

        userServiceImpl.performUserPasswordChangeOperation("1231-123-123", "qwerty123", "pass123");
        //should throw exception related to not matching password, that results authentication exception

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }


    @Test
    public void testPerformUserModification() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "notMatchingPasswordToEncrypted");
        user1.setId(1L);

        UserEntity changedUser = new UserEntity("Marek", "Kostrzewa", "marek_kostrzewa@gmail.com", "pass123");
        changedUser.setId(1L);

        Optional<UserEntity> userEntityOptional = Optional.of(user1);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(userRepository.findById(any())).thenReturn(userEntityOptional);
        when(userRepository.save(any(UserEntity.class))).thenReturn(changedUser);

        UserEntity userReturned = userServiceImpl.performUserModification("1231-123-123", changedUser);

        assertNotNull(userReturned);
        assertEquals(changedUser, userReturned);

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    public void testGetUserDetails() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Marek", "Kostrzewa", "marek_kostrzewa@gmail.com", "pass123");
        user1.setId(1L);

        Optional<UserEntity> userEntityOptional = Optional.of(user1);

        UserDetails userDetailsExpected = new UserDetails("Marek", "Kostrzewa");

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(userRepository.findById(any())).thenReturn(userEntityOptional);

        UserDetails userDetailsReturned = userServiceImpl.getUserDetails("1231-123-123");

        assertNotNull(userDetailsReturned);
        assertEquals(userDetailsExpected.getFirstName(), userDetailsReturned.getFirstName());
        assertEquals(userDetailsExpected.getLastName(), userDetailsReturned.getLastName());
    }

    @Test
    public void testGetUserFromDbByIdSuccess() {
        UserEntity user1 = new UserEntity("Marek", "Kostrzewa", "marek_kostrzewa@gmail.com", "pass123");
        user1.setId(1L);

        Optional<UserEntity> userEntityOptional = Optional.of(user1);

        when(userRepository.findById(any())).thenReturn(userEntityOptional);

        UserEntity userEntityReturned = userServiceImpl.getUserFromDbById(1L);

        assertNotNull(userEntityReturned);
        assertEquals(user1, userEntityReturned);

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetUserFromDbByIdFailure() {
        Optional<UserEntity> userEntityOptional = Optional.empty();

        when(userRepository.findById(any())).thenReturn(userEntityOptional);

        UserEntity userEntityReturned = userServiceImpl.getUserFromDbById(1L);
        //should throw exception related to state in which there is no such user in database

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testUpdateTasksDataAfterUserDeletionUserAvailable() {
        UserEntity userForTask = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        userForTask.setId(1L);

        ProjectEntity project = new ProjectEntity("Test project");
        project.setId(1L);
        project.setSupervisor(userForTask);

        TaskEntity task = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        task.setAssignee(userForTask);
        task.setProject(project);

        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(task);

        Set<ProjectEntity> projects = new HashSet<>();
        projects.add(project);

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        user1.setId(1L);
        user1.setTasks(tasks);
        user1.setProjects(projects);

        userServiceImpl.updateTasksDataAfterUserDeletion(user1);

        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }

    @Test
    public void testUpdateTasksDataAfterUserDeletionUserNotAvailable() {
        UserEntity userForTask = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        userForTask.setId(2L);

        ProjectEntity project = new ProjectEntity("Test project");
        project.setId(1L);
        project.setSupervisor(userForTask);

        TaskEntity task = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        task.setAssignee(userForTask);
        task.setProject(project);

        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(task);

        Set<ProjectEntity> projects = new HashSet<>();
        projects.add(project);

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "qwerty123");
        user1.setId(1L);
        user1.setTasks(tasks);
        user1.setProjects(projects);

        userServiceImpl.updateTasksDataAfterUserDeletion(user1);

        verify(taskRepository, never()).save(any(TaskEntity.class));
    }
}