package pl.asap.asapbe.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.exceptions.*;
import pl.asap.asapbe.repositories.TaskRepository;
import pl.asap.asapbe.repositories.UserAuthDetailsRepository;
import pl.asap.asapbe.repositories.UserRepository;
import pl.asap.asapbe.response_model.UserDetails;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService{
    private UserAuthDetailsServiceImpl userAuthDetailsServiceImpl;
    private AuthServiceImpl authServiceImpl;
    private UserRepository userRepository;
    private UserAuthDetailsRepository userAuthDetailsRepository;
    private TaskRepository taskRepository;

    @Autowired
    public UserServiceImpl(UserAuthDetailsServiceImpl userAuthDetailsServiceImpl, AuthServiceImpl authServiceImpl, UserRepository userRepository, UserAuthDetailsRepository userAuthDetailsRepository, TaskRepository taskRepository) {
        this.userAuthDetailsServiceImpl = userAuthDetailsServiceImpl;
        this.authServiceImpl = authServiceImpl;
        this.userRepository = userRepository;
        this.userAuthDetailsRepository = userAuthDetailsRepository;
        this.taskRepository = taskRepository;
    }

    public List<UserEntity> getListOfAllUsers(String authToken) {
        if (authServiceImpl.authenticateUserByToken(authToken) != null) //user authenticated
            return userRepository.findAll();
        else
            throw new UserAuthenticationException();
    }

    public UserAuthDetailsEntity performUserLogin(String email, String password) {
        UserEntity userEntity = userRepository.findByEmailAndPassword(email, authServiceImpl.encryptPassword(password));
        if (userEntity != null) {
            return userAuthDetailsServiceImpl.getUserAuthDetailsFromUserEntity(userEntity);
        } else
            throw new UserAuthenticationException();
    }

    public UserAuthDetailsEntity performUserRegistration(String firstName, String lastName, String email, String password) {
        UserEntity searchedUser = userRepository.findByEmail(email);
        if (searchedUser == null) {//user with such email not registered, performing creation
            UserEntity userEntity = new UserEntity(firstName, lastName, email, authServiceImpl.encryptPassword(password));
            String authToken = authServiceImpl.generateToken();
            UserAuthDetailsEntity userAuthDetailsToSave = new UserAuthDetailsEntity(authToken);
            UserEntity savedUser = userRepository.save(userEntity);
            userAuthDetailsToSave.setUserId(savedUser.getId());
            return userAuthDetailsRepository.save(userAuthDetailsToSave);
        } else {
            UserEntity userEntity = new UserEntity(firstName, lastName, email, authServiceImpl.encryptPassword(password));
            if (searchedUser.equals(userEntity))
                return performUserLogin(email, password);
            else
                throw new EmailAlreadyExistsInDatabaseException();
        }
    }

    public void performUserDeletion(String authToken) {
        UserAuthDetailsEntity userAuthDetailsEntity = authServiceImpl.authenticateUserByToken(authToken);
        UserEntity userToDelete = getUserEntityFromUserAuthDetailsEntity(userAuthDetailsEntity); //no way there is null, because of above if statement
        updateTasksDataAfterUserDeletion(userToDelete);
        userRepository.delete(userToDelete);
        userAuthDetailsRepository.delete(userAuthDetailsEntity);
    }

    public UserEntity getUserEntityFromUserAuthDetailsEntity(UserAuthDetailsEntity userAuthDetailsEntity) {
        Optional<UserEntity> userEntity = userRepository.findById(userAuthDetailsEntity.getUserId());
        if (userEntity.isPresent()) {//user exists in UserEntity table
            return userEntity.get();
        } else {
            userAuthDetailsRepository.delete(userAuthDetailsEntity);// deleting auth details since there is no corresponding user in UserEntity table (very unlikely to happen)
            throw new UserNotFoundException();
        }
    }

    public void performUserPasswordChangeOperation(String authToken, String oldPassword, String newPassword) {
        UserAuthDetailsEntity requestingUser = authServiceImpl.authenticateUserByToken(authToken);
        UserEntity userEntity = getUserEntityFromUserAuthDetailsEntity(requestingUser);
        if (userEntity.getPassword().equals(authServiceImpl.encryptPassword(oldPassword))) {//old password matching
            userEntity.setPassword(authServiceImpl.encryptPassword(newPassword));
            userRepository.save(userEntity);
        } else
            throw new UserAuthenticationException();
    }

    public UserEntity performUserModification(String authToken, UserEntity changedUser) {
        UserAuthDetailsEntity requestingUser = authServiceImpl.authenticateUserByToken(authToken);
        UserEntity userToBeChanged = getUserEntityFromUserAuthDetailsEntity(requestingUser);
        userToBeChanged.setFirstName(changedUser.getFirstName());
        userToBeChanged.setLastName(changedUser.getLastName());
        userToBeChanged.setEmail(changedUser.getEmail());
        return userRepository.save(userToBeChanged);
    }

    public UserDetails getUserDetails(String authToken) {
        UserAuthDetailsEntity requestingUser = authServiceImpl.authenticateUserByToken(authToken);
        UserEntity userEntity = getUserEntityFromUserAuthDetailsEntity(requestingUser);
        return new UserDetails(userEntity.getFirstName(), userEntity.getLastName());
    }

    public UserEntity getUserFromDbById(Long id) {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isPresent())
            return user.get();
        else
            throw new UserNotFoundException();
    }

    public void updateTasksDataAfterUserDeletion(UserEntity userEntity) {
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

