package bg.sofia.uni.fmi.mjt.goodreads.exception;

public class ListNameAlreadyExistsException extends Exception {
    public ListNameAlreadyExistsException(String message) {
        super(message);
    }

    public ListNameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
