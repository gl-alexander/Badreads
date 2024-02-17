package bg.sofia.uni.fmi.mjt.goodreads.book;

import bg.sofia.uni.fmi.mjt.goodreads.exception.APIResponseException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidRequestException;

import java.net.URISyntaxException;
import java.util.List;

public interface BookRepository {
    List<Book> searchByTitle(String title) throws InvalidRequestException, APIResponseException, URISyntaxException;

    List<Book> searchByAuthor(String author) throws InvalidRequestException, APIResponseException, URISyntaxException;

    List<Book> searchByTitleAndAuthor(String title, String author)
            throws InvalidRequestException, APIResponseException, URISyntaxException;

    BookDetails getBookInfo(String id) throws URISyntaxException, APIResponseException;

    List<Book> searchByRequest(BookRequest request) throws URISyntaxException, APIResponseException;

    List<Book> searchByRequest(BookRequest request, int pageIndex) throws URISyntaxException, APIResponseException;
}
