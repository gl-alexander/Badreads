package bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.book.BookDetails;
import bg.sofia.uni.fmi.mjt.goodreads.book.BookRepository;
import bg.sofia.uni.fmi.mjt.goodreads.book.BookRequest;
import bg.sofia.uni.fmi.mjt.goodreads.book.api.HttpRequestSender;
import bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.deserializer.BookDetailsDeserializer;
import bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.deserializer.GoogleBooksResponseDeserializer;
import bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.request.GoogleBooksRequest;
import bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.response.GoogleBooksResponse;
import bg.sofia.uni.fmi.mjt.goodreads.exception.APIResponseException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidRequestException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;

public class GoogleBooksAPI implements BookRepository {
    private static final int STATUS_CODE_OK = 200;
    private static final String API_PROTOCOL = "https";
    private static final String API_ENDPOINT = "//www.googleapis.com/books/v1/volumes";
    private Gson gson;
    private HttpRequestSender requestSender;
    private String apiKey;

    public GoogleBooksAPI(String apiKey, HttpRequestSender requestSender) {
        gson = new GsonBuilder()
                .registerTypeAdapter(GoogleBooksResponse.class, new GoogleBooksResponseDeserializer())
                .registerTypeAdapter(BookDetails.class, new BookDetailsDeserializer())
                .create();
        this.apiKey = apiKey;
        this.requestSender = requestSender;
    }

    public GoogleBooksAPI(String apiKey) {
        this(apiKey, HttpRequestSender.getInstance());
    }

    @Override
    public BookDetails getBookInfo(String id) throws URISyntaxException, APIResponseException {
        try {
            HttpResponse<String> response = requestSender.sendRequest(buildURI("/" + id));
            if (response.statusCode() != STATUS_CODE_OK) {
                throw new APIResponseException(response.body());
            }
            return gson.fromJson(response.body(), BookDetails.class);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public List<Book> searchByRequest(BookRequest request) throws URISyntaxException, APIResponseException {
        return searchByRequest(request, 0);
    }

    @Override
    public List<Book> searchByRequest(BookRequest request, int pageIndex)
            throws URISyntaxException, APIResponseException {

        GoogleBooksRequest googleBooksRequest = new GoogleBooksRequest(request, apiKey);
        URI uri = buildURI(googleBooksRequest.createQueryString(pageIndex));
        return getBooksList(uri);
    }

    @Override
    public List<Book> searchByTitle(String title)
            throws InvalidRequestException, APIResponseException, URISyntaxException {

        GoogleBooksRequest request = new GoogleBooksRequest(
                BookRequest.builder().setTitle(title).build(),
                apiKey
        );
        return getBooksList(buildURI(request.createQueryString()));
    }

    @Override
    public List<Book> searchByAuthor(String author)
            throws InvalidRequestException, APIResponseException, URISyntaxException {

        GoogleBooksRequest request = new GoogleBooksRequest(
                BookRequest.builder().setAuthor(author).build(),
                apiKey
        );
        return getBooksList(buildURI(request.createQueryString()));
    }

    @Override
    public List<Book> searchByTitleAndAuthor(String title, String author)
            throws InvalidRequestException, APIResponseException, URISyntaxException {

        GoogleBooksRequest request = new GoogleBooksRequest(
                BookRequest.builder().setTitle(title).setAuthor(author).build(),
                apiKey
        );
        return getBooksList(buildURI(request.createQueryString()));
    }

    private List<Book> getBooksList(URI requestURI) throws APIResponseException {
        try {
            HttpResponse<String> response = requestSender.sendRequest(requestURI);
            if (response.statusCode() != STATUS_CODE_OK) {
                throw new APIResponseException(response.body());
            }
            GoogleBooksResponse parsedResponse = gson.fromJson(response.body(), GoogleBooksResponse.class);
            return parsedResponse.books();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public URI buildURI(String queryString) throws URISyntaxException {
        return new URI(API_PROTOCOL, API_ENDPOINT + queryString, null).normalize();
    }
}
