package greencity.exception.exceptions;

public class PasswordSameAsOldException extends RuntimeException {

    public PasswordSameAsOldException(String message) {
        super(message);
    }
}
