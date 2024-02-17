package bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import bg.sofia.uni.fmi.mjt.goodreads.book.BookRequest;

public class GoogleBooksRequestTest {

    @Test
    public void testCreateQueryString() throws InvalidRequestException {
        BookRequest bookRequest = BookRequest.builder()
                .setTitle("Java Programming")
                .setAuthor("John Doe")
                .build();

        String apiKey = "sample-key";
        GoogleBooksRequest googleBooksRequest = new GoogleBooksRequest(bookRequest, apiKey);

        String queryString = googleBooksRequest.createQueryString();
        String expectedQueryString = "?q=intitle:Java Programming+inauthor:John Doe&maxResults=10&key=sample-key";

        assertEquals(expectedQueryString, queryString);
    }

    @Test
    public void testCreateQueryStringWithPageIndex() throws InvalidRequestException {
        BookRequest bookRequest = BookRequest.builder()
                .setTitle("Java Programming")
                .setAuthor("John Doe")
                .build();

        String apiKey = "sample-key";
        GoogleBooksRequest googleBooksRequest = new GoogleBooksRequest(bookRequest, apiKey);

        int pageIndex = 2;
        String queryString = googleBooksRequest.createQueryString(pageIndex);
        String expectedQueryString =
                "?q=intitle:Java Programming+inauthor:John Doe&startIndex=20&maxResults=10&key=sample-key";

        assertEquals(expectedQueryString, queryString);
    }

    @Test
    public void testCreateQueryStringInvalid() {
        assertThrows(InvalidRequestException.class, () -> BookRequest.builder().build());
    }
}
