package pl.asap.asapbe.controllers;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.asap.asapbe.entities.*;
import pl.asap.asapbe.services.TaskService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaskControllerTest {

    @Mock
    TaskService taskService;

    TaskController taskController;
    MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        taskController = new TaskController(taskService);
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    public void testGetAllTasksFromSpecificProject() throws Exception {
        TaskEntity task1 = new TaskEntity("Test title", "Test description", Status.OPEN, Priority.HIGH);
        task1.setId(1L);
        TaskEntity task2 = new TaskEntity("Test title2", "Test description2", Status.DONE, Priority.LOW);
        task2.setId(2L);

        List<TaskEntity> tasks = Arrays.asList(task1, task2);

        when(taskService.getAllTasksFromProject(anyString(), anyLong())).thenReturn(tasks);

        mockMvc.perform(get("/tasks")
                .header("token", "134123")
                .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test title")))
                .andExpect(jsonPath("$[0].description", is("Test description")))
                .andExpect(jsonPath("$[0].status", is("OPEN")))
                .andExpect(jsonPath("$[0].priority", is("HIGH")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Test title2")))
                .andExpect(jsonPath("$[1].description", is("Test description2")))
                .andExpect(jsonPath("$[1].status", is("DONE")))
                .andExpect(jsonPath("$[1].priority", is("LOW")));

        verify(taskService, times(1)).getAllTasksFromProject(anyString(), anyLong());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testGetTask() throws Exception {
        TaskEntity task1 = new TaskEntity("Test title", "Test description", Status.OPEN, Priority.HIGH);
        task1.setId(1L);

        when(taskService.getTaskById(anyString(), anyLong())).thenReturn(task1);

        mockMvc.perform(get("/tasks/task")
                .header("token", "134123")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test title")))
                .andExpect(jsonPath("$.description", is("Test description")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.priority", is("HIGH")));

        verify(taskService, times(1)).getTaskById(anyString(), anyLong());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testCreateTask() throws Exception {
        TaskEntity task1 = new TaskEntity("Test title", "Test description", Status.OPEN, Priority.HIGH);
        task1.setId(1L);

        Gson gson = new Gson();
        String jsonObj = gson.toJson(task1);

        when(taskService.performTaskCreation(anyString(), any(), anyLong())).thenReturn(task1);

        mockMvc.perform(post("/tasks")
                .header("token", "134123")
                .param("projectId", "1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonObj))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test title")))
                .andExpect(jsonPath("$.description", is("Test description")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.priority", is("HIGH")));

        verify(taskService, times(1)).performTaskCreation(anyString(), any(), anyLong());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testModifyTask() throws Exception {
        TaskEntity taskBody = new TaskEntity("Test title", "Test description", Status.OPEN, Priority.HIGH);//body for modification
        TaskEntity taskResult = new TaskEntity("Test title", "Test description", Status.OPEN, Priority.HIGH);//mocked task to return
        taskResult.setId(1L);

        Gson gson = new Gson();
        String jsonObj = gson.toJson(taskBody);

        when(taskService.performTaskModification(anyString(), any(), anyLong())).thenReturn(taskResult);

        mockMvc.perform(put("/tasks")
                .header("token", "134123")
                .param("taskId", "1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonObj))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test title")))
                .andExpect(jsonPath("$.description", is("Test description")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.priority", is("HIGH")));

        verify(taskService, times(1)).performTaskModification(anyString(), any(), anyLong());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testAssignToTask() throws Exception {
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        TaskEntity task1 = new TaskEntity("Test title", "Test description", Status.OPEN, Priority.HIGH);
        task1.setId(1L);
        task1.setAssignee(user1);

        when(taskService.performTaskAssignment(anyString(), anyLong(), anyLong())).thenReturn(task1);

        mockMvc.perform(put("/tasks/assign")
                .header("token", "134123")
                .param("taskId", "1")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.assignee").exists())
                .andExpect(jsonPath("$.assignee.id", is(1)))
                .andExpect(jsonPath("$.assignee.firstName", is("Jan")))
                .andExpect(jsonPath("$.assignee.lastName", is("Kowalski")))
                .andExpect(jsonPath("$.assignee.email", is("jan_kowalski@gmail.com")));

        verify(taskService, times(1)).performTaskAssignment(anyString(), anyLong(), anyLong());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testDeleteTask() throws Exception {
        mockMvc.perform(delete("/tasks")
                .header("token", "134123")
                .param("taskId", "1"))
                .andExpect(status().isOk());

        verify(taskService, times(1)).performTaskDeletion(anyString(), anyLong());
        verifyNoMoreInteractions(taskService);
    }
}