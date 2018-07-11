package pl.asap.asapbe.services;

import pl.asap.asapbe.entities.UserAuthDetailsEntity;

public interface AuthService {

    UserAuthDetailsEntity authenticateUserByToken(String authToken);

    String generateToken();

    String encryptPassword(String password);
}