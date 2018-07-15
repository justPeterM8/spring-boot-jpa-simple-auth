package pl.asap.asapbe.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.asap.asapbe.entities.*;
import pl.asap.asapbe.exceptions.*;
import pl.asap.asapbe.repositories.ProjectRepository;
import pl.asap.asapbe.repositories.TaskRepository;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TaskServiceImplTest {

    @Mock
    ProjectServiceImpl projectServiceImpl;

    @Mock
    UserServiceImpl userServiceImpl;

    @Mock
    UserAuthDetailsServiceImpl userAuthDetailsServiceImpl;

    @Mock
    AuthServiceImpl authServiceImpl;

    @Mock
    ProjectRepository projectRepository;

    @Mock
    TaskRepository taskRepository;

    TaskServiceImpl taskServiceImpl;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        taskServiceImpl = new TaskServiceImpl(projectServiceImpl, userServiceImpl, userAuthDetailsServiceImpl, authServiceImpl, projectRepository, taskRepository);
    }

    @Test
    public void testGetAllTasksFromProjectSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);

        Set<UserEntity> users = new HashSet<>();
        users.add(user1);

        TaskEntity task = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);

        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(task);

        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        projectEntity1.setUsers(users);
        projectEntity1.setTasks(tasks);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(projectServiceImpl.getProjectFromDbById(anyLong())).thenReturn(projectEntity1);
        when(projectServiceImpl.isUserPartOfProject(any(), any())).thenReturn(true);

        List<TaskEntity> tasksReturned = taskServiceImpl.getAllTasksFromProject("1231-123-123", 1L);

        assertNotNull(tasksReturned);
        assertEquals(new ArrayList<>(tasks), tasksReturned);

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectServiceImpl, times(1)).isUserPartOfProject(any(), any());

    }

    @Test(expected = InsufficientPermissionException.class)
    public void testGetAllTasksFromProjectFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);

        Set<UserEntity> users = new HashSet<>();
        users.add(user1);

        TaskEntity task = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);

        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(task);

        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        projectEntity1.setUsers(users);
        projectEntity1.setTasks(tasks);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(projectServiceImpl.getProjectFromDbById(anyLong())).thenReturn(projectEntity1);
        when(projectServiceImpl.isUserPartOfProject(any(), any())).thenReturn(false);

        List<TaskEntity> tasksReturned = taskServiceImpl.getAllTasksFromProject("1231-123-123", 1L);
        //Should throw exception related to state where user performing get action has insufficient permission

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectServiceImpl, times(1)).isUserPartOfProject(any(), any());
    }

    @Test
    public void testGetTaskByIdSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");
        TaskEntity task = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        Optional<TaskEntity> taskEntityOptional = Optional.of(task);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(taskRepository.findById(anyLong())).thenReturn(taskEntityOptional);

        TaskEntity taskReturned = taskServiceImpl.getTaskById("1231-123-123", 1L);

        assertNotNull(taskReturned);
        assertEquals(task, taskReturned);

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).findAll();
    }

    @Test(expected = UserAuthenticationException.class)
    public void testGetTaskByIdFailure() {
        TaskEntity task = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        Optional<TaskEntity> taskEntityOptional = Optional.of(task);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(null);
        when(taskRepository.findById(anyLong())).thenReturn(taskEntityOptional);

        TaskEntity taskReturned = taskServiceImpl.getTaskById("1231-123-123", 1L);
        //This should throw an exception related to user authentication error

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).findAll();
    }

    @Test
    public void testPerformTaskCreationSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);

        TaskEntity task = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);

        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(task);

        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        projectEntity1.setTasks(new HashSet<>());

        ProjectEntity projectEntityUpdated = new ProjectEntity("Test project");
        projectEntityUpdated.setId(1L);
        projectEntityUpdated.setTasks(tasks);


        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(userServiceImpl.getUserEntityFromUserAuthDetailsEntity(any())).thenReturn(user1);
        when(projectServiceImpl.getProjectFromDbById(anyLong())).thenReturn(projectEntity1);
        when(projectRepository.save(any())).thenReturn(projectEntityUpdated);
        when(taskRepository.save(any())).thenReturn(task);

        TaskEntity taskReturned = taskServiceImpl.performTaskCreation("1231-123-123", task, 1L);
        assertNotNull(taskReturned);
        assertEquals(task, taskReturned);
        assertEquals(projectEntityUpdated, taskReturned.getProject());

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }

    @Test(expected = TaskAlreadyExistsInProjectException.class)
    public void testPerformTaskCreationFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);

        TaskEntity task = new TaskEntity("Test task", "Description for test task 1", Status.OPEN, Priority.HIGH);
        task.setId(1L);

        TaskEntity taskCreated = new TaskEntity("Test task", "Description for test task 2", Status.DONE, Priority.NORMAL);
        task.setId(2L);

        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(task);

        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        projectEntity1.setTasks(tasks);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(userServiceImpl.getUserEntityFromUserAuthDetailsEntity(any())).thenReturn(user1);
        when(projectServiceImpl.getProjectFromDbById(anyLong())).thenReturn(projectEntity1);

        taskServiceImpl.performTaskCreation("1231-123-123", taskCreated, 1L);
        //should throw exception related to state in which task that is created already exists (another task with the same title exists)

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(taskRepository, never()).findAll();
    }

    @Test
    public void testPerformTaskModificationSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");

        ProjectEntity projectEntityForTask = new ProjectEntity("Test project");
        projectEntityForTask.setId(1L);

        TaskEntity taskBeforeChange = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        taskBeforeChange.setId(1L);
        taskBeforeChange.setProject(projectEntityForTask);

        Optional<TaskEntity> taskEntityOptional = Optional.of(taskBeforeChange);

        TaskEntity taskAfterChange = new TaskEntity("Test task", "New description after change", Status.OPEN, Priority.HIGH);
        taskAfterChange.setId(1L);

        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(taskBeforeChange);

        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        projectEntity1.setTasks(tasks);


        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(taskRepository.findById(anyLong())).thenReturn(taskEntityOptional);
        when(projectServiceImpl.getProjectFromDbById(anyLong())).thenReturn(projectEntity1);
        when(taskRepository.save(any())).thenReturn(taskAfterChange);

        TaskEntity taskReturned = taskServiceImpl.performTaskModification("1231-123-123", taskAfterChange, 1L);

        assertNotNull(taskReturned);
        assertEquals(taskAfterChange, taskReturned);

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).findAll();
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }

    @Test(expected = TaskAlreadyExistsInProjectException.class)
    public void testPerformTaskModificationFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");

        ProjectEntity projectEntityForTask = new ProjectEntity("Test project");
        projectEntityForTask.setId(1L);

        TaskEntity taskBeforeChange = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        taskBeforeChange.setId(1L);
        taskBeforeChange.setProject(projectEntityForTask);

        Optional<TaskEntity> taskEntityOptional = Optional.of(taskBeforeChange);

        TaskEntity taskAfterChange = new TaskEntity("Test task", "New description after change", Status.OPEN, Priority.HIGH);
        taskAfterChange.setId(1L);

        TaskEntity taskInProjectWithDifferentIdButDuplicatedTitle = new TaskEntity("Test task", "New description after change", Status.OPEN, Priority.HIGH);
        taskInProjectWithDifferentIdButDuplicatedTitle.setId(2L);

        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(taskBeforeChange);
        tasks.add(taskInProjectWithDifferentIdButDuplicatedTitle);

        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        projectEntity1.setTasks(tasks);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(taskRepository.findById(anyLong())).thenReturn(taskEntityOptional);
        when(projectServiceImpl.getProjectFromDbById(anyLong())).thenReturn(projectEntity1);
        when(taskRepository.save(any())).thenReturn(taskAfterChange);

        TaskEntity taskReturned = taskServiceImpl.performTaskModification("1231-123-123", taskAfterChange, 1L);
        //should throw exception related to state in which task with different id, but the same title as modified version of another task exists

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).findAll();
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }

    @Test
    public void testPerformTaskDeletionSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);

        ProjectEntity projectEntityForTask = new ProjectEntity("Test project");
        projectEntityForTask.setId(1L);
        projectEntityForTask.setSupervisor(user1);

        TaskEntity taskBeforeChange = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        taskBeforeChange.setId(1L);
        taskBeforeChange.setProject(projectEntityForTask);

        Optional<TaskEntity> taskEntityOptional = Optional.of(taskBeforeChange);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(taskRepository.findById(anyLong())).thenReturn(taskEntityOptional);

        taskServiceImpl.performTaskDeletion("1231-123-123", 1L);

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, times(1)).delete(any(TaskEntity.class));
    }

    @Test(expected = InsufficientPermissionException.class)
    public void testPerformTaskDeletionFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);

        ProjectEntity projectEntityForTask = new ProjectEntity("Test project");
        projectEntityForTask.setId(1L);
        projectEntityForTask.setSupervisor(user1);

        TaskEntity taskBeforeChange = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        taskBeforeChange.setId(1L);
        taskBeforeChange.setProject(projectEntityForTask);

        Optional<TaskEntity> taskEntityOptional = Optional.of(taskBeforeChange);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(taskRepository.findById(anyLong())).thenReturn(taskEntityOptional);

        taskServiceImpl.performTaskDeletion("1231-123-123", 1L);
        //should throw exception related to state where user has InsuficientPermissions

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).delete(any(TaskEntity.class));
    }

    @Test
    public void testPerformTaskAssignmentSuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);

        Set<UserEntity> users = new HashSet<>();
        users.add(user1);

        ProjectEntity projectEntityForTask = new ProjectEntity("Test project");
        projectEntityForTask.setId(1L);
        projectEntityForTask.setUsers(users);

        TaskEntity taskBeforeChange = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        taskBeforeChange.setId(1L);
        taskBeforeChange.setProject(projectEntityForTask);

        TaskEntity taskExpected = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        taskExpected.setId(1L);
        taskExpected.setProject(projectEntityForTask);
        taskExpected.setAssignee(user1);

        Optional<TaskEntity> taskEntityOptional = Optional.of(taskBeforeChange);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(taskRepository.findById(anyLong())).thenReturn(taskEntityOptional);
        when(userServiceImpl.getUserFromDbById(anyLong())).thenReturn(user1);
        when(userAuthDetailsServiceImpl.getUserAuthDetailsFromUserEntity(user1)).thenReturn(userAuthDetailsEntity);
        when(projectServiceImpl.isUserPartOfProject(userAuthDetailsEntity, projectEntityForTask)).thenReturn(true);

        TaskEntity taskReturned = taskServiceImpl.performTaskAssignment("1231-123-123", 1L, 1L);

        assertNotNull(taskReturned);
        assertEquals(taskExpected, taskReturned);

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).findAll();
        verify(projectServiceImpl, times(1)).updateProjectWithModifiedTaskData(any(), anyString(), any());
    }

    @Test(expected = UserNotPartOfProjectException.class)
    public void testPerformTaskAssignmentFailure() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(2L, "1231-123-123");

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);

        ProjectEntity projectEntityForTask = new ProjectEntity("Test project");
        projectEntityForTask.setId(1L);
        projectEntityForTask.setUsers(new HashSet<>());

        TaskEntity taskBeforeChange = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        taskBeforeChange.setId(1L);
        taskBeforeChange.setProject(projectEntityForTask);

        TaskEntity taskExpected = new TaskEntity("Test task", "Description for test task", Status.OPEN, Priority.HIGH);
        taskExpected.setId(1L);
        taskExpected.setProject(projectEntityForTask);
        taskExpected.setAssignee(user1);

        Optional<TaskEntity> taskEntityOptional = Optional.of(taskBeforeChange);

        when(authServiceImpl.authenticateUserByToken(anyString())).thenReturn(userAuthDetailsEntity);
        when(taskRepository.findById(anyLong())).thenReturn(taskEntityOptional);
        when(userServiceImpl.getUserFromDbById(anyLong())).thenReturn(user1);
        when(userAuthDetailsServiceImpl.getUserAuthDetailsFromUserEntity(user1)).thenReturn(userAuthDetailsEntity);
        when(projectServiceImpl.isUserPartOfProject(userAuthDetailsEntity, projectEntityForTask)).thenReturn(false);

        TaskEntity taskReturned = taskServiceImpl.performTaskAssignment("1231-123-123", 1L, 1L);
        //should throw exception related to state in which user is not part of project they want to modify

        verify(authServiceImpl, times(1)).authenticateUserByToken(anyString());
        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).findAll();
        verify(projectServiceImpl, times(1)).updateProjectWithModifiedTaskData(any(), anyString(), any());
    }

    @Test
    public void testIsTaskAlreadyCreatedInProjectPositive() {
        TaskEntity task1 = new TaskEntity("Test task 1", "Description for test task 1", Status.OPEN, Priority.HIGH);
        task1.setId(2L);
        TaskEntity task2 = new TaskEntity("Test task 2", "Description for test task 2", Status.IN_TESTS, Priority.HIGH);
        task2.setId(2L);
        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(task1);
        tasks.add(task2);

        TaskEntity taskDuplicatedTitle = new TaskEntity("Test task 2", "Description for test task 3", Status.IN_TESTS, Priority.HIGH);
        task2.setId(3L);

        Boolean isAlreadyInProject = taskServiceImpl.isTaskAlreadyCreatedInProject(tasks, taskDuplicatedTitle);
        assertNotNull(isAlreadyInProject);
        assertEquals(true, isAlreadyInProject);
    }

    @Test
    public void testIsTaskAlreadyCreatedInProjectNegative() {
        TaskEntity task1 = new TaskEntity("Test task 1", "Description for test task 1", Status.OPEN, Priority.HIGH);
        task1.setId(1L);
        TaskEntity task2 = new TaskEntity("Test task 2", "Description for test task 2", Status.IN_TESTS, Priority.HIGH);
        task2.setId(2L);
        Set<TaskEntity> tasks = new HashSet<>();
        tasks.add(task1);
        tasks.add(task2);

        TaskEntity taskNotDuplicatedTitle = new TaskEntity("Test task 3", "Description for test task 3", Status.IN_TESTS, Priority.HIGH);
        task2.setId(3L);

        Boolean isAlreadyInProject = taskServiceImpl.isTaskAlreadyCreatedInProject(tasks, taskNotDuplicatedTitle);
        assertNotNull(isAlreadyInProject);
        assertEquals(false, isAlreadyInProject);
    }

    @Test
    public void testGetTaskFromDbByIdSuccess() {
        TaskEntity task1 = new TaskEntity("Test task 1", "Description for test task 1", Status.OPEN, Priority.HIGH);
        task1.setId(1L);
        Optional<TaskEntity> taskEntityOptional = Optional.of(task1);

        when(taskRepository.findById(anyLong())).thenReturn(taskEntityOptional);

        TaskEntity taskReturned = taskServiceImpl.getTaskFromDbById(1L);

        assertNotNull(taskReturned);
        assertEquals(task1, taskReturned);

        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).findAll();
    }

    @Test(expected = NoSuchTaskException.class)
    public void testGetTaskFromDbByIdFailure() {
        Optional<TaskEntity> taskEntityOptional = Optional.empty();

        when(taskRepository.findById(anyLong())).thenReturn(taskEntityOptional);

        TaskEntity taskReturned = taskServiceImpl.getTaskFromDbById(1L);
        //should throw exception related to state in which there is no such task in database
    }
}
