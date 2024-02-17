package bg.sofia.uni.fmi.mjt.goodreads.exception;

public class UserDoesntExistException extends Exception {
    public UserDoesntExistException(String message) {
        super(message);
    }

    public UserDoesntExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
