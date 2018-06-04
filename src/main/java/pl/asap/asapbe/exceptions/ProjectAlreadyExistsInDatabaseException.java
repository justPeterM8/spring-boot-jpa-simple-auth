package pl.asap.asapbe.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Project with given name already exists")
public class ProjectAlreadyExistsInDatabaseException extends RuntimeException{

}
