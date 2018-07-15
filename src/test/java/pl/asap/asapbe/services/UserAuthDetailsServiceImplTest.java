package pl.asap.asapbe.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.exceptions.UserNotFoundException;
import pl.asap.asapbe.repositories.UserAuthDetailsRepository;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class UserAuthDetailsServiceImplTest {

    @Mock
    UserAuthDetailsRepository userAuthDetailsRepository;

    UserAuthDetailsServiceImpl userAuthDetailsServiceImpl;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userAuthDetailsServiceImpl = new UserAuthDetailsServiceImpl(userAuthDetailsRepository);
    }

    @Test
    public void testGetUserAuthDetailsFromUserEntitySuccess() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");
        Optional<UserAuthDetailsEntity> userAuthDetailsEntityOptional = Optional.of(userAuthDetailsEntity);

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);

        when(userAuthDetailsRepository.findByUserId(anyLong())).thenReturn(userAuthDetailsEntityOptional);

        UserAuthDetailsEntity authDetailsEntityReturned = userAuthDetailsServiceImpl.getUserAuthDetailsFromUserEntity(user1);
        assertNotNull(authDetailsEntityReturned);
        assertEquals(userAuthDetailsEntity, authDetailsEntityReturned);

        verify(userAuthDetailsRepository, times(1)).findByUserId(anyLong());
        verify(userAuthDetailsRepository, never()).findAll();
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetUserAuthDetailsFromUserEntityFailure() {
        Optional<UserAuthDetailsEntity> userAuthDetailsEntityOptional = Optional.empty();

        UserEntity user1 = new UserEntity("Jan", "Kowalski", "jan_kowalski@gmail.com", "pass");
        user1.setId(1L);

        when(userAuthDetailsRepository.findByUserId(anyLong())).thenReturn(userAuthDetailsEntityOptional);

        UserAuthDetailsEntity authDetailsEntityReturned = userAuthDetailsServiceImpl.getUserAuthDetailsFromUserEntity(user1);
        //should throw exception related to state in which there is no user connected to id given

        verify(userAuthDetailsRepository, times(1)).findByUserId(anyLong());
        verify(userAuthDetailsRepository, never()).findAll();
    }
}