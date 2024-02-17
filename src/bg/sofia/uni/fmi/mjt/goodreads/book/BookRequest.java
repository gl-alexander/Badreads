package bg.sofia.uni.fmi.mjt.goodreads.book;

import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidRequestException;

public class BookRequest {
    public static final int BOOKS_PER_PAGE = 10;
    protected String title;
    protected String author;

    private BookRequest(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public static BookRequestBuilder builder() {
        return new BookRequestBuilder();
    }

    public static class BookRequestBuilder {
        private String title;
        private String author;
        private boolean validRequest = false;

        public BookRequestBuilder setTitle(String title) {
            this.title = title;
            validRequest = true;
            return this;
        }

        public BookRequestBuilder setAuthor(String author) {
            this.author = author;
            validRequest = true;
            return this;
        }

        public BookRequest build() throws InvalidRequestException {
            if (!validRequest) {
                throw new InvalidRequestException("Request must have at least one parameter");
            }
            return new BookRequest(title, author);
        }
    }
}
