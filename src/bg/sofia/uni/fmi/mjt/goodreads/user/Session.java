package bg.sofia.uni.fmi.mjt.goodreads.user;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.book.BookRequest;

import java.nio.ByteBuffer;
import java.util.List;

public class Session {
    private String loggedUserId;
    private BookRequest lastRequest;
    private int currentPage;
    private List<Book> displayedBooks;
    private Book selectedBook;
    private static final int BUFFER_SIZE = 2048;
    private ByteBuffer buffer;

    public Session() {
        resetSession();
    }

    public String getLoggedUserId() {
        return loggedUserId;
    }

    public List<Book> getDisplayedBooks() {
        return displayedBooks;
    }

    public BookRequest getLastRequest() {
        return lastRequest;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setLoggedUserId(String loggedUserId) {
        this.loggedUserId = loggedUserId;
    }

    public void setDisplayedBooks(List<Book> displayedBooks) {
        this.displayedBooks = displayedBooks;
    }

    public void setLastRequest(BookRequest request) {
        this.lastRequest = request;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public Book getSelectedBook() {
        return selectedBook;
    }

    public void setSelectedBook(Book book) {
        this.selectedBook = book;
    }

    public void decrementCurrentPage() {
        currentPage -= 1;
    }

    public void incrementCurrentPage() {
        currentPage += 1;
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    public void resetSession() {
        this.loggedUserId = "";
        this.displayedBooks = null;
        this.lastRequest = null;
        this.currentPage = -1;
        this.selectedBook = null;

        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }
}
