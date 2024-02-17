package bg.sofia.uni.fmi.mjt.goodreads.user;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;

import java.util.List;

public record User(String id, String username, String password, List<String> friends, List<Book> recommendedBooks) {
    public static final String WANT_TO_READ_LIST_NAME = "want-to-read";
    public static final String READ_LIST_NAME = "read";
}
