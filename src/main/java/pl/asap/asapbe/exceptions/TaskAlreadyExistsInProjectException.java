package pl.asap.asapbe.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Task with given title is already created")
public class TaskAlreadyExistsInProjectException extends RuntimeException{

}
