package bg.sofia.uni.fmi.mjt.goodreads.exception;

public class APIResponseException extends Exception {
    public APIResponseException(String message) {
        super(message);
    }

    public APIResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
