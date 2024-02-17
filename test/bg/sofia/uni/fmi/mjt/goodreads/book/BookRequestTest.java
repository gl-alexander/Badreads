package bg.sofia.uni.fmi.mjt.goodreads.book;

import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BookRequestTest {
    @Test
    public void testBuildWithTitle() throws InvalidRequestException {
        BookRequest bookRequest = BookRequest.builder()
                .setTitle("Java Programming")
                .build();

        assertEquals("Java Programming", bookRequest.getTitle());
        assertNull(bookRequest.getAuthor());
    }

    @Test
    public void testBuildWithAuthor() throws InvalidRequestException {
        BookRequest bookRequest = BookRequest.builder()
                .setAuthor("John Doe")
                .build();

        assertNull(bookRequest.getTitle());
        assertEquals("John Doe", bookRequest.getAuthor());
    }

    @Test
    public void testBuildWithTitleAndAuthor() throws InvalidRequestException {
        BookRequest bookRequest = BookRequest.builder()
                .setTitle("Java Programming")
                .setAuthor("John Doe")
                .build();

        assertEquals("Java Programming", bookRequest.getTitle());
        assertEquals("John Doe", bookRequest.getAuthor());
    }

    @Test
    public void testBuildWithoutParameters() {
        assertThrows(InvalidRequestException.class, () -> BookRequest.builder().build());
    }
}
