package bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.deserializer;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class BookDeserializer implements JsonDeserializer<Book>  {
    @Override
    public Book deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject object = json.getAsJsonObject();

        String id = object.get("id").getAsString();
        JsonObject volumeInfo = object.get("volumeInfo").getAsJsonObject();

        String title = volumeInfo.get("title").getAsString();
        Type authorsList = new TypeToken<List<String>>() { }.getType();
        List<String> authors = new Gson().fromJson(volumeInfo.get("authors"), authorsList);

        return new Book(id, title, authors);
    }

}
