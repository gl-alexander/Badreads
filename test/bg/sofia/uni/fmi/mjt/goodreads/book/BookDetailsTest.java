package bg.sofia.uni.fmi.mjt.goodreads.book;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookDetailsTest {
    private static final BookDetails BOOK = new BookDetails(
            "1",
            "Sample Book",
            List.of("Author1", "Author2"),
            "Sample description",
            300,
            2022,
            List.of("Fiction", "Mystery"),
            4,
            100);

    private static final BookDetails BOOK_NULL = new BookDetails(
            "2",
            "Book with Null Values",
            List.of("Author1"),
            "desc",
            0,
            2021,
            null,
            0,
            0
    );

    private static final BookDetails BOOK_WITH_TAGS = new BookDetails(
            "1", "Test Book", List.of("Author1", "Author2"),
            "<p>This is a <i>test</i> description<br>with multiple tags</p>",
            200, 2020, List.of("Fiction", "Science Fiction"), 4, 100
    );

    @Test
    public void testToString() {
        String expectedString = "Title - \"Sample Book\"" + System.lineSeparator() +
                "Authors - Author1, Author2" + System.lineSeparator() +
                "Description - Sample description" + System.lineSeparator() +
                "Page count - 300" + System.lineSeparator() +
                "Publish date - 2022" + System.lineSeparator() +
                "Categories - Fiction, Mystery" + System.lineSeparator() +
                "Rating - 4, 100 total ratings";

        assertEquals(expectedString, BOOK.toString());
    }

    @Test
    public void testToStringWithNullValues() {
        String expectedString = "Title - \"Book with Null Values\"" + System.lineSeparator() +
                "Authors - Author1" + System.lineSeparator() +
                "Description - desc" + System.lineSeparator() +
                "Publish date - 2021";

        assertEquals(expectedString, BOOK_NULL.toString());
    }

    @Test
    public void testToStringWithRemovedTags() {
        String result = BOOK_WITH_TAGS.toString();

        assertEquals("Title - \"Test Book\"" + System.lineSeparator() +
                "Authors - Author1, Author2" + System.lineSeparator() +
                "Description - This is a test description" + System.lineSeparator() +
                "with multiple tags" + System.lineSeparator() +
                "Page count - 200" + System.lineSeparator() +
                "Publish date - 2020" + System.lineSeparator() +
                "Categories - Fiction, Science Fiction" + System.lineSeparator() +
                "Rating - 4, 100 total ratings", result);
    }

}
