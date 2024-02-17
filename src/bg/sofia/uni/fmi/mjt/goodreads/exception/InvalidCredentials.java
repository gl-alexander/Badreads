package bg.sofia.uni.fmi.mjt.goodreads.exception;

public class InvalidCredentials extends Exception {
    public InvalidCredentials(String message) {
        super(message);
    }

    public InvalidCredentials(String message, Throwable cause) {
        super(message, cause);
    }
}
