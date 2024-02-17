package bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.deserializer;

import bg.sofia.uni.fmi.mjt.goodreads.book.BookDetails;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookDetailsDeserializer implements JsonDeserializer<BookDetails> {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Override
    public BookDetails deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject object = json.getAsJsonObject();
        Gson tempGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        String id = object.get("id").getAsString();
        JsonObject volumeInfo = object.get("volumeInfo").getAsJsonObject();
        String publishDateString = volumeInfo.get("publishedDate").getAsString();
        int year;
        try {
            year = Integer.parseInt(publishDateString);
        } catch (NumberFormatException e) {
            year = LocalDate.parse(publishDateString, FORMAT).getYear();
        }

        BookDetails tempBook = tempGson.fromJson(volumeInfo, BookDetails.class);
        return new BookDetails(id, tempBook.title(), tempBook.authors(), tempBook.description(), tempBook.pageCount(),
                year, tempBook.categories(), tempBook.averageRating(), tempBook.ratingsCount());
    }
}
