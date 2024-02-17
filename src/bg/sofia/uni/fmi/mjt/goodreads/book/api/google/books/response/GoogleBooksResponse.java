package bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.response;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;

import java.util.List;

public record GoogleBooksResponse(int totalCount, List<Book> books) {
}
