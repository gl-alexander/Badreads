package bg.sofia.uni.fmi.mjt.goodreads.book.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpRequestSender {
    private static HttpRequestSender instance;
    private HttpClient client;

    private HttpRequestSender() {
        client = HttpClient.newHttpClient();
    }

    public static HttpRequestSender getInstance() {
        if (instance == null) {
            instance = new HttpRequestSender();
        }
        return instance;
    }

    public HttpResponse<String> sendRequest(URI uri) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .build();
        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    public void close() {
        client.close();
    }
}
