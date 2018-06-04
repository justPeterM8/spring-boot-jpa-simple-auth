package pl.asap.asapbe.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Task with given id does not exist")
public class NoSuchTaskException extends RuntimeException{

}
