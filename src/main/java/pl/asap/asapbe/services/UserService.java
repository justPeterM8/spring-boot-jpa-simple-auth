package pl.asap.asapbe.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.exceptions.*;
import pl.asap.asapbe.response_model.UserDetails;

import java.util.List;
import java.util.Optional;

@Service
public class UserService extends BaseService {
    private UserAuthDetailsService userAuthDetailsService;

    @Autowired
    public UserService(UserAuthDetailsService userAuthDetailsService) {
        this.userAuthDetailsService = userAuthDetailsService;
    }

    public List<UserEntity> getListOfAllUsers(String authToken) {
        if (authService.authenticateUserByToken(authToken) != null) //user authenticated
            return userRepository.findAll();
        else
            throw new UserAuthenticationException();
    }

    public UserAuthDetailsEntity performUserLogin(String email, String password) {
        UserEntity userEntity = userRepository.findByEmailAndPassword(email, authService.encryptPassword(password));
        if (userEntity != null) {
            return userAuthDetailsService.getUserAuthDetailsFromUserEntity(userEntity);
        } else
            throw new UserAuthenticationException();
    }

    public UserAuthDetailsEntity performUserRegistration(String firstName, String lastName, String email, String password) {
        UserEntity searchedUser = userRepository.findByEmail(email);
        if (searchedUser == null) {//user with such email not registered, performing creation
            UserEntity userEntity = new UserEntity(firstName, lastName, email, authService.encryptPassword(password));
            String authToken = authService.generateToken();
            UserAuthDetailsEntity userAuthDetailsToSave = new UserAuthDetailsEntity(authToken);
            UserEntity savedUser = userRepository.save(userEntity);
            userAuthDetailsToSave.setUserId(savedUser.getId());
            return userAuthDetailsRepository.save(userAuthDetailsToSave);
        } else {
            if (searchedUser.equals(new UserEntity(firstName, lastName, email, authService.encryptPassword(password))))
                return performUserLogin(email, password);
            else
                throw new EmailAlreadyExistsInDatabaseException();
        }
    }

    public void performUserDeletion(String authToken) {
        UserAuthDetailsEntity userAuthDetailsEntity = authService.authenticateUserByToken(authToken);
        UserEntity userToDelete = getUserEntityFromUserAuthDetailsEntity(userAuthDetailsEntity); //no way there is null, because of above if statement
        updateTasksDataAfterUserDeletion(userToDelete);
        userRepository.delete(userToDelete);
        userAuthDetailsRepository.delete(userAuthDetailsEntity);
    }

    UserEntity getUserEntityFromUserAuthDetailsEntity(UserAuthDetailsEntity userAuthDetailsEntity) {
        Optional<UserEntity> userEntity = userRepository.findById(userAuthDetailsEntity.getUserId());
        if (userEntity.isPresent()) {//user exists in UserEntity table
            return userEntity.get();
        } else {
            userAuthDetailsRepository.delete(userAuthDetailsEntity);// deleting auth details since there is no corresponding user in UserEntity table (very unlikely to happen)
            throw new UserNotFoundException();
        }
    }

    public void performUserPasswordChangeOperation(String authToken, String oldPassword, String newPassword) {
        UserAuthDetailsEntity requestingUser = authService.authenticateUserByToken(authToken);
        UserEntity userEntity = getUserEntityFromUserAuthDetailsEntity(requestingUser);
        if (userEntity.getPassword().equals(authService.encryptPassword(oldPassword))) {//old password matching
            userEntity.setPassword(authService.encryptPassword(newPassword));
            userRepository.save(userEntity);
        } else
            throw new UserAuthenticationException();
    }

    public UserEntity performUserModification(String authToken, UserEntity changedUser) {
        UserAuthDetailsEntity requestingUser = authService.authenticateUserByToken(authToken);
        UserEntity userToBeChanged = getUserEntityFromUserAuthDetailsEntity(requestingUser);
        userToBeChanged.setFirstName(changedUser.getFirstName());
        userToBeChanged.setLastName(changedUser.getLastName());
        userToBeChanged.setEmail(changedUser.getEmail());
        return userRepository.save(userToBeChanged);
    }

    public UserDetails getUserDetails(String authToken) {
        UserAuthDetailsEntity requestingUser = authService.authenticateUserByToken(authToken);
        UserEntity userEntity = getUserEntityFromUserAuthDetailsEntity(requestingUser);
        return new UserDetails(userEntity.getFirstName(), userEntity.getLastName());
    }

    UserEntity getUserFromDbById(Long id) {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isPresent())
            return user.get();
        else
            throw new UserNotFoundException();
    }

    private void updateTasksDataAfterUserDeletion(UserEntity userEntity) {
        userEntity.getTasks()
                .stream()
                .filter(task -> task.getAssignee().getId().equals(userEntity.getId()))
                .forEach(task -> {
                    UserEntity supervisor = task.getProject().getSupervisor();
                    task.setAssignee(supervisor);//assigning supervisor of project task is in, to avoid unassigned state
                    taskRepository.save(task);
                });
    }
}

