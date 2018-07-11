package pl.asap.asapbe.services;

import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.response_model.UserDetails;

import java.util.List;

public interface UserService {

    List<UserEntity> getListOfAllUsers(String authToken);

    UserAuthDetailsEntity performUserLogin(String email, String password);

    UserAuthDetailsEntity performUserRegistration(String firstName, String lastName, String email, String password);

    void performUserDeletion(String authToken);

    UserEntity getUserEntityFromUserAuthDetailsEntity(UserAuthDetailsEntity userAuthDetailsEntity);

    void performUserPasswordChangeOperation(String authToken, String oldPassword, String newPassword);

    UserEntity performUserModification(String authToken, UserEntity changedUser);

    UserDetails getUserDetails(String authToken);

    UserEntity getUserFromDbById(Long id);

    void updateTasksDataAfterUserDeletion(UserEntity userEntity);
}