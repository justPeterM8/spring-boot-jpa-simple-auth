package pl.asap.asapbe.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "User has no permissions to perform this action (basing on user's role in project)")
public class InsufficientPermissionException extends RuntimeException{

}
