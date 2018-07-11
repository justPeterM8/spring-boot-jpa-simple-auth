package pl.asap.asapbe.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.repositories.UserAuthDetailsRepository;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthServiceImplTest {

    @Mock
    UserAuthDetailsRepository userAuthDetailsRepository;

    AuthServiceImpl authServiceImpl;


    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        authServiceImpl = new AuthServiceImpl(userAuthDetailsRepository, userAuthDetailsService);
    }

    @Test
    public void testAuthenticateUserByToken() {
        UserAuthDetailsEntity userAuthDetailsEntity = new UserAuthDetailsEntity(1L, "1231-123-123");
        Optional<UserAuthDetailsEntity> userOptional = Optional.of(userAuthDetailsEntity);

        when(userAuthDetailsRepository.findByToken(anyString())).thenReturn(userOptional);

        UserAuthDetailsEntity returnedDetails = authServiceImpl.authenticateUserByToken("1231-123-123");
        assertNotNull("Null details returned", returnedDetails);
        verify(userAuthDetailsRepository, times(1)).findByToken(anyString());
        verify(userAuthDetailsRepository, never()).findAll();
    }

    @Test
    public void testGenerateToken() {
        assertNotNull("Null token returned", authServiceImpl.generateToken());
        for(int i=0; i<100; i++)//testing randomness
            assertNotEquals(authServiceImpl.generateToken(), authServiceImpl.generateToken());
    }

    @Test
    public void testEncryptPassword() {
        String passwordRaw = "test_password";
        String passwordEncryptedExpected = "16EC1EBB01FE02DED9B7D5447D3DFC65";
        assertNotNull("Null token returned", authServiceImpl.encryptPassword(passwordRaw));
        assertEquals(passwordEncryptedExpected, authServiceImpl.encryptPassword(passwordRaw));
    }
}