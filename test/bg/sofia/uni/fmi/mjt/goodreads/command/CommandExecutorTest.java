package bg.sofia.uni.fmi.mjt.goodreads.command;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.book.BookDetails;
import bg.sofia.uni.fmi.mjt.goodreads.book.BookRepository;
import bg.sofia.uni.fmi.mjt.goodreads.book.BookRequest;
import bg.sofia.uni.fmi.mjt.goodreads.exception.APIResponseException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.BookNotInListException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidCredentials;
import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidRequestException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.ListNameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.ListNameDoesntExistException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.UserDoesntExistException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.UsernameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.goodreads.storage.Storage;
import bg.sofia.uni.fmi.mjt.goodreads.user.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandExecutorTest {
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: \"%s\" expects %d arguments. Example: \"%s\"";

    private static final Book BOOK1 = new Book("1", "Title1", List.of("Author1"));
    private static final BookDetails BD_1 = new BookDetails(BOOK1.id(), BOOK1.title(), BOOK1.authors(),
            "Desc", 100, 2000, List.of("Category1"), 5, 10);
    private static final Book BOOK2 = new Book("2", "Title2", List.of("Author3"));
    private static final Book BOOK3 = new Book("3", "Title3", List.of("Author3"));
    private static final List<Book> BOOKS_LIST = List.of(BOOK1, BOOK2, BOOK3);
    private static final String PRINTED_LIST = "0: " + BOOK1 + System.lineSeparator() +
            "1: " + BOOK2 + System.lineSeparator() +
            "2: " + BOOK3;

    private static final String EXPECTED_HELP_DESCRIPTION =
            "Command Descriptions:" + System.lineSeparator() +
                    "login <username> <password>: Log in to the system." + System.lineSeparator() +
                    "register <username> <password>: Register a new user." + System.lineSeparator() +
                    "logout: Log out from the system." + System.lineSeparator() +
                    "search-title <title>: Search for books by title." + System.lineSeparator() +
                    "search-author <author>: Search for books by author." + System.lineSeparator() +
                    "search-title-author <title> <author>: Search for books by both title and author." +
                    System.lineSeparator() +
                    "select <book_id>: Select a book from the search list." + System.lineSeparator() +
                    "deselect: Deselect the currently selected book." + System.lineSeparator() +
                    "add-book <list_name>: Add the selected book to a list." + System.lineSeparator() +
                    "next-page: View the next page of search results." + System.lineSeparator() +
                    "prev-page: View the previous page of search results." + System.lineSeparator() +
                    "add-friend <friend_username>: Add a friend to your network." + System.lineSeparator() +
                    "create-list <list_name>: Create a new list for organizing books." + System.lineSeparator() +
                    "remove-list <list_name>: Remove a list from your collections." + System.lineSeparator() +
                    "view-list <list_name>: View the contents of a specific list." + System.lineSeparator() +
                    "remove-book <list_name> <index>: Remove book at given index from a list." +
                    System.lineSeparator() +
                    "recommend-book: Recommend the selected book to friends." + System.lineSeparator() +
                    "view-friends-recommended: View books recommended by your friends." + System.lineSeparator() +
                    "view-user-recommended: View books you have recommended." + System.lineSeparator();

    @Mock
    private BookRepository bookRepository;
    @Mock
    private Storage storage;

    private CommandExecutor commandExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        commandExecutor = new CommandExecutor(storage, bookRepository);
    }

    @Test
    void testRegisterCorrect() throws UsernameAlreadyExistsException, ListNameAlreadyExistsException {
        Command cmd = new Command("register", new String[]{"username", "password"});
        Session session = new Session();
        when(storage.register("username", "password")).thenReturn("id");
        String response = commandExecutor.execute(cmd, session);
        assertEquals("Registered new user with the ID id" + System.lineSeparator() + "You are now logged in.",
                response,
                "Expected correct response when successfully executing command");
        assertEquals(session.getLoggedUserId(), "id",
                "Expected register to save logged user id to session");
    }

    @Test
    void testRegisterIncorrect() throws UsernameAlreadyExistsException, ListNameAlreadyExistsException {
        Command incorrectCommand = new Command("register", new String[]{"username"});
        Command correctCommand = new Command("register", new String[]{"username", "password"});
        Session session = new Session();

        String response = commandExecutor.execute(incorrectCommand, session);
        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "register", 2, "register <username> <password>"),
                response,
                "Expected valid response when passing fewer arguments");
        session.setLoggedUserId("randomId");
        assertEquals("You are already logged in the system", commandExecutor.execute(correctCommand, session),
                "Expected an appropriate message when user is already logged in the system");

        session.resetSession();
        UsernameAlreadyExistsException e = new UsernameAlreadyExistsException("msg");
        when(storage.register("username", "password"))
                .thenThrow(e);
        assertEquals("msg", commandExecutor.execute(correctCommand, session),
                "Expected an error message when registering with a taken username");
    }

    @Test
    void testLoginCorrect() throws InvalidCredentials {
        Command cmd = new Command("login", new String[]{"username", "password"});
        Session session = new Session();
        when(storage.login("username", "password")).thenReturn("id");
        String response = commandExecutor.execute(cmd, session);
        assertEquals("Logged in", response,
                "Expected correct response when successfully executing command");
        assertEquals(session.getLoggedUserId(), "id",
                "Expected login to save logged user id to session");
    }

    @Test
    void testLoginIncorrect() throws InvalidCredentials {
        Command incorrectCommand = new Command("login", new String[]{"username"});
        Command correctCommand = new Command("login", new String[]{"username", "password"});
        Session session = new Session();

        String response = commandExecutor.execute(incorrectCommand, session);
        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "login", 2, "login <username> <password>"),
                response,
                "Expected valid response when passing fewer arguments");
        session.setLoggedUserId("randomId");
        assertEquals("You are already logged in the system", commandExecutor.execute(correctCommand, session),
                "Expected an appropriate message when user is already logged in the system");

        session.resetSession();
        InvalidCredentials e = new InvalidCredentials("msg");
        when(storage.login("username", "password")).thenThrow(e);
        assertEquals("msg", commandExecutor.execute(correctCommand, session),
                "Expected an error message when logging in with invalid credentials");
    }

    @Test
    void testLogoutCorrect() {
        Command cmd = new Command("logout", new String[]{});
        Session session = new Session();
        session.setLoggedUserId("randomId");
        String response = commandExecutor.execute(cmd, session);
        assertEquals("Logged out", response,
                "Expected correct response when successfully executing command");
        assertTrue(session.getLoggedUserId().isEmpty(),
                "Expected to reset session after logout");
    }

    @Test
    void testLogoutIncorrect() {
        Command incorrectCommand = new Command("logout", new String[]{"username"});
        Command correctCommand = new Command("logout", new String[]{});
        Session session = new Session();

        String response = commandExecutor.execute(incorrectCommand, session);
        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "logout", 0, "logout"),
                response,
                "Expected valid response when passing incorrect number of arguments");
        assertEquals("You are not logged in the system", commandExecutor.execute(correctCommand, session),
                "Expected an appropriate message when user is not logged in the system");
    }

    @Test
    void testSearchTitleCorrect() throws URISyntaxException, APIResponseException {
        Command cmd = new Command("search-title", new String[]{"title"});
        Session session = new Session();
        when(bookRepository.searchByRequest(any())).thenReturn(BOOKS_LIST);

        String response = commandExecutor.execute(cmd, session);
        assertEquals(PRINTED_LIST, response,
                "Expected correct response when successfully executing command");
        assertEquals("title", session.getLastRequest().getTitle(),
                "Expected to save title to session");
        assertEquals(BOOKS_LIST, session.getDisplayedBooks(),
                "Expected to save list of books to session");
    }

    @Test
    void testSearchTitleIncorrect() throws InvalidRequestException, URISyntaxException, APIResponseException {
        Command incorrectCommand = new Command("search-title", new String[]{"title", "another"});
        Command correctCommand = new Command("search-title", new String[]{"title"});
        Session session = new Session();

        String response = commandExecutor.execute(incorrectCommand, session);
        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "search-title", 1,
                        "search-title \"<book-title>\""),
                response,
                "Expected valid response when passing incorrect number of arguments");
        APIResponseException e = new APIResponseException("msg");
        when(bookRepository.searchByRequest(any())).thenThrow(e);
        assertEquals("Error occurred while processing request: msg",
                commandExecutor.execute(correctCommand, session),
                "Expected error message when there is an API error");
    }

    @Test
    void testSearchAuthorCorrect() throws URISyntaxException, APIResponseException {
        Command cmd = new Command("search-author", new String[]{"author"});
        Session session = new Session();
        when(bookRepository.searchByRequest(any())).thenReturn(BOOKS_LIST);

        String response = commandExecutor.execute(cmd, session);
        assertEquals(PRINTED_LIST, response,
                "Expected correct response when successfully executing command");
        assertEquals("author", session.getLastRequest().getAuthor(),
                "Expected to save author to session");
        assertEquals(BOOKS_LIST, session.getDisplayedBooks(),
                "Expected to save list of books to session");
    }

    @Test
    void testSearchAuthorIncorrect() throws URISyntaxException, APIResponseException {
        Command incorrectCommand = new Command("search-author", new String[]{"author", "another"});
        Command correctCommand = new Command("search-author", new String[]{"author"});
        Session session = new Session();

        String response = commandExecutor.execute(incorrectCommand, session);
        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "search-author", 1,
                        "search-author \"<book-author>\""),
                response,
                "Expected valid response when passing incorrect number of arguments");
        APIResponseException e = new APIResponseException("msg");
        when(bookRepository.searchByRequest(any())).thenThrow(e);
        assertEquals("Error occurred while processing request: msg",
                commandExecutor.execute(correctCommand, session),
                "Expected error message when there is an API error");
    }

    @Test
    void testSearchTitleAndAuthorCorrect() throws URISyntaxException, APIResponseException {
        Command cmd = new Command("search-title-author", new String[]{"title", "author"});
        Session session = new Session();
        when(bookRepository.searchByRequest(any())).thenReturn(BOOKS_LIST);

        String response = commandExecutor.execute(cmd, session);
        assertEquals(PRINTED_LIST, response,
                "Expected correct response when successfully executing command");
        assertEquals("title", session.getLastRequest().getTitle(),
                "Expected to save title to session");
        assertEquals("author", session.getLastRequest().getAuthor(),
                "Expected to save author to session");
        assertEquals(BOOKS_LIST, session.getDisplayedBooks(),
                "Expected to save list of books to session");
    }

    @Test
    void testSearchTitleAndAuthorIncorrect() throws URISyntaxException, APIResponseException {
        Command correctCommand = new Command("search-title-author", new String[]{"title", "author"});
        Command incorrectCommand = new Command("search-title-author", new String[]{"incorrect"});
        Session session = new Session();

        String response = commandExecutor.execute(incorrectCommand, session);
        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "search-title-author", 2,
                        "search-title-author \"<book-title>\" \"<book-author>\""),
                response,
                "Expected valid response when passing incorrect number of arguments");
        APIResponseException e = new APIResponseException("msg");
        when(bookRepository.searchByRequest(any())).thenThrow(e);
        assertEquals("Error occurred while processing request: msg",
                commandExecutor.execute(correctCommand, session),
                "Expected error message when there is an API error");
    }

    @Test
    void testGetNextPageCorrect() throws URISyntaxException, APIResponseException, InvalidRequestException {
        Command cmd = new Command("next-page", new String[]{});
        Session session = new Session();
        session.setCurrentPage(0);
        session.setLastRequest(BookRequest.builder().setTitle("title").build());
        session.setDisplayedBooks(List.of(BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1));

        when(bookRepository.searchByRequest(any(), anyInt())).thenReturn(List.of(BOOK3));

        String response = commandExecutor.execute(cmd, session);
        assertEquals("10: " + BOOK3, response,
                "Expected next page of books to be printed correctly when executing next-page");
        assertEquals(1, session.getCurrentPage(),
                "Expected to increment current page in session");
        assertEquals(List.of(BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK3),
                session.getDisplayedBooks(),
                "Expected to save extra books to session");
    }

    @Test
    void testGetNextPageCorrectLoadedBooks() throws InvalidRequestException {
        Command cmd = new Command("next-page", new String[]{});
        Session session = new Session();
        session.setCurrentPage(0);
        session.setLastRequest(BookRequest.builder().setTitle("title").build());
        session.setDisplayedBooks(List.of(BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK3));

        String response = commandExecutor.execute(cmd, session);
        assertEquals("10: " + BOOK3, response,
                "Expected next page of books to be printed correctly when executing next-page");
        assertEquals(1, session.getCurrentPage(),
                "Expected to increment current page in session");
        assertEquals(List.of(BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK3),
                session.getDisplayedBooks(),
                "Expected to save extra books to session");
    }

    @Test
    void testGetNextPageIncorrect() throws URISyntaxException, APIResponseException, InvalidRequestException {
        Command correctCommand = new Command("next-page", new String[]{});
        Command incorrectCommand = new Command("next-page", new String[]{"incorrect"});
        Session session = new Session();

        String response = commandExecutor.execute(incorrectCommand, session);
        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "next-page", 0,
                        "next-page"),
                response,
                "Expected valid response when passing incorrect number of arguments");
        String noSearchResponse = commandExecutor.execute(correctCommand, session);
        assertEquals("You haven't searched for a book yet", noSearchResponse,
                "Expected an error message when no previous search exists");
        session.setLastRequest(BookRequest.builder().setTitle("title").build());
        session.setCurrentPage(0);
        session.setDisplayedBooks(Collections.emptyList());
        when(bookRepository.searchByRequest(any(), anyInt())).thenReturn(null);
        String noBooksResponse = commandExecutor.execute(correctCommand, session);
        assertEquals("No more books match the search", noBooksResponse,
                "Expected a message when there are no more books to show");
        assertEquals(0, session.getCurrentPage(),
                "Expected session current page to remain the same after invalid request");
        APIResponseException e = new APIResponseException("msg");
        when(bookRepository.searchByRequest(any(), anyInt())).thenThrow(e);
        assertEquals("Error executing request: msg",
                commandExecutor.execute(correctCommand, session),
                "Expected error message when there is an API error");
    }

    @Test
    void testGetPrevPageCorrect() throws URISyntaxException, APIResponseException, InvalidRequestException {
        Command cmd = new Command("prev-page", new String[]{});
        Session session = new Session();
        session.setCurrentPage(1);
        session.setLastRequest(BookRequest.builder().setTitle("title").build());
        session.setDisplayedBooks(List.of(BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK1, BOOK3));

        when(bookRepository.searchByRequest(any(), anyInt())).thenReturn(List.of(BOOK3));

        String response = commandExecutor.execute(cmd, session);
        assertEquals("0: " + BOOK1 + System.lineSeparator() +
                        "1: " + BOOK1 + System.lineSeparator() +
                        "2: " + BOOK1 + System.lineSeparator() +
                        "3: " + BOOK1 + System.lineSeparator() +
                        "4: " + BOOK1 + System.lineSeparator() +
                        "5: " + BOOK1 + System.lineSeparator() +
                        "6: " + BOOK1 + System.lineSeparator() +
                        "7: " + BOOK1 + System.lineSeparator() +
                        "8: " + BOOK1 + System.lineSeparator() +
                        "9: " + BOOK1 + System.lineSeparator()
                ,
                response,
                "Expected next page of books to be printed correctly when executing next-page");
        assertEquals(0, session.getCurrentPage(),
                "Expected to decrement current page in session");
    }

    @Test
    void testGetPrevPageIncorrect() throws InvalidRequestException {
        Command correctCommand = new Command("prev-page", new String[]{});
        Command incorrectCommand = new Command("prev-page", new String[]{"incorrect"});
        Session session = new Session();

        String response = commandExecutor.execute(incorrectCommand, session);
        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "prev-page", 0,
                        "prev-page"),
                response,
                "Expected valid response when passing incorrect number of arguments");
        String noSearchResponse = commandExecutor.execute(correctCommand, session);
        assertEquals("You haven't searched for a book yet", noSearchResponse,
                "Expected an error message when no previous search exists");
        session.setLastRequest(BookRequest.builder().setTitle("title").build());
        session.setCurrentPage(0);
        session.setDisplayedBooks(Collections.emptyList());
        String noBooksResponse = commandExecutor.execute(correctCommand, session);
        assertEquals("No previous page", noBooksResponse,
                "Expected a message when there are no more books to show");
        assertEquals(0, session.getCurrentPage(),
                "Expected session current page to remain the same after invalid request");
    }

    @Test
    public void testSelectBookSuccess() throws URISyntaxException, APIResponseException {
        Command cmd = new Command("select", new String[]{"0"});
        Session session = new Session();
        session.setDisplayedBooks(BOOKS_LIST);

        when(bookRepository.getBookInfo("1")).thenReturn(BD_1);
        String result = commandExecutor.execute(cmd, session);

        assertEquals(BD_1.toString(), result,
                "Expected correct book details to be printed when selecting a book");
        assertNotNull(session.getSelectedBook(),
                "Expected session to have selected book");
        assertEquals(BOOK1, session.getSelectedBook(),
                "Expected correct selected book");
    }

    @Test
    public void testSelectBookAlreadySelected() {
        Session session = new Session();
        session.setSelectedBook(BOOK1);
        session.setDisplayedBooks(BOOKS_LIST);

        String result = commandExecutor.execute(new Command("select", new String[]{"0"}), session);

        assertEquals("You already have a book selected. To deselect it use: deselect", result,
                "Expected a correct message when session already has a book selected");
        assertEquals(BOOK1, session.getSelectedBook(),
                "Expected session to remain with selected book");
    }

    @Test
    public void testSelectBookInvalid() {
        Command incorrect = new Command("select", new String[]{"invalid", "count"});
        Command correct = new Command("select", new String[]{"5"});
        Session session = new Session();

        assertEquals("You haven't made any book requests", commandExecutor.execute(correct, session),
                "Expected error message when no selection has been made");

        session.setDisplayedBooks(Collections.emptyList());
        assertEquals("Your last request was empty", commandExecutor.execute(correct, session),
                "Expected error message when displayed result is empty");

        session.setDisplayedBooks(BOOKS_LIST);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "select", 1, "select <book number from list>"),
                commandExecutor.execute(incorrect, session),
                "Expected correct message for invalid number of arguments");

        String result = commandExecutor.execute(correct, session);

        assertEquals("Index 5 is out of bounds. Displayed books are in range [0, 2]", result,
                "Expected correct message when given index is out of bounds for the displayed books");
        assertNull(session.getSelectedBook(),
                "No book should be selected if given false index");
    }

    @Test
    public void testSelectBookException() throws URISyntaxException, APIResponseException {
        Session session = new Session();
        session.setDisplayedBooks(BOOKS_LIST);

        String result = commandExecutor.execute(new Command("select", new String[]{"invalidIndex"}), session);

        assertEquals("Invalid index passed, please enter a valid number", result,
                "Expected correct message when given index is not a valid number");
        APIResponseException e = new APIResponseException("msg");
        when(bookRepository.getBookInfo("1")).thenThrow(e);
        session.setDisplayedBooks(BOOKS_LIST);
        session.setCurrentPage(0);
        String exceptionResult = commandExecutor.execute(new Command("select", new String[]{"0"}), session);
        assertEquals("Error occurred while processing request: msg", exceptionResult,
                "Expected correct error message when API error occurs");
        assertNull(session.getSelectedBook(),
                "No book should be selected if given false index");
    }

    @Test
    public void testDeselectSuccess() {
        Session session = new Session();
        session.setSelectedBook(BOOK1);

        String result = commandExecutor.execute(new Command("deselect", new String[]{}), session);

        assertEquals("Removed selection", result);
        assertNull(session.getSelectedBook(), "Expected to remove selected book from session");
    }

    @Test
    public void testDeselectNotSelected() {
        Session session = new Session();

        String result = commandExecutor.execute(new Command("deselect", new String[]{}), session);

        assertEquals("You haven't selected a book", result);
    }

    @Test
    public void testDeselectInvalidArgsCount() {
        Session session = new Session();
        session.setSelectedBook(BOOK1);

        String result = commandExecutor.execute(new Command("deselect", new String[]{"arg1"}), session);

        assertEquals("Invalid count of arguments: \"deselect\" expects 0 arguments. Example: \"deselect\"", result);
        assertNotNull(session.getSelectedBook(), "Selected book shouldn't be removed when executing incorrect command");
    }

    @Test
    public void testAddToListSuccess() throws ListNameDoesntExistException {
        Session session = new Session();
        session.setSelectedBook(BOOK1);
        session.setLoggedUserId("user1");

        String result = commandExecutor.execute(new Command("add-book", new String[]{"list1"}), session);

        assertEquals("Successfully added Title1 to list1", result,
                "Expected message when successfully adding a book to list");
        verify(storage).addToList("user1", "list1", BOOK1);
    }

    @Test
    public void testAddToListNotLoggedIn() throws ListNameDoesntExistException {
        Session session = new Session();
        session.setSelectedBook(BOOK1);

        String result = commandExecutor.execute(new Command("add-book", new String[]{"list1"}), session);

        assertEquals("You aren't logged in the system", result,
                "Expected error message when no user is logged in the system");
        verify(storage, never()).addToList("user1", "list1", BOOK1);
    }

    @Test
    public void testAddToListNoSelectedBook() throws ListNameDoesntExistException {
        Session session = new Session();
        session.setLoggedUserId("user1");

        String result = commandExecutor.execute(new Command("add-book", new String[]{"list1"}), session);

        assertEquals("You haven't selected a book, select a book by first searching the Book Repository", result,
                "Expected correct message when no book has been selected");
        verify(storage, never()).addToList("user1", "list1", BOOK1);
    }

    @Test
    public void testAddToListInvalidArgsCount() throws ListNameDoesntExistException {
        Session session = new Session();
        session.setLoggedUserId("user1");
        session.setSelectedBook(BOOK1);

        String result = commandExecutor.execute(new Command("add-book", new String[]{"list1", "extraArg"}), session);

        assertEquals("Invalid count of arguments: \"add-book\" expects 1 arguments. Example: \"add-book <list-name>\"",
                result,
                "Expected a message when passed arguments count is invalid");
        verify(storage, never()).addToList("user1", "list1", BOOK1);
    }

    @Test
    public void testAddToListListNameDoesntExist() throws ListNameDoesntExistException {
        Session session = new Session();
        session.setLoggedUserId("user1");
        session.setSelectedBook(BOOK1);
        doThrow(new ListNameDoesntExistException("List does not exist")).when(storage)
                .addToList(anyString(), anyString(), any());

        String result = commandExecutor.execute(new Command("add-book", new String[]{"nonexistentList"}), session);

        assertEquals("List does not exist", result,
                "Expected error output when list with that name doesn't exist");
    }

    @Test
    public void testAddFriendSuccess() throws UserDoesntExistException {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(new Command("add-friend", new String[]{"friend123"}), session);

        assertEquals("Friend added successfully", result);
        verify(storage).addFriend("user123", "friend123");
    }

    @Test
    public void testAddFriendNotLoggedIn() throws UserDoesntExistException {
        Session session = new Session();
        String result = commandExecutor.execute(new Command("add-friend", new String[]{"friend123"}), session);

        assertEquals("You aren't logged in the system", result);
        verify(storage, never()).addFriend(anyString(), anyString());
    }

    @Test
    public void testAddFriendInvalidArgsCount() throws UserDoesntExistException {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(new Command("add-friend", new String[]{"friend123", "extraArg"}),
                session);

        assertEquals("Invalid count of arguments: \"add-friend\" expects 1 arguments. " +
                "Example: \"add-friend <friend-username>\"", result);
        verify(storage, never()).addFriend(anyString(), anyString());
    }

    @Test
    public void testAddFriendUserDoesntExist() throws UserDoesntExistException {
        Session session = new Session();
        session.setLoggedUserId("user123");
        doThrow(new UserDoesntExistException("User does not exist")).when(storage)
                .addFriend(anyString(), anyString());

        String result = commandExecutor.execute(new Command("add-friend", new String[]{"nonexistentUser"}), session);

        assertEquals("User does not exist", result);
        verify(storage).addFriend("user123", "nonexistentUser");
    }

    @Test
    public void testCreateListSuccess() throws ListNameAlreadyExistsException {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(new Command("create-list", new String[]{"myList"}), session);

        assertEquals("List created successfully", result);
        verify(storage).createList("user123", "myList");
    }

    @Test
    public void testCreateListNotLoggedIn() throws ListNameAlreadyExistsException {
        Session session = new Session();
        String result = commandExecutor.execute(new Command("create-list", new String[]{"myList"}), session);

        assertEquals("You aren't logged in the system", result);
        verify(storage, never()).createList(anyString(), anyString());
    }

    @Test
    public void testCreateListInvalidArgsCount() throws ListNameAlreadyExistsException {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(new Command("create-list", new String[]{"myList", "extraArg"}),
                session);

        assertEquals("Invalid count of arguments: \"create-list\" expects 1 arguments. " +
                "Example: \"create-list <list-name>\"", result);
        verify(storage, never()).createList(anyString(), anyString());
    }

    @Test
    public void testCreateListAlreadyExists() throws ListNameAlreadyExistsException {
        Session session = new Session();
        session.setLoggedUserId("user123");
        doThrow(new ListNameAlreadyExistsException("List already exists")).when(storage)
                .createList(anyString(), anyString());

        String result = commandExecutor.execute(new Command("create-list", new String[]{"existingList"}), session);

        assertEquals("List already exists", result);
        verify(storage).createList("user123", "existingList");
    }

    @Test
    public void testRemoveListSuccess() throws ListNameDoesntExistException {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(new Command("remove-list", new String[]{"myList"}), session);

        assertEquals("List removed successfully", result);
        verify(storage).removeList("user123", "myList");
    }

    @Test
    public void testRemoveListNotLoggedIn() throws ListNameDoesntExistException {
        Session session = new Session();
        String result = commandExecutor.execute(new Command("remove-list", new String[]{"myList"}), session);

        assertEquals("You aren't logged in the system", result);
        verify(storage, never()).removeList(anyString(), anyString());
    }

    @Test
    public void testRemoveListInvalidArgsCount() throws ListNameDoesntExistException {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(new Command("remove-list", new String[]{"myList", "extraArg"}),
                session);

        assertEquals("Invalid count of arguments: \"remove-list\" expects 1 arguments. " +
                "Example: \"remove-list <list-name>\"", result);
        verify(storage, never()).removeList(anyString(), anyString());
    }

    @Test
    public void testRemoveListDoesntExist() throws ListNameDoesntExistException {
        Session session = new Session();
        session.setLoggedUserId("user123");
        doThrow(new ListNameDoesntExistException("List doesn't exist")).when(storage)
                .removeList(anyString(), anyString());

        String result = commandExecutor.execute(new Command("remove-list", new String[]{"nonexistentList"}), session);

        assertEquals("List doesn't exist", result);
        verify(storage).removeList("user123", "nonexistentList");
    }

    @Test
    public void testRemoveBookSuccess() throws ListNameDoesntExistException, BookNotInListException {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(new Command("remove-book", new String[]{"myList", "0"}), session);

        assertEquals("Book removed successfully", result);
        verify(storage).removeFromList("user123", "myList", 0);
    }

    @Test
    public void testRemoveBookNotLoggedIn() throws ListNameDoesntExistException, BookNotInListException {
        Session session = new Session();
        String result = commandExecutor.execute(new Command("remove-book", new String[]{"myList", "0"}), session);

        assertEquals("You aren't logged in the system", result);
        verify(storage, never()).removeFromList(anyString(), anyString(), anyInt());
    }

    @Test
    public void testRemoveBookInvalidArgsCount() throws ListNameDoesntExistException, BookNotInListException {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(new Command("remove-book", new String[]{"myList"}), session);

        assertEquals("Invalid count of arguments: \"remove-book\" expects 2 arguments. " +
                "Example: \"remove-book <list-name> <book index in list>\"", result);
        verify(storage, never()).removeFromList(anyString(), anyString(), anyInt());
    }

    @Test
    public void testRemoveBookNumberFormatException() throws ListNameDoesntExistException, BookNotInListException {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(new Command("remove-book", new String[]{"myList", "notANumber"}),
                session);

        assertEquals("Invalid index, please enter a valid number", result);
        verify(storage, never()).removeFromList(anyString(), anyString(), anyInt());
    }

    @Test
    public void testRemoveBookBookNotInList() throws BookNotInListException, ListNameDoesntExistException {
        Session session = new Session();
        session.setLoggedUserId("user123");
        doThrow(new BookNotInListException("Book not in list")).when(storage)
                .removeFromList(anyString(), anyString(), anyInt());

        String result = commandExecutor.execute(new Command("remove-book", new String[]{"myList", "0"}), session);

        assertEquals("Book not in list", result);
        verify(storage).removeFromList("user123", "myList", 0);
    }

    @Test
    public void testRemoveBookListNameDoesntExist() throws ListNameDoesntExistException, BookNotInListException {
        Command correct = new Command("remove-book", new String[]{"nonexistentList", "0"});
        Session session = new Session();
        session.setLoggedUserId("user123");
        doThrow(new ListNameDoesntExistException("List doesn't exist")).when(storage)
                .removeFromList(anyString(), anyString(), anyInt());

        String result = commandExecutor.execute(correct, session);

        assertEquals("List doesn't exist", result);
        verify(storage).removeFromList("user123", "nonexistentList", 0);
    }

    @Test
    public void testRecommendBookSuccess() throws URISyntaxException, APIResponseException {
        Session session = new Session();
        session.setLoggedUserId("user123");
        session.setSelectedBook(BOOK1);
        when(bookRepository.getBookInfo("1")).thenReturn(BD_1);

        String result = commandExecutor.execute(new Command("recommend-book", new String[]{}), session);

        assertEquals("Book added to recommendations", result);
        verify(storage).recommendBook("user123", BOOK1);
    }

    @Test
    public void testRecommendBookNotLoggedIn() {
        Session session = new Session();
        String result = commandExecutor.execute(new Command("recommend-book", new String[]{}), session);

        assertEquals("You aren't logged in the system", result);
        verify(storage, never()).recommendBook(anyString(), any());
    }

    @Test
    public void testRecommendBookNoSelectedBook() {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(new Command("recommend-book", new String[]{}), session);

        assertEquals("You haven't selected a book, select a book by first searching the Book Repository", result);
        verify(storage, never()).recommendBook(anyString(), any());
    }

    @Test
    public void testRecommendBookInvalidArgsCount() {
        Session session = new Session();
        session.setLoggedUserId("user123");
        session.setSelectedBook(BOOK1);

        String result = commandExecutor.execute(new Command("recommend-book", new String[]{"extraArg"}), session);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "recommend-book", 0, "recommend-book"),
                result);
        verify(storage, never()).recommendBook(anyString(), any());
    }

    @Test
    public void testViewUserRecommendedSuccess() {
        Session session = new Session();
        session.setLoggedUserId("user123");
        when(storage.getUserRecommendations("user123")).thenReturn(BOOKS_LIST);

        String result = commandExecutor.execute(new Command("view-user-recommended", new String[]{}), session);

        assertEquals(PRINTED_LIST.replace(":", ""), result);
    }

    @Test
    public void testViewUserRecommendedNotLoggedIn() {
        Session session = new Session();
        String result = commandExecutor.execute(new Command("view-user-recommended", new String[]{}), session);

        assertEquals("You aren't logged in the system", result);
        verify(storage, never()).getUserRecommendations(anyString());
    }

    @Test
    public void testViewUserRecommendedNoRecommendations() {
        Session session = new Session();
        session.setLoggedUserId("user123");
        when(storage.getUserRecommendations("user123")).thenReturn(new ArrayList<>());

        String result = commandExecutor.execute(new Command("view-user-recommended", new String[]{}), session);

        assertEquals("No recommended books", result);
    }

    @Test
    public void testViewUserRecommendedInvalidArgsCount() {
        Command incorrect = new Command("view-user-recommended", new String[]{"extraArg"});
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(incorrect, session);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                        "view-user-recommended", 0, "view-user-recommended"),
                result);
        verify(storage, never()).getUserRecommendations(anyString());
    }

    @Test
    public void testViewFriendsRecommendedSuccess() {
        Session session = new Session();
        session.setLoggedUserId("user123");

        Map<String, List<Book>> mockFriendsRecommendations = new HashMap<>();
        mockFriendsRecommendations.put("friend1", List.of(BOOK1));
        mockFriendsRecommendations.put("friend2", List.of(BOOK2));
        when(storage.getFriendsRecommendations("user123")).thenReturn(mockFriendsRecommendations);

        String result = commandExecutor.execute(new Command("view-friends-recommended", new String[]{}), session);

        String expected = "friend1 recommends " + BOOK1 + System.lineSeparator() +
                "friend2 recommends " + BOOK2 + System.lineSeparator();
        assertEquals(expected, result);
    }

    @Test
    public void testViewFriendsRecommendedNotLoggedIn() {
        Session session = new Session();
        String result = commandExecutor.execute(new Command("view-friends-recommended", new String[]{}), session);

        assertEquals("You aren't logged in the system", result);
        verify(storage, never()).getFriendsRecommendations(anyString());
    }

    @Test
    public void testViewFriendsRecommendedNoRecommendations() {
        Session session = new Session();
        session.setLoggedUserId("user123");
        when(storage.getFriendsRecommendations("user123")).thenReturn(new HashMap<>());

        String result = commandExecutor.execute(new Command("view-friends-recommended", new String[]{}), session);

        assertEquals("", result);
    }

    @Test
    public void testViewFriendsRecommendedInvalidArgsCount() {
        Session session = new Session();
        session.setLoggedUserId("user123");

        String result = commandExecutor.execute(
                new Command("view-friends-recommended", new String[]{"extraArg"}), session);

        assertEquals("Invalid count of arguments: \"view-friends-recommended\" expects 0 arguments. " +
                "Example: \"view-friends-recommended\"", result);
        verify(storage, never()).getFriendsRecommendations(anyString());
    }

    @Test
    void testUnknownCommand() {
        assertEquals("Unknown command", commandExecutor.execute(
                new Command("random", new String[]{"args"}), new Session()),
                "Expected Unknown command message when passing invalid command name"
        );
    }

    @Test
    public void testViewListWithValidArgs() throws ListNameDoesntExistException {
        Command correct = new Command("view-list", new String[]{"listName"});
        Session session = new Session();
        session.setLoggedUserId("userId");

        when(storage.getList("userId", "listName")).thenReturn(BOOKS_LIST);

        String result = commandExecutor.execute(correct, session);

        assertEquals(PRINTED_LIST.replace(":", ""), result,
                "Expected correct list output when viewing list");
    }

    @Test
    public void testViewListWithInvalidArgsCount() throws ListNameDoesntExistException {
        Command incorrect = new Command("view-list", new String[]{"listName", "invalid"});
        Session session = new Session();
        session.setLoggedUserId("logged-in-user");

        String result = commandExecutor.execute(incorrect, session);

        assertEquals("Invalid count of arguments: \"view-list\" expects 1 arguments. " +
                "Example: \"view-list <list-name>\"", result,
                "Expected error message when passing invalid number of arguments");
        verify(storage, never()).getList(anyString(), anyString());
    }

    @Test
    public void testViewListWhenNotLoggedIn() throws ListNameDoesntExistException {
        Command correct = new Command("view-list", new String[]{"listName"});
        Session session = new Session();

        String result = commandExecutor.execute(correct, session);

        assertEquals("You aren't logged in the system", result,
                "Expected error message when getting list without being logged in");
        verify(storage, never()).getList(anyString(), anyString());
    }

    @Test
    public void testViewListWithListNameDoesntExist() throws ListNameDoesntExistException {
        Command correct = new Command("view-list", new String[]{"nonexistent-list"});
        Session session = new Session();
        session.setLoggedUserId("userId");

        when(storage.getList("userId", "nonexistent-list"))
                .thenThrow(new ListNameDoesntExistException("List not found"));

        String result = commandExecutor.execute(correct, session);

        assertEquals("List not found", result,
                "Expected error message when list isn't found");
    }

    @Test
    public void testMenuCommand() {
        Session session = new Session();

        String result = commandExecutor.execute(new Command("menu", new String[]{}), session);

        assertEquals(EXPECTED_HELP_DESCRIPTION, result);
    }

    @Test
    public void testHelpCommand() {
        Session session = new Session();

        String result = commandExecutor.execute(new Command("help", new String[]{}), session);

        assertEquals(EXPECTED_HELP_DESCRIPTION, result);
    }

    @Test
    public void testInvalidCommand() {
        Session session = new Session();

        String resultHelp = commandExecutor.execute(new Command("help", new String[]{"args"}), session);
        String resultMenu = commandExecutor.execute(new Command("menu", new String[]{"args"}), session);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "help", 0, "help"), resultHelp,
                "Expected correct error message when passing invalid number of arguments");
        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "help", 0, "help"), resultMenu,
                "Expected correct error message when passing invalid number of arguments");
    }
}