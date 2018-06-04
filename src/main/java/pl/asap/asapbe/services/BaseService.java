package pl.asap.asapbe.services;

import org.springframework.beans.factory.annotation.Autowired;
import pl.asap.asapbe.repositories.ProjectRepository;
import pl.asap.asapbe.repositories.TaskRepository;
import pl.asap.asapbe.repositories.UserAuthDetailsRepository;
import pl.asap.asapbe.repositories.UserRepository;

public class BaseService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthService authService;
    @Autowired
    UserAuthDetailsRepository userAuthDetailsRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    TaskRepository taskRepository;

    public BaseService() {}
}
