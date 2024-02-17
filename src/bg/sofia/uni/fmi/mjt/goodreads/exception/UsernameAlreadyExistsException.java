package bg.sofia.uni.fmi.mjt.goodreads.exception;

public class UsernameAlreadyExistsException extends Exception {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public UsernameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
