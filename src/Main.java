import bg.sofia.uni.fmi.mjt.goodreads.Server;
import bg.sofia.uni.fmi.mjt.goodreads.book.BookRepository;
import bg.sofia.uni.fmi.mjt.goodreads.book.api.google.books.GoogleBooksAPI;
import bg.sofia.uni.fmi.mjt.goodreads.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.goodreads.storage.Storage;
import bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.FileSystemStorage;
import bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater.FileUpdater;
import bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater.Updater;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Main {
    private static final int PORT = 7777;
    private static final Path PROPERTIES_PATH = Paths.get("src/app.properties");

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(String.valueOf(PROPERTIES_PATH)));
        String apiKey = properties.getProperty("GoogleApiKey");

        Updater userTableUpdater = new FileUpdater(FileSystemStorage.USERS_TABLE);
        Updater listTableUpdater = new FileUpdater(FileSystemStorage.LISTS_TABLE);

        FileReader userTable = new FileReader(FileSystemStorage.USERS_TABLE);
        FileReader listTable = new FileReader(FileSystemStorage.LISTS_TABLE);

        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
        Storage storage = new FileSystemStorage(userTable, listTable, userTableUpdater, listTableUpdater, executor);
        BookRepository bookRepository = new GoogleBooksAPI(apiKey);

        CommandExecutor commandExecutor = new CommandExecutor(storage, bookRepository);
        Server server = new Server(PORT, commandExecutor);
        server.start();

        try {
            stop(executor, userTable, listTable);
        } catch (IOException e) {
            System.out.println("Error closing resources");
        }
    }

    static void stop(ScheduledExecutorService executor, Reader userTable, Reader listTable) throws IOException {
        executor.close();
        userTable.close();
        listTable.close();
    }
}