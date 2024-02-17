package bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.thread;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater.StringUpdater;
import bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater.Updater;
import bg.sofia.uni.fmi.mjt.goodreads.user.User;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SaveToFileThreadTest {
    @Test
    public void testRun() {
        List<User> users = new ArrayList<>();
        Map<String, Map<String, List<Book>>> userBookshelf = new HashMap<>();
        String usersTable = "userTableInitial";
        Updater usersTableUpdater = new StringUpdater(usersTable);
        String listTable = "listTableInitial";
        Updater listTableUpdater = new StringUpdater(listTable);
        Gson gson = new Gson();

        SaveToFileThread saveToFileThread = new SaveToFileThread(users, userBookshelf, usersTableUpdater,
                listTableUpdater, gson);

        saveToFileThread.run();

        assertEquals("[]", usersTableUpdater.getData(),
                "Expected data to get reset when updating");
        assertEquals("{}", listTableUpdater.getData(),
                "Expected data to get reset when updating");
    }
}
