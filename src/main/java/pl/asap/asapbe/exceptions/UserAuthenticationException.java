package pl.asap.asapbe.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Bad credentials or wrong token provided")
public class UserAuthenticationException extends RuntimeException{

}
