package bg.sofia.uni.fmi.mjt.goodreads.exception;

public class ListNameDoesntExistException extends Exception {
    public ListNameDoesntExistException(String message) {
        super(message);
    }

    public ListNameDoesntExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
