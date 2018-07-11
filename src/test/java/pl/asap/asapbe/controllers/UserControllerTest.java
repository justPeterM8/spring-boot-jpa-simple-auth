package pl.asap.asapbe.controllers;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.response_model.UserDetails;
import pl.asap.asapbe.services.UserServiceImpl;

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

public class UserControllerTest {

    @Mock
    UserServiceImpl userServiceImpl;

    UserController userController;
    MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userController = new UserController(userServiceImpl);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testGetAllUsers() throws Exception {
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);
        UserEntity user2 = new UserEntity("Marek", "Kostrzewa", "marek_kostrzewa@gmail.com", "pass2123");
        user2.setId(2L);
        List<UserEntity> users = Arrays.asList(user1, user2);

        when(userServiceImpl.getListOfAllUsers(anyString())).thenReturn(users);

        mockMvc.perform(get("/users")
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

        verify(userServiceImpl, times(1)).getListOfAllUsers(anyString());
        verifyNoMoreInteractions(userServiceImpl);
    }

    @Test
    public void testGetUserDetails() throws Exception {
        UserDetails userDetails = new UserDetails("Jan", "Kowalski");

        when(userServiceImpl.getUserDetails(anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/users/details")
                .header("token", "134123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.firstName", is("Jan")))
                .andExpect(jsonPath("$.lastName", is("Kowalski")));

        verify(userServiceImpl, times(1)).getUserDetails(anyString());
        verifyNoMoreInteractions(userServiceImpl);
    }

    @Test
    public void testLoginUser() throws Exception {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        when(userServiceImpl.performUserLogin(anyString(), anyString())).thenReturn(userAuthDetailsEntity);

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("token", "134123")
                .param("email", "test@gmail.com")
                .param("password", "test_password"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.token", is("1231-123-123")));

        verify(userServiceImpl, times(1)).performUserLogin(anyString(), anyString());
        verifyNoMoreInteractions(userServiceImpl);
    }

    @Test
    public void testChangeUserPassword() throws Exception {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        mockMvc.perform(post("/users/password")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("token", "134123")
                .param("oldPassword", "test_old_passsword")
                .param("newPassword", "test_new_passsword"))
                .andExpect(status().isOk());

        verify(userServiceImpl, times(1)).performUserPasswordChangeOperation(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(userServiceImpl);
    }

    @Test
    public void testCreateUser() throws Exception {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");

        when(userServiceImpl.performUserRegistration(anyString(), anyString(), anyString(), anyString())).thenReturn(userAuthDetailsEntity);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("token", "134123")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "test@gmail.com")
                .param("password", "test_password"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.token", is("1231-123-123")));

        verify(userServiceImpl, times(1)).performUserRegistration(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(userServiceImpl);
    }

    @Test
    public void testModifyUser1() throws Exception {
        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");//for body and response
        user1.setId(1L);

        Gson gson = new Gson();
        String jsonObj = gson.toJson(user1);

        when(userServiceImpl.performUserModification(anyString(), any())).thenReturn(user1);

        mockMvc.perform(put("/users")
                .header("token", "134123")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonObj))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("Jan")))
                .andExpect(jsonPath("$.lastName", is("Kowalski")))
                .andExpect(jsonPath("$.email", is("jan_kowalski@gmail.com")));

        verify(userServiceImpl, times(1)).performUserModification(anyString(), any());
        verifyNoMoreInteractions(userServiceImpl);
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/users")
                .header("token", "134123"))
                .andExpect(status().isOk());

        verify(userServiceImpl, times(1)).performUserDeletion(anyString());
        verifyNoMoreInteractions(userServiceImpl);
    }
}