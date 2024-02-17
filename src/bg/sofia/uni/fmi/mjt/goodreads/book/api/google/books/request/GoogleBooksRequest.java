package bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.request;

import bg.sofia.uni.fmi.mjt.goodreads.book.BookRequest;

public class GoogleBooksRequest {
    private static final String QUERY_TAG = "q";
    private static final String AUTHOR_TAG = "inauthor";
    private static final String TITLE_TAG = "intitle";
    private static final String TAG_DENOMINATOR = ":";
    private static final String KEY_TAG = "key";
    private static final String START_INDEX_TAG = "startIndex";
    private static final String RESULTS_COUNT_TAG = "maxResults";
    private static final int DEFAULT_FIRST_PAGE = 0;
    private String apiKey;
    private BookRequest request;

    public GoogleBooksRequest(BookRequest request, String apiKey) {
        this.request = request;
        this.apiKey = apiKey;
    }

    public String createQueryString() {
        return createQueryString(DEFAULT_FIRST_PAGE);
    }

    public String createQueryString(int pageIndex) {
        StringBuilder sb = new StringBuilder("?");
        sb.append(QUERY_TAG).append("=");
        if (request.getTitle() != null) {
            sb.append(TITLE_TAG).append(TAG_DENOMINATOR).append(request.getTitle());
        }
        if (request.getAuthor() != null) {
            sb.append("+").append(AUTHOR_TAG).append(TAG_DENOMINATOR).append(request.getAuthor());
        }
        if (pageIndex > 0) {
            sb.append("&").append(START_INDEX_TAG).append("=").append(pageIndex * BookRequest.BOOKS_PER_PAGE);
        }
        sb.append("&").append(RESULTS_COUNT_TAG).append("=").append(BookRequest.BOOKS_PER_PAGE);
        sb.append("&").append(KEY_TAG).append("=").append(apiKey);
        return sb.toString();
    }
}
