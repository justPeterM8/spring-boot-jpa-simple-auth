package pl.asap.asapbe.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "User is not part of project")
public class UserNotPartOfProjectException extends RuntimeException{

}
