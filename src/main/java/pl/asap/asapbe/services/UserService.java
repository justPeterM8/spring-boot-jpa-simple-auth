package pl.asap.asapbe.services;

import org.springframework.stereotype.Service;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.exceptions.*;
import pl.asap.asapbe.response_model.UserDetails;

import java.util.List;
import java.util.Optional;

@Service
public class UserService extends BaseService {

    public List<UserEntity> getListOfAllUsers(String authToken) {
        if (authService.authenticateUserByToken(authToken) != null) //user authenticated
            return userRepository.findAll();
        else
            throw new UserAuthenticationException();
    }

    public UserAuthDetailsEntity performUserLogin(String email, String password) {
        UserEntity userEntity = userRepository.findByEmailAndPassword(email, authService.encryptPassword(password));
        if (userEntity != null) {
            return userAuthDetailsRepository.findByUserId(userEntity.getId());
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
        updateProjectsDataAfterUserDeletion(userToDelete);
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
        //basic authentication, because there is no way other user will change password of another user
        UserAuthDetailsEntity requestingUser = authService.authenticateUserByToken(authToken);
        UserEntity userEntity = getUserEntityFromUserAuthDetailsEntity(requestingUser);
        if (userEntity.getPassword().equals(authService.encryptPassword(oldPassword))) {//old password matching
            userEntity.setPassword(authService.encryptPassword(newPassword));
            userRepository.save(userEntity);
        } else
            throw new UserAuthenticationException();
    }

    public void performUserModification(String authToken, UserEntity changedUser) {
        //basic authentication, only user will have opportunity to change their accrount details
        UserAuthDetailsEntity requestingUser = authService.authenticateUserByToken(authToken);
        UserEntity userToBeChanged = getUserEntityFromUserAuthDetailsEntity(requestingUser);
        userToBeChanged.setFirstName(changedUser.getFirstName());
        userToBeChanged.setLastName(changedUser.getLastName());
        userToBeChanged.setEmail(changedUser.getEmail());
        userRepository.save(userToBeChanged);
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

    private void updateProjectsDataAfterUserDeletion(UserEntity userEntity) {
        userEntity
                .getProjects()
                .forEach(projectEntity -> {
                    if (projectEntity.getSupervisor().getId().equals(userEntity.getId())) {//deleting supervisor's account affects deleting whole project and it's tasks
                        projectEntity.getTasks()
                                .forEach(taskRepository::delete);
                        projectRepository.delete(projectEntity);
                    } else {
                        projectEntity.getUsers()
                                .stream()
                                .filter(user -> user.getId().equals(userEntity.getId()))
                                .forEach(user -> user = null);
                        projectRepository.save(projectEntity);
                    }
                });
    }

    private void updateTasksDataAfterUserDeletion(UserEntity userEntity) {
        userEntity.getTasks()
                .stream()
                .filter(task -> task.getAssignee().getId().equals(userEntity.getId()))
                .forEach(task -> task.setAssignee(null));
    }
}

