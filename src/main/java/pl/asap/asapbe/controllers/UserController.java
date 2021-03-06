package pl.asap.asapbe.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.response_model.UserDetails;
import pl.asap.asapbe.services.UserService;
import pl.asap.asapbe.services.UserServiceImpl;

import java.util.List;

@RestController
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserEntity>> getAllUsers(@RequestHeader("token") String authToken) {
        return ResponseEntity.ok(userService.getListOfAllUsers(authToken));
    }

    @GetMapping("/users/details")
    public ResponseEntity<UserDetails> getUserDetails(@RequestHeader("token") String authToken) {
        return ResponseEntity.ok(userService.getUserDetails(authToken));
    }

    @PostMapping(value = "/users/login", headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<UserAuthDetailsEntity> loginUser(@RequestParam("email") String email,
                                                           @RequestParam("password") String password) {
        return ResponseEntity.ok(userService.performUserLogin(email, password));
    }

    @PostMapping(value = "/users/password", headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity changeUserPassword(@RequestHeader("token") String authToken,
                                             @RequestParam("oldPassword") String oldPassword,
                                             @RequestParam("newPassword") String newPassword) {
        userService.performUserPasswordChangeOperation(authToken, oldPassword, newPassword);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = "/users", headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<UserAuthDetailsEntity> createUser(@RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password) {
        return ResponseEntity.ok(userService.performUserRegistration(firstName, lastName, email, password));
    }

    @PutMapping(value = "/users")
    public ResponseEntity<UserEntity> modifyUser(@RequestHeader("token") String authToken,
                                     @RequestBody UserEntity user) {
        return ResponseEntity.ok(userService.performUserModification(authToken, user));
    }

    @DeleteMapping("/users")
    public ResponseEntity deleteUser(@RequestHeader("token") String authToken) {
        userService.performUserDeletion(authToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
