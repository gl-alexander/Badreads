package bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.deserializer;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.response.GoogleBooksResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class GoogleBooksResponseDeserializer implements JsonDeserializer<GoogleBooksResponse> {
    private static final String BOOKS_COUNT_TAG = "totalItems";
    private static final String BOOKS_LIST_TAG = "items";
    @Override
    public GoogleBooksResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject object = json.getAsJsonObject();

        int totalCount = object.get(BOOKS_COUNT_TAG).getAsInt();
        Gson booksGson = new GsonBuilder().registerTypeAdapter(Book.class, new BookDeserializer()).create();

        Type listType = new TypeToken<List<Book>>() { }.getType();
        List<Book> books = booksGson.fromJson(object.get(BOOKS_LIST_TAG), listType);
        return new GoogleBooksResponse(totalCount, books);
    }
}
