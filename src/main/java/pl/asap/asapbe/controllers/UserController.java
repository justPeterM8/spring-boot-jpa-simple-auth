package pl.asap.asapbe.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.response_model.UserDetails;
import pl.asap.asapbe.services.UserServiceImpl;

import java.util.List;

@RestController
public class UserController {
    private UserServiceImpl userServiceImpl;

    @Autowired
    public UserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserEntity>> getAllUsers(@RequestHeader("token") String authToken) {
        return ResponseEntity.ok(userServiceImpl.getListOfAllUsers(authToken));
    }

    @GetMapping("/users/details")
    public ResponseEntity<UserDetails> getUserDetails(@RequestHeader("token") String authToken) {
        return ResponseEntity.ok(userServiceImpl.getUserDetails(authToken));
    }

    @PostMapping(value = "/users/login", headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<UserAuthDetailsEntity> loginUser(@RequestParam("email") String email,
                                                           @RequestParam("password") String password) {
        return ResponseEntity.ok(userServiceImpl.performUserLogin(email, password));
    }

    @PostMapping(value = "/users/password", headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity changeUserPassword(@RequestHeader("token") String authToken,
                                             @RequestParam("oldPassword") String oldPassword,
                                             @RequestParam("newPassword") String newPassword) {
        userServiceImpl.performUserPasswordChangeOperation(authToken, oldPassword, newPassword);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = "/users", headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<UserAuthDetailsEntity> createUser(@RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password) {
        return ResponseEntity.ok(userServiceImpl.performUserRegistration(firstName, lastName, email, password));
    }

    @PutMapping(value = "/users")
    public ResponseEntity<UserEntity> modifyUser(@RequestHeader("token") String authToken,
                                     @RequestBody UserEntity user) {
        return ResponseEntity.ok(userServiceImpl.performUserModification(authToken, user));
    }

    @DeleteMapping("/users")
    public ResponseEntity deleteUser(@RequestHeader("token") String authToken) {
        userServiceImpl.performUserDeletion(authToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
