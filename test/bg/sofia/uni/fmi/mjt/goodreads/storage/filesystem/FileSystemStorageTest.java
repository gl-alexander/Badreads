package bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.exception.BookNotInListException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidCredentials;
import bg.sofia.uni.fmi.mjt.goodreads.exception.ListNameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.ListNameDoesntExistException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.UserDoesntExistException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.UsernameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater.StringUpdater;
import bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater.Updater;
import bg.sofia.uni.fmi.mjt.goodreads.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileSystemStorageTest {
    private static final Book BOOK1 = new Book("1", "Title1", List.of("Author1"));
    private static final Book BOOK2 = new Book("2", "Title2", List.of("Author3"));
    private static final Book BOOK3 = new Book("3", "Title3", List.of("Author3"));
    private static final User USER1 = new User("1", "user1", "password",
            List.of("user2", "user3"), List.of(BOOK1));
    private static final User USER2 = new User("2", "user2", "password",
            Collections.emptyList(), Collections.emptyList());
    private static final User USER3 = new User("3", "user3", "password",
            List.of("user1"), List.of(BOOK3));

    private static final Map<String, Map<String, List<Book>>> USER_LISTS = Map.of(
            "1", Map.of("want-to-read", List.of(BOOK2), "read", List.of(BOOK1)),
            "2", Map.of("want-to-read", List.of(BOOK1, BOOK2))
    );

    private static final String USER_LIST_JSON = "[" +
            "{" +
            "\"id\":\"1\"," +
            "\"username\":\"user1\"," +
            "\"password\":\"password\"," +
            "\"friends\":[\"user2\",\"user3\"]," +
            "\"recommendedBooks\":[{\"id\":\"1\",\"title\":\"Title1\",\"authors\":[\"Author1\"]}]" +
            "}," +
            "{" +
            "\"id\":\"2\"," +
            "\"username\":\"user2\"," +
            "\"password\":\"password\"," +
            "\"friends\":[]," +
            "\"recommendedBooks\":[]" +
            "}," +
            "{" +
            "\"id\":\"3\"," +
            "\"username\":\"user3\"," +
            "\"password\":\"password\"," +
            "\"friends\":[\"user1\"]," +
            "\"recommendedBooks\":[{\"id\":\"3\",\"title\":\"Title3\",\"authors\":[\"Author3\"]}]" +
            "}" +
            "]";

    private static final String BOOKSHELFS_JSON = "{" +
            "\"2\":{" +
            "\"want-to-read\":[" +
            "{\"id\":\"1\",\"title\":\"Title1\",\"authors\":[\"Author1\"]}," +
            "{\"id\":\"2\",\"title\":\"Title2\",\"authors\":[\"Author3\"]}" +
            "]" +
            "}," +
            "\"1\":{" +
            "\"want-to-read\":[{\"id\":\"2\",\"title\":\"Title2\",\"authors\":[\"Author3\"]}]," +
            "\"read\":[{\"id\":\"1\",\"title\":\"Title1\",\"authors\":[\"Author1\"]}]" +
            "}" +
            "}";
    @Mock
    private ScheduledExecutorService saveToFileExecutor;
    @Mock
    private ScheduledFuture task;
    private FileSystemStorage storage;
    private StringWriter usersTableWriter;
    private StringWriter bookshelfTableWriter;
    private Updater usersTableUpdater;
    private Updater bookshelfTableUpdater;

    @BeforeEach
    public void setUp() throws IOException {
        saveToFileExecutor = mock(ScheduledExecutorService.class);
        task = mock(ScheduledFuture.class);

        when(saveToFileExecutor.scheduleWithFixedDelay(any(Runnable.class), anyInt(), anyInt(), any(TimeUnit.class)))
                .thenReturn(task);

        usersTableWriter = new StringWriter();
        bookshelfTableWriter = new StringWriter();
        Reader usersTableReader = new StringReader(USER_LIST_JSON);
        Reader bookshelfTableReader = new StringReader(BOOKSHELFS_JSON);

        usersTableUpdater = new StringUpdater(usersTableWriter.toString());
        bookshelfTableUpdater = new StringUpdater(bookshelfTableWriter.toString());

        storage = new FileSystemStorage(
                usersTableReader, bookshelfTableReader, usersTableUpdater, bookshelfTableUpdater, saveToFileExecutor
                );
    }

    @Test
    public void testExistsForExistingUser() {
        assertTrue(storage.exists(USER1.id()), "Exists method should return true for an existing user");
    }

    @Test
    public void testExistsForNonexistentUser() {
        assertFalse(storage.exists("randomId"), "Exists method should return false for a nonexistent user");
    }

    @Test
    public void testExistsForEmptyUserId() {
        assertFalse(storage.exists(""), "Exists method should return false for an empty user id");
    }

    @Test
    public void testExistsForNullUserId() {
        assertFalse(storage.exists(null), "Exists method should return false for a null user id");
    }

    @Test
    public void testRegisterNewUserSaveToUpdater() throws Exception {
        String username = "newUser";
        String password = "password";

        String userId = storage.register(username, password);

        assertNotNull(userId, "User Id returned from register shouldn't be null");
        assertFalse(userId.isEmpty(), "User Id returned from register shouldn't be empty");
        assertTrue(storage.exists(userId), "Expected register to add the new user to storage");
    }

    @Test
    public void testRegisterUserWithSameUsername() {
        assertThrows(UsernameAlreadyExistsException.class,
                () -> storage.register("user1", "anotherPassword"),
                "Expected UsernameAlreadyExistsException when registering with used username"
        );
    }

    @Test
    public void testLoginWithValidCredentials() throws Exception {
        String userId = storage.login("user1", "password");

        assertNotNull(userId, "User Id returned from login shouldn't be null");
        assertFalse(userId.isEmpty(), "User Id returned from login shouldn't be empty");
        assertEquals("1", userId, "Expected user Id returned from login to match to the registered user's id");
    }

    @Test
    public void testLoginWithInvalidUsername() {
        assertThrows(InvalidCredentials.class, () -> storage.login("username", "password"),
                "Expected InvalidCredentials exception when logging in with invalid username");
    }

    @Test
    public void testLoginWithInvalidPassword() {
        assertThrows(InvalidCredentials.class, () -> storage.login("user1", "invalidPassword"),
                "Expected InvalidCredentials exception when logging in with invalid password");
    }

    @Test
    public void testGetListWithValidListName() throws Exception {
        String userId = "2";
        String listName = "want-to-read";

        List<Book> list = storage.getList(userId, listName);
        assertNotNull(list, "Expected returned list to not be null");
        assertEquals(2, list.size(), "Expected returned list to be the correct size");
        assertTrue(list.contains(BOOK1), "Expected returned list to have the correct books");
        assertTrue(list.contains(BOOK2), "Expected returned list to have the correct books");
    }

    @Test
    public void testGetListWithInvalidListName() {
        String userId = "1";
        String invalidListName = "nonexistent-list";
        assertThrows(ListNameDoesntExistException.class, () -> storage.getList(userId, invalidListName),
                "Expected ListNameDoesntExistException when getting list that doesn't exist");
    }

    @Test
    public void testGetListWithNonexistentUser() {
        String userId = "nonexistent-user";
        String listName = "want-to-read";

        assertThrows(RuntimeException.class, () -> storage.getList(userId, listName),
                "Expected RuntimeException when getting list from invalid  id");
    }

    @Test
    public void testCreateListSuccessfully() throws Exception {
        String userId = "1";
        String newListName = "new-list";

        storage.createList(userId, newListName);
        assertThrows(ListNameAlreadyExistsException.class, () -> storage.createList(userId, newListName),
                "Expected new list name to be added for the user");
    }

    @Test
    public void testCreateListWithExistingListName() {
        String userId = "1";
        String existingListName = "want-to-read";

        assertThrows(ListNameAlreadyExistsException.class, () -> storage.createList(userId, existingListName),
                "Expected ListNameAlreadyExistsException when creating a list that already exists");
    }

    @Test
    public void testCreateListForNonexistentUser() {
        String userId = "nonexistent-user";
        String newListName = "new-list";

        assertThrows(RuntimeException.class, () -> storage.createList(userId, newListName),
                "Expected RuntimeException when passing invalid id");
    }

    @Test
    public void testRemoveListSuccessfully() throws Exception {
        String userId = "1";
        String listNameToRemove = "read";

        storage.removeList(userId, listNameToRemove);

        assertThrows(ListNameDoesntExistException.class, () -> storage.getList(userId, listNameToRemove),
                "Expected list to be successfully removed");
    }

    @Test
    public void testRemoveListWithNonexistentListName() {
        String userId = "1";
        String nonexistentListName = "nonexistent-list";

        assertThrows(ListNameDoesntExistException.class, () -> storage.removeList(userId, nonexistentListName),
                "Expected ListNameDoesntExistException when removing a list that doesn't exist");
    }

    @Test
    public void testRemoveListForNonexistentUser() {
        String userId = "nonexistent-user";
        String listNameToRemove = "read";

        assertThrows(RuntimeException.class, () -> storage.removeList(userId, listNameToRemove),
                "Expected RuntimeException when passing invalid id");
    }

    @Test
    public void testRemoveListForUserWithNoLists() {
        String userId = "3";
        String listNameToRemove = "read";

        assertThrows(ListNameDoesntExistException.class, () -> storage.removeList(userId, listNameToRemove),
                "Expected ListNameDoesntExistException exception when removing list for user with no lists");
    }

    @Test
    public void testAddToListSuccessfully() throws Exception {
        String userId = "1";
        String listName = "want-to-read";
        Book bookToAdd = new Book("4", "Title4", List.of("Author4"));

        storage.addToList(userId, listName, bookToAdd);

        List<Book> updatedList = storage.getList(userId, listName);
        assertTrue(updatedList.contains(bookToAdd),
                "Expected book to be added to the given list name");
    }

    @Test
    public void testAddToListForUserWithNoLists() {
        String userId = "3";
        String targetList = "read";

        assertThrows(ListNameDoesntExistException.class, () -> storage.addToList(userId, targetList, BOOK1),
                "Expected ListNameDoesntExistException exception when adding to list for user with no lists");
    }

    @Test
    public void testAddToListWithNonexistentListName() {
        String userId = "1";
        String nonexistentListName = "nonexistent-list";
        Book bookToAdd = new Book("4", "Title4", List.of("Author4"));

        assertThrows(ListNameDoesntExistException.class,
                () -> storage.addToList(userId, nonexistentListName, bookToAdd),
                "Expected ListNameDoesntExistException when adding to list that doesn't exist");
    }

    @Test
    public void testAddToListForNonexistentUser() {
        String userId = "nonexistent-user";
        String listName = "want-to-read";
        Book bookToAdd = new Book("4", "Title4", List.of("Author4"));

        assertThrows(RuntimeException.class, () -> storage.addToList(userId, listName, bookToAdd),
                "Expected RuntimeException when passing invalid id");
    }

    @Test
    public void testRemoveFromListSuccessfully() throws Exception {
        String userId = "2";
        String listName = "want-to-read";
        int indexToRemove = 0;

        storage.removeFromList(userId, listName, indexToRemove);

        List<Book> updatedList = storage.getList(userId, listName);
        assertEquals(1, updatedList.size(), "Expected list size to change after removing successfully");
        assertEquals(BOOK2, updatedList.get(0), "Expected correct list after removing book");
    }

    @Test
    public void testRemoveFromListForUserWithNoLists() {
        String userId = "3";
        String listNameToRemove = "read";

        assertThrows(ListNameDoesntExistException.class, () -> storage.removeFromList(userId, listNameToRemove, 0),
                "Expected ListNameDoesntExistException exception when removing from list for user with no lists");
    }

    @Test
    public void testRemoveFromListWithNonexistentListName() {
        String userId = "1";
        String nonexistentListName = "nonexistent-list";
        int indexToRemove = 0;

        assertThrows(ListNameDoesntExistException.class,
                () -> storage.removeFromList(userId, nonexistentListName, indexToRemove),
                "Expected ListNameDoesntExistException when removing from list that doesn't exist");
    }

    @Test
    public void testRemoveFromListWithIndexOutOfRange() {
        String userId = "1";
        String listName = "want-to-read";
        int indexOutOfRange = BOOKSHELFS_JSON.length();

        assertThrows(BookNotInListException.class, () -> storage.removeFromList(userId, listName, indexOutOfRange),
                "Expected BookNotInListException when removing book with index outside list range");
    }

    @Test
    public void testRemoveFromListForNonexistentUser() {
        String userId = "nonexistent-user";
        String listName = "want-to-read";
        int indexToRemove = 0;

        assertThrows(RuntimeException.class, () -> storage.removeFromList(userId, listName, indexToRemove),
                "Expected RuntimeException when passing invalid id");
    }

    @Test
    public void testAddFriendSuccessfully() throws Exception {
        storage.addFriend("2", "user3");
        assertTrue(storage.getUserFriends("2").contains("user3"),
                "Expected correct list of friends after adding friend");
    }

    @Test
    public void testAddFriendForNonexistentUser() {
        assertThrows(RuntimeException.class, () -> storage.addFriend("nonexistent-user", "user2"),
                "Expected RuntimeException when passing invalid id");
    }

    @Test
    public void testAddFriendForNonexistentFriend() {
        assertThrows(UserDoesntExistException.class, () -> storage.addFriend("1", "nonexistent-friend"),
                "Expected UserDoesntExistException when passing username that doesn't exist");
    }

    @Test
    public void testAddFriendDuplicateFriend() throws Exception {
        storage.addFriend("1", "user2");
        List<String> userFriendsSnapshot = storage.getUserFriends("1");
        storage.addFriend("1", "user2");
        assertEquals(userFriendsSnapshot, storage.getUserFriends("1"),
                "Expected to not have duplicate friends");
    }

    @Test
    public void testRecommendBookSuccessfully()  {
        storage.recommendBook("1", BOOK2);
        assertTrue(storage.getUserRecommendations("1").contains(BOOK2),
                "Expected book to be added to recommended list.");
    }

    @Test
    public void testRecommendBookDuplicateRecommendation() {
        storage.recommendBook("2", BOOK2);
        storage.recommendBook("2", BOOK2);
        assertEquals(1, storage.getUserRecommendations("2").size(),
                "Expected not to have duplicate recommended books");
    }

    @Test
    public void testRecommendBookForNonexistentUser() {
        assertThrows(RuntimeException.class, () -> storage.recommendBook("nonexistent-user", BOOK2),
                "Expected RuntimeException when passing invalid id");
    }

    @Test
    public void testGetFriendsRecommendationsSuccessfully() {
        Map<String, List<Book>> friendsRecommendations = storage.getFriendsRecommendations("1");
        assertTrue(friendsRecommendations.containsKey("user2"),
                "Expected correct map of friends and recommended books");
        assertTrue(friendsRecommendations.containsKey("user3"),
                "Expected correct map of friends and recommended books");
        assertEquals(0, friendsRecommendations.get("user2").size(),
                "Expected correct lists for friends' recommendations");
        assertEquals(1, friendsRecommendations.get("user3").size(),
                "Expected correct lists for friends' recommendations");
    }

    @Test
    public void testGetFriendsRecommendationsForUserWithNoFriends() {
        Map<String, List<Book>> friendsRecommendations = storage.getFriendsRecommendations("2");
        assertTrue(friendsRecommendations.isEmpty(),
                "Expected empty map for user with no friends");
    }

    @Test
    public void testGetUserRecommendationsSuccessfully() {
        List<Book> userRecommendations = storage.getUserRecommendations("1");
        assertEquals(1, userRecommendations.size(), "Expected correct list of recommendations");
        assertTrue(userRecommendations.contains(BOOK1), "Expected correct list of recommendations");
    }

    @Test
    public void testGetUserRecommendationsForUserWithNoRecommendations() {
        List<Book> userRecommendations = storage.getUserRecommendations("2");
        assertTrue(userRecommendations.isEmpty(), "Expected empty list for user with no recommendations");
    }

    @Test
    public void testGetUserRecommendationsForNonexistentUser() {
        assertThrows(RuntimeException.class, () -> storage.getUserRecommendations("nonexistent-user"),
                "Expeted RuntimeException when getting user that doesn't exist");
    }
}
