package pl.asap.asapbe.controllers;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.asap.asapbe.entities.ProjectEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.services.ProjectServiceImpl;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ProjectControllerTest {

    @Mock
    ProjectServiceImpl projectServiceImpl;

    ProjectController projectController;
    MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        projectController = new ProjectController(projectServiceImpl);
        mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();

    }

    @Test
    public void testGetAllProjects() throws Exception {
        ProjectEntity projectEntity1 = new ProjectEntity("Test project");
        projectEntity1.setId(1L);
        ProjectEntity projectEntity2 = new ProjectEntity("Test project2");
        projectEntity2.setId(2L);
        List<ProjectEntity> projects = Arrays.asList(projectEntity1, projectEntity2);

        when(projectServiceImpl.getListOfAllProjects(anyString())).thenReturn(projects);

        mockMvc.perform(get("/projects")
                .header("token", "134123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test project")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Test project2")));

        verify(projectServiceImpl, times(1)).getListOfAllProjects(anyString());
        verifyNoMoreInteractions(projectServiceImpl);
    }

    @Test
    public void testGetAllUsersInProject() throws Exception {
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        UserEntity user2 = new UserEntity("Marek", "Kostrzewa", "marek_kostrzewa@gmail.com", "pass2123");
        user2.setId(2L);
        List<UserEntity> users = Arrays.asList(user1, user2);


        when(projectServiceImpl.getAllUsersFromSpecificProject(anyString(), anyLong())).thenReturn(users);
        mockMvc.perform(get("/projects/users")
                .param("projectId", "1")
                .header("token", "134123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].firstName", is("Jan")))
                .andExpect(jsonPath("$[0].lastName", is("Kowalski")))
                .andExpect(jsonPath("$[0].email", is("jan_kowalski@gmail.com")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].firstName", is("Marek")))
                .andExpect(jsonPath("$[1].lastName", is("Kostrzewa")))
                .andExpect(jsonPath("$[1].email", is("marek_kostrzewa@gmail.com")));

        verify(projectServiceImpl, times(1)).getAllUsersFromSpecificProject(anyString(), anyLong());
        verifyNoMoreInteractions(projectServiceImpl);
    }

    @Test
    public void testCreateProject() throws Exception {
        ProjectEntity projectEntity = new ProjectEntity("Test project");
        projectEntity.setId(1L);

        Gson gson = new Gson();
        String objJson = gson.toJson(projectEntity);

        when(projectServiceImpl.performProjectCreation(anyString(), any())).thenReturn(projectEntity);

        mockMvc.perform(post("/projects")
                .header("token", "134123")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test project")));

        verify(projectServiceImpl, times(1)).performProjectCreation(anyString(), any());
        verifyNoMoreInteractions(projectServiceImpl);
    }

    @Test
    public void testModifyProject() throws Exception {
        ProjectEntity projectEntityBody = new ProjectEntity("Test project");//body for change in service

        ProjectEntity projectEntityResult = new ProjectEntity("Test project");//object returned after change
        projectEntityResult.setId(1L);

        Gson gson = new Gson();
        String objJson = gson.toJson(projectEntityBody);

        when(projectServiceImpl.performProjectModification(anyString(), anyLong(), any())).thenReturn(projectEntityResult);

        mockMvc.perform(put("/projects")
                .header("token", "134123")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test project")));

        verify(projectServiceImpl, times(1)).performProjectModification(anyString(), anyLong(), any());
        verifyNoMoreInteractions(projectServiceImpl);
    }

    @Test
    public void testAddUserToProject() throws Exception {
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        UserEntity user2 = new UserEntity("Marek", "Kostrzewa", "marek_kostrzewa@gmail.com", "pass2123");
        user2.setId(2L);

        List<UserEntity> usersInProject = Arrays.asList(user1, user2);
        when(projectServiceImpl.performAddingUserToProjectOperation(anyString(), anyLong(), anyLong())).thenReturn(usersInProject);

        mockMvc.perform(put("/projects/addUser")
                .header("token", "134123")
                .param("projectId", "1")
                .param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("Jan")))
                .andExpect(jsonPath("$[0].lastName", is("Kowalski")))
                .andExpect(jsonPath("$[0].email", is("jan_kowalski@gmail.com")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].firstName", is("Marek")))
                .andExpect(jsonPath("$[1].lastName", is("Kostrzewa")))
                .andExpect(jsonPath("$[1].email", is("marek_kostrzewa@gmail.com")));

        verify(projectServiceImpl, times(1)).performAddingUserToProjectOperation(anyString(), anyLong(), anyLong());
        verifyNoMoreInteractions(projectServiceImpl);
    }

    @Test
    public void testDeleteUserFromProject() throws Exception {
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        UserEntity user2 = new UserEntity("Marek", "Kostrzewa", "marek_kostrzewa@gmail.com", "pass2123");
        user2.setId(2L);

        List<UserEntity> usersInProject = Arrays.asList(user1, user2);
        when(projectServiceImpl.performDeletingUserFromProjectOperation(anyString(), anyLong(), anyLong())).thenReturn(usersInProject);

        mockMvc.perform(put("/projects/deleteUser")
                .header("token", "134123")
                .param("projectId", "1")
                .param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("Jan")))
                .andExpect(jsonPath("$[0].lastName", is("Kowalski")))
                .andExpect(jsonPath("$[0].email", is("jan_kowalski@gmail.com")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].firstName", is("Marek")))
                .andExpect(jsonPath("$[1].lastName", is("Kostrzewa")))
                .andExpect(jsonPath("$[1].email", is("marek_kostrzewa@gmail.com")));

        verify(projectServiceImpl, times(1)).performDeletingUserFromProjectOperation(anyString(), anyLong(), anyLong());
        verifyNoMoreInteractions(projectServiceImpl);
    }

    @Test
    public void testDeleteProject() throws Exception {
        mockMvc.perform(delete("/projects")
                .header("token", "134123")
                .param("id", "1"))
                .andExpect(status().isOk());

        verify(projectServiceImpl, times(1)).performProjectDeletion(anyString(), anyLong());
        verifyNoMoreInteractions(projectServiceImpl);
    }
}

//        MvcResult mvcResult =
//        System.out.println(mvcResult.getResponse().getContentAsString());
//        .andReturn()