package pl.asap.asapbe.services;

import pl.asap.asapbe.entities.ProjectEntity;
import pl.asap.asapbe.entities.TaskEntity;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.exceptions.UserAuthenticationException;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthService {

    UserAuthDetailsEntity authenticateUserByToken(String authToken);

    String generateToken();

    String encryptPassword(String password);
}