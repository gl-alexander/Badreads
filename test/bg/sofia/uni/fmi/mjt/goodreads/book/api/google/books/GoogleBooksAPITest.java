package bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.book.BookDetails;
import bg.sofia.uni.fmi.mjt.goodreads.book.BookRequest;
import bg.sofia.uni.fmi.mjt.goodreads.book.api.HttpRequestSender;
import bg.sofia.uni.fmi.mjt.goodreads.exception.APIResponseException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoogleBooksAPITest {
    private static final int STATUS_CODE_OK = 200;
    private static final String BOOK1_JSON = "{\n" +
            "  \"id\": \"1\",\n" +
            "  \"volumeInfo\": {\n" +
            "    \"title\": \"The Wind-up Bird Chronicle\",\n" +
            "    \"authors\": [\n" +
            "      \"Haruki Murakami\"\n" +
            "    ],\n" +
            "    \"publisher\": \"Alfred A. Knopf\",\n" +
            "    \"publishedDate\": \"2007-08-02\",\n" +
            "    \"description\": \"Short Description\",\n" +
            "    \"pageCount\": 611,\n" +
            "    \"categories\": [\n" +
            "      \"Fiction / Historical / General\"\n" +
            "    ],\n" +
            "    \"averageRating\": 4,\n" +
            "    \"ratingsCount\": 9\n" +
            "  }\n" +
            "}";

    private static final BookDetails BOOK1_DETAILS = new BookDetails("1", "The Wind-up Bird Chronicle",
            List.of("Haruki Murakami"), "Short Description", 611, 2007,
            List.of("Fiction / Historical / General"), 4, 9);

    private static final Book BOOK1 = new Book(BOOK1_DETAILS.id(), BOOK1_DETAILS.title(), BOOK1_DETAILS.authors());

    private static final String BOOK2_JSON = "{\n" +
            "      \"id\": \"2\",\n" +
            "      \"volumeInfo\": {\n" +
            "        \"title\": \"Another Book Title\",\n" +
            "        \"authors\": [\n" +
            "          \"Author Name\"\n" +
            "        ],\n" +
            "        \"publisher\": \"Publisher Name\",\n" +
            "        \"publishedDate\": \"2000\",\n" +
            "        \"description\": \"Another short description\",\n" +
            "        \"pageCount\": 400,\n" +
            "        \"categories\": [\n" +
            "          \"Fiction / Mystery\"\n" +
            "        ],\n" +
            "        \"averageRating\": 3,\n" +
            "        \"ratingsCount\": 12\n" +
            "      }\n" +
            "    }";

    private static final BookDetails BOOK2_DETAILS = new BookDetails("2", "Another Book Title",
            List.of("Author Name"), "Another short description", 400, 2000,
            List.of("Fiction / Mystery"), 3, 12);

    private static final Book BOOK2 = new Book(BOOK2_DETAILS.id(), BOOK2_DETAILS.title(), BOOK2_DETAILS.authors());
    private static final String BOOK_LIST_JSON = "{totalItems:2, items:[" +
            BOOK1_JSON + ", " +
            BOOK2_JSON +
            "]}";
    private static final String MOCK_API_KEY = "apiKey";

    @Mock
    private HttpRequestSender requestSender;

    @InjectMocks
    private GoogleBooksAPI googleBooksAPI;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        googleBooksAPI = new GoogleBooksAPI(MOCK_API_KEY, requestSender);
    }

    @Test
    public void testGetBookInfoCorrect()
            throws URISyntaxException, APIResponseException, IOException, InterruptedException {

        String bookId = "1";
        URI expectedURI = new URI("https", "//www.googleapis.com/books/v1/volumes/" + bookId, null);
        HttpResponse mockedHttpResponse = mock(HttpResponse.class);

        when(requestSender.sendRequest(expectedURI)).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.statusCode()).thenReturn(STATUS_CODE_OK);
        when(mockedHttpResponse.body()).thenReturn(BOOK1_JSON);

        BookDetails result = googleBooksAPI.getBookInfo(bookId);

        assertEquals(BOOK1_DETAILS, result,
                "Expected the API to correctly parse the response JSON");
    }

    @Test
    public void testGetBookInfoIncorrect()
            throws IOException, InterruptedException {

        String bookId = "1";
        HttpResponse mockedHttpResponse = mock(HttpResponse.class);

        when(requestSender.sendRequest(any(URI.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.statusCode()).thenReturn(0);

        assertThrows(APIResponseException.class, () -> googleBooksAPI.getBookInfo(bookId),
                "Expected APIResponseException when response status code isn't 200");
    }

    @Test
    public void testGetBookInfoShortDate()
            throws URISyntaxException, APIResponseException, IOException, InterruptedException {

        String bookId = "2";
        URI expectedURI = new URI("https", "//www.googleapis.com/books/v1/volumes/" + bookId, null);
        HttpResponse mockedHttpResponse = mock(HttpResponse.class);

        when(requestSender.sendRequest(expectedURI)).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.statusCode()).thenReturn(STATUS_CODE_OK);
        when(mockedHttpResponse.body()).thenReturn(BOOK2_JSON);

        BookDetails result = googleBooksAPI.getBookInfo(bookId);

        assertEquals(BOOK2_DETAILS, result,
                "Expected the API to correctly parse the response JSON with short date format - yyyy");
    }

    @Test
    public void testSearchByRequest()
            throws URISyntaxException, APIResponseException, IOException,
            InterruptedException, InvalidRequestException {
        BookRequest bookRequest = BookRequest.builder().setTitle("hey").build();

        HttpResponse<String> mockedHttpResponse = mock(HttpResponse.class);

        when(requestSender.sendRequest(any(URI.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.statusCode()).thenReturn(STATUS_CODE_OK);
        when(mockedHttpResponse.body()).thenReturn(BOOK_LIST_JSON);

        List<Book> result = googleBooksAPI.searchByRequest(bookRequest);
        assertEquals(List.of(BOOK1, BOOK2), result,
                "Expected a correct list of books to be parsed");
    }

    @Test
    public void testSearchByTitle()
            throws URISyntaxException, APIResponseException, IOException,
            InterruptedException, InvalidRequestException {
        String title = "Java Programming";

        HttpResponse<String> mockedHttpResponse = mock(HttpResponse.class);

        when(requestSender.sendRequest(any(URI.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.statusCode()).thenReturn(STATUS_CODE_OK);
        when(mockedHttpResponse.body()).thenReturn(BOOK_LIST_JSON);

        List<Book> result = googleBooksAPI.searchByTitle(title);
        assertEquals(List.of(BOOK1, BOOK2), result,
                "Expected a correct response when searching by title");
    }

    @Test
    public void testSearchByAuthor()
            throws URISyntaxException, APIResponseException, IOException,
            InterruptedException, InvalidRequestException {
        String author = "Sample Author";

        HttpResponse<String> mockedHttpResponse = mock(HttpResponse.class);

        when(requestSender.sendRequest(any(URI.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.statusCode()).thenReturn(STATUS_CODE_OK);
        when(mockedHttpResponse.body()).thenReturn(BOOK_LIST_JSON);

        List<Book> result = googleBooksAPI.searchByAuthor(author);
        assertEquals(List.of(BOOK1, BOOK2), result,
                "Expected a correct response when searching by author");
    }

    @Test
    public void testSearchByTitleAndAuthor()
            throws URISyntaxException, APIResponseException, IOException,
            InterruptedException, InvalidRequestException {
        String author = "Sample Author";
        String title = "Sample title";

        HttpResponse<String> mockedHttpResponse = mock(HttpResponse.class);

        when(requestSender.sendRequest(any(URI.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.statusCode()).thenReturn(STATUS_CODE_OK);
        when(mockedHttpResponse.body()).thenReturn(BOOK_LIST_JSON);

        List<Book> result = googleBooksAPI.searchByTitleAndAuthor(title, author);
        assertEquals(List.of(BOOK1, BOOK2), result,
                "Expected a correct response when searching by title and author");
    }
}
