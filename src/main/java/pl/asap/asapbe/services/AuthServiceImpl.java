package pl.asap.asapbe.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.exceptions.UserAuthenticationException;
import pl.asap.asapbe.repositories.UserAuthDetailsRepository;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserAuthDetailsRepository userAuthDetailsRepository;

    @Autowired
    public AuthServiceImpl(UserAuthDetailsRepository userAuthDetailsRepository) {
        this.userAuthDetailsRepository = userAuthDetailsRepository;
    }

     public UserAuthDetailsEntity authenticateUserByToken(String authToken) {
        Optional<UserAuthDetailsEntity> userDetailsOptional = userAuthDetailsRepository.findByToken(authToken);
        if (!userDetailsOptional.isPresent())
            throw new UserAuthenticationException();
        return userDetailsOptional.get();
    }

    public String generateToken() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    public String encryptPassword(String password) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new HexBinaryAdapter().marshal(md5.digest(password.getBytes()));
    }
}
