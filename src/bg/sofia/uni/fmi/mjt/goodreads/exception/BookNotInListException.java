package bg.sofia.uni.fmi.mjt.goodreads.exception;

public class BookNotInListException extends Exception {
    public BookNotInListException(String message) {
        super(message);
    }

    public BookNotInListException(String message, Throwable cause) {
        super(message, cause);
    }
}
