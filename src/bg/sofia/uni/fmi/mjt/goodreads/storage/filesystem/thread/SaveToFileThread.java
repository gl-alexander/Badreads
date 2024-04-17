package bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.thread;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater.Updater;
import bg.sofia.uni.fmi.mjt.goodreads.user.User;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class SaveToFileThread implements Runnable {
    private final Updater usersTableUpdater;
    private  final Updater listTableUpdater;
    private final List<User> users;
    private final Map<String, Map<String, List<Book>>> userBookshelf;
    private final Gson gson;

    public SaveToFileThread(List<User> users, Map<String, Map<String, List<Book>>> userBookshelf,
                            Updater usersTableUpdater, Updater listTableUpdater, Gson gson) {
        this.users = users;
        this.userBookshelf = userBookshelf;
        this.usersTableUpdater = usersTableUpdater;
        this.listTableUpdater = listTableUpdater;
        this.gson = gson;
    }

    @Override
    public void run() {
        try {
            saveUsers();
            saveBookshelves();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        synchronized (users) {
            usersTableUpdater.update(gson.toJson(users));
        }
    }

    private void saveBookshelves() {
        synchronized (userBookshelf) {
            listTableUpdater.update(gson.toJson(userBookshelf));
        }
    }

}
