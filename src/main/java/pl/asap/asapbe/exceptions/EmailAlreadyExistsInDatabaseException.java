package pl.asap.asapbe.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "User gave already existing email, with wrong account details")
public class EmailAlreadyExistsInDatabaseException extends RuntimeException{

}
