package greencity.exception.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception that we get when user trying to pass bad request.
 *
 * @author Nazar Vladyka
 * @version 1.0
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    /**
     * Constructor for BadRequestException.
     *
     * @param message - giving message.
     */
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String invalid_input_data, ConstraintViolationException ex) {
    }
}

