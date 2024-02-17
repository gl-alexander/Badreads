package bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.exception.BookNotInListException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidCredentials;
import bg.sofia.uni.fmi.mjt.goodreads.exception.ListNameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.ListNameDoesntExistException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.UserDoesntExistException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.UsernameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.goodreads.storage.Storage;
import bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.thread.SaveToFileThread;
import bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater.Updater;
import bg.sofia.uni.fmi.mjt.goodreads.user.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FileSystemStorage implements Storage {
    private static final String RELATIVE_PATH_STRING = "src/bg/sofia/uni/fmi/mjt/goodreads/storage/filesystem/tables";
    private static final Path TABLES_PATH = Paths.get(RELATIVE_PATH_STRING);
    public static final File USERS_TABLE = new File(RELATIVE_PATH_STRING + "/users_table.json");
    public static final File LISTS_TABLE = new File(RELATIVE_PATH_STRING + "/lists_table.json");
    private static final int INITIAL_DELAY_SECONDS = 10;
    private static final int SAVE_PERIOD_SECONDS = 60;

    private final List<User> users;
    private final Map<String, Map<String, List<Book>>> userBookshelfs;
    private Reader usersTableReader;
    private Reader bookshelfTableReader;
    private Gson gson;

    public FileSystemStorage(Reader usersTableReader, Reader bookshelfTableReader,
                             Updater usersTableUpdater, Updater bookshelfTableUpdater,
                             ScheduledExecutorService saveToFileExecutor) throws IOException {
        this(usersTableReader, bookshelfTableReader, usersTableUpdater, bookshelfTableUpdater, saveToFileExecutor,
                INITIAL_DELAY_SECONDS, SAVE_PERIOD_SECONDS);
    }

    public FileSystemStorage(Reader usersTableReader, Reader bookshelfTableReader,
                             Updater usersTableUpdater, Updater bookshelfTableUpdater,
                             ScheduledExecutorService saveToFileExecutor, int initialDelayToSave, int savePeriod)
            throws IOException {
        gson = new Gson();
        this.usersTableReader = usersTableReader;
        this.bookshelfTableReader = bookshelfTableReader;
        initializeTables();
        users = readUserTableDirectory();
        userBookshelfs = readListDirectory();

        saveToFileExecutor.scheduleAtFixedRate(
                new SaveToFileThread(users, userBookshelfs, usersTableUpdater, bookshelfTableUpdater, gson),
                initialDelayToSave,
                savePeriod,
                TimeUnit.SECONDS);
    }

    private synchronized void initializeTables() throws IOException {
        if (!Files.exists(TABLES_PATH)) {
            Files.createDirectory(TABLES_PATH);
        }

        if (!Files.exists(USERS_TABLE.toPath())) {
            Files.createFile(USERS_TABLE.toPath());
        }

        if (!Files.exists(LISTS_TABLE.toPath())) {
            Files.createFile(LISTS_TABLE.toPath());
        }
    }

    private synchronized List<User> readUserTableDirectory() {
        Type listUser = new TypeToken<List<User>>() { }.getType();
        return gson.fromJson(usersTableReader, listUser);
    }

    private synchronized Map<String, Map<String, List<Book>>> readListDirectory() {
        Type userBookshelfMapType = new TypeToken<Map<String, Map<String, List<Book>>>>() { }.getType();
        return gson.fromJson(bookshelfTableReader, userBookshelfMapType);
    }

    @Override
    public synchronized boolean exists(String userId) {
        return users.stream().anyMatch(x -> x.id().equals(userId));
    }

    @Override
    public synchronized String register(String username, String password)
            throws UsernameAlreadyExistsException, ListNameAlreadyExistsException {
        if (!users.isEmpty()) {
            Optional<User> sameUsernameUser = users.stream().filter(x -> x.username().equals(username)).findFirst();
            if (sameUsernameUser.isPresent()) {
                throw new UsernameAlreadyExistsException("This username is already in use");
            }
        }

        String newUserId = UUID.randomUUID().toString();
        users.add(new User(newUserId, username, password, new ArrayList<>(), new ArrayList<>()));

        createList(newUserId, User.WANT_TO_READ_LIST_NAME);
        createList(newUserId, User.READ_LIST_NAME);

        return newUserId;
    }

    @Override
    public synchronized String login(String username, String password) throws InvalidCredentials {
        if (users.isEmpty()) {
            throw new InvalidCredentials("Invalid username");
        }
        Optional<User> sameUsernameUser = users.stream().filter(x -> x.username().equals(username)).findFirst();
        if (sameUsernameUser.isEmpty()) {
            throw new InvalidCredentials("Invalid username");
        }
        if (!sameUsernameUser.get().password().equals(password)) {
            throw new InvalidCredentials("Invalid password");
        }
        return sameUsernameUser.get().id();
    }

    @Override
    public synchronized List<Book> getList(String userId, String listName) throws ListNameDoesntExistException {

        Map<String, List<Book>> booksList = userBookshelfs.get(userId);
        if (!booksList.containsKey(listName)) {
            throw new ListNameDoesntExistException("This user doesn't have such list name");
        }
        return booksList.get(listName);
    }

    @Override
    public synchronized void createList(String userId, String listName) throws ListNameAlreadyExistsException {
        User user = getUserById(userId);
        if (!userBookshelfs.containsKey(userId)) {
            userBookshelfs.put(userId, new HashMap<>());
        }
        Map<String, List<Book>> userLists = userBookshelfs.get(user.id());
        if (userLists.containsKey(listName)) {
            throw new ListNameAlreadyExistsException("List name already exists");
        }
        userBookshelfs.get(user.id()).put(listName, new ArrayList<>());
    }

    @Override
    public synchronized void removeList(String userId, String listName) throws ListNameDoesntExistException {
        User user = getUserById(userId);
        if (!userBookshelfs.containsKey(user.id())) {
            throw new ListNameDoesntExistException("This user doesn't have any lists");
        }
        Map<String, List<Book>> userList = userBookshelfs.get(user.id());
        if (!userList.containsKey(listName)) {
            throw new ListNameDoesntExistException("No list with that name exists");
        }
        userList.remove(listName);
    }

    @Override
    public synchronized void addToList(String userId, String listName, Book book) throws ListNameDoesntExistException {
        User user = getUserById(userId);
        if (!userBookshelfs.containsKey(user.id())) {
            throw new ListNameDoesntExistException("This user doesn't have any lists");
        }
        Map<String, List<Book>> userList = userBookshelfs.get(user.id());
        if (!userList.containsKey(listName)) {
            throw new ListNameDoesntExistException("No list with that name exists");
        }
        userList.get(listName).add(book);
    }

    @Override
    public synchronized void removeFromList(String userId, String listName, int index)
            throws ListNameDoesntExistException, BookNotInListException {
        User user = getUserById(userId);
        if (!userBookshelfs.containsKey(user.id())) {
            throw new ListNameDoesntExistException("This user doesn't have any lists");
        }
        Map<String, List<Book>> userList = userBookshelfs.get(user.id());
        if (!userList.containsKey(listName)) {
            throw new ListNameDoesntExistException("No list with that name exists");
        }
        if (index < 0 || index >= userList.get(listName).size()) {
            throw new BookNotInListException("Index out of range");
        }
        userList.get(listName).remove(index);
    }

    @Override
    public synchronized void addFriend(String userId, String friendUsername) throws UserDoesntExistException {
        User user = getUserById(userId);
        User friend = getUserByUsername(friendUsername);

        if (user.friends().contains(friendUsername)) {
            return;
        }
        user.friends().add(friend.username());
    }

    @Override
    public synchronized List<String> getUserFriends(String userId) {
        User user = getUserById(userId);
        return user.friends();
    }

    @Override
    public synchronized void recommendBook(String userId, Book book) {
        User user = getUserById(userId);
        if (user.recommendedBooks().contains(book)) {
            return;
        }
        user.recommendedBooks().add(book);
    }

    @Override
    public synchronized Map<String, List<Book>> getFriendsRecommendations(String userId) {
        User user = getUserById(userId);
        return user.friends().stream()
                .map(x -> {
                    try {
                        return getUserByUsername(x);
                    } catch (UserDoesntExistException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toMap(
                        User::username,
                        friend -> getUserRecommendations(friend.id())
                        )
                );
    }

    @Override
    public synchronized List<Book> getUserRecommendations(String userId) {
        User user = getUserById(userId);
        return user.recommendedBooks();
    }

    private synchronized User getUserById(String userId) {
        if (users.isEmpty()) {
            throw new RuntimeException("Invalid user ID");
        }
        return users.stream()
                .filter(x -> x.id().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid user ID"));
    }

    private synchronized User getUserByUsername(String username) throws UserDoesntExistException {
        return users.stream()
                .filter(x -> x.username().equals(username))
                .findFirst()
                .orElseThrow(() -> new UserDoesntExistException("Username doesn't exist"));

    }

}
