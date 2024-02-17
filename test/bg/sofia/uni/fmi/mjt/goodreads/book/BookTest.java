package bg.sofia.uni.fmi.mjt.goodreads.book;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookTest {
    @Test
    void testToString() {
        Book book = new Book("1", "Title", List.of("Author1", "Author2"));
        String expected = "\"Title\" - Author1, Author2";
        assertEquals(expected, book.toString(),
                "Expected book to be printed correctly");
    }

    @Test
    void testToStringSingleAuthor() {
        Book book = new Book("1", "Title", List.of("Author1"));
        String expected = "\"Title\" - Author1";
        assertEquals(expected, book.toString(),
                "Expected book to be printed correctly");
    }

    @Test
    void testToStringNullAuthor() {
        Book book = new Book("1", "Title", null);
        String expected = "\"Title\" - UNKNOWN";
        assertEquals(expected, book.toString(),
                "Expected book to be printed correctly when author list is null");
    }
}
