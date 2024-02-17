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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandExecutor {
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: \"%s\" expects %d arguments. Example: \"%s\"";

    private static final String LOGIN = "login";
    private static final String REGISTER = "register";
    private static final String LOGOUT = "logout";
    private static final String SEARCH_TITLE = "search-title";
    private static final String SEARCH_AUTHOR = "search-author";
    private static final String SEARCH_TITLE_AUTHOR = "search-title-author";
    private static final String SELECT_BOOK = "select";
    private static final String DESELECT_BOOK = "deselect";
    private static final String ADD_TO_LIST = "add-book";
    private static final String NEXT_PAGE = "next-page";
    private static final String PREV_PAGE = "prev-page";
    private static final String ADD_FRIEND = "add-friend";
    private static final String CREATE_LIST = "create-list";
    private static final String REMOVE_LIST = "remove-list";
    private static final String VIEW_LIST = "view-list";
    private static final String REMOVE_BOOK = "remove-book";
    private static final String RECOMMEND_BOOK = "recommend-book";
    private static final String VIEW_FRIENDS_RECOMMENDED = "view-friends-recommended";
    private static final String VIEW_USER_RECOMMENDED = "view-user-recommended";
    private static final String MENU = "menu";
    private static final String HELP = "help";


    private Storage storage;

    private BookRepository bookRepository;

    private Map<String, BookDetails> loadedBookDetails;

    public CommandExecutor(Storage storage, BookRepository bookRepository) {
        this.storage = storage;
        this.bookRepository = bookRepository;
        this.loadedBookDetails = new HashMap<>();
    }

    public String execute(Command cmd, Session session) {
        return switch (cmd.command()) {
            case REGISTER -> register(cmd.arguments(), session);
            case LOGIN -> login(cmd.arguments(), session);
            case LOGOUT -> logout(cmd.arguments(), session);
            case SEARCH_TITLE -> searchTitle(cmd.arguments(), session);
            case SEARCH_AUTHOR -> searchAuthor(cmd.arguments(), session);
            case SEARCH_TITLE_AUTHOR -> searchTitleAndAuthor(cmd.arguments(), session);
            case SELECT_BOOK -> selectBook(cmd.arguments(), session);
            case DESELECT_BOOK -> deselect(cmd.arguments(), session);
            case NEXT_PAGE -> nextPage(cmd.arguments(), session);
            case PREV_PAGE -> prevPage(cmd.arguments(), session);
            case ADD_TO_LIST -> addToList(cmd.arguments(), session);
            case CREATE_LIST -> createList(cmd.arguments(), session);
            case ADD_FRIEND -> addFriend(cmd.arguments(), session);
            case REMOVE_LIST -> removeList(cmd.arguments(), session);
            case REMOVE_BOOK -> removeBook(cmd.arguments(), session);
            case RECOMMEND_BOOK -> recommendBook(cmd.arguments(), session);
            case VIEW_USER_RECOMMENDED -> viewUserRecommended(cmd.arguments(), session);
            case VIEW_FRIENDS_RECOMMENDED -> viewFriendsRecommended(cmd.arguments(), session);
            case VIEW_LIST -> viewList(cmd.arguments(), session);
            case HELP, MENU -> getCommandsDescription(cmd.arguments(), session);
            default -> "Unknown command";
        };
    }

    private String getCommandsDescription(String[] args, Session session) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, HELP, 0, HELP) ;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Command Descriptions:").append(System.lineSeparator());
        sb.append("login <username> <password>: Log in to the system.").append(System.lineSeparator());
        sb.append("register <username> <password>: Register a new user.").append(System.lineSeparator());
        sb.append("logout: Log out from the system.").append(System.lineSeparator());
        sb.append("search-title <title>: Search for books by title.").append(System.lineSeparator());
        sb.append("search-author <author>: Search for books by author.").append(System.lineSeparator());
        sb.append("search-title-author <title> <author>: Search for books by both title and author.")
                .append(System.lineSeparator());
        sb.append("select <book_id>: Select a book from the search list.").append(System.lineSeparator());
        sb.append("deselect: Deselect the currently selected book.").append(System.lineSeparator());
        sb.append("add-book <list_name>: Add the selected book to a list.").append(System.lineSeparator());
        sb.append("next-page: View the next page of search results.").append(System.lineSeparator());
        sb.append("prev-page: View the previous page of search results.").append(System.lineSeparator());
        sb.append("add-friend <friend_username>: Add a friend to your network.").append(System.lineSeparator());
        sb.append("create-list <list_name>: Create a new list for organizing books.").append(System.lineSeparator());
        sb.append("remove-list <list_name>: Remove a list from your collections.").append(System.lineSeparator());
        sb.append("view-list <list_name>: View the contents of a specific list.").append(System.lineSeparator());
        sb.append("remove-book <list_name> <index>: Remove book at given index from a list.")
                .append(System.lineSeparator());
        sb.append("recommend-book: Recommend the selected book to friends.").append(System.lineSeparator());
        sb.append("view-friends-recommended: View books recommended by your friends.").append(System.lineSeparator());
        sb.append("view-user-recommended: View books you have recommended.").append(System.lineSeparator());

        return sb.toString();
    }

    private String register(String[] args, Session session) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REGISTER, 2, REGISTER + " <username> <password>");
        }
        if (!session.getLoggedUserId().isEmpty()) {
            return "You are already logged in the system";
        }

        try {
            String newId = storage.register(args[0], args[1]);
            session.setLoggedUserId(newId);
            return String.format("Registered new user with the ID %s" + System.lineSeparator() +
                    "You are now logged in.", newId);
        } catch (UsernameAlreadyExistsException | ListNameAlreadyExistsException e) {
            return e.getMessage();
        }
    }

    private String login(String[] args, Session session) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, LOGIN, 2, LOGIN + " <username> <password>");
        }
        if (!session.getLoggedUserId().isEmpty()) {
            return "You are already logged in the system";
        }
        try {
            String userId = storage.login(args[0], args[1]);
            session.setLoggedUserId(userId);
            return "Logged in";
        } catch (InvalidCredentials e) {
            return e.getMessage();
        }
    }

    private String logout(String[] args, Session session) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, LOGOUT, 0, LOGOUT);
        }
        if (session.getLoggedUserId().isEmpty()) {
            return "You are not logged in the system";
        }
        session.resetSession();
        return "Logged out";
    }

    private String printBooksResult(List<Book> books) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < books.size(); i++) {
            sb.append(i).append(" ").append(books.get(i));
            if (i < books.size() - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private String printPage(List<Book> books, int page) {
        StringBuilder sb = new StringBuilder();
        for (int i = page * BookRequest.BOOKS_PER_PAGE; i < (page + 1) * BookRequest.BOOKS_PER_PAGE; i++) {
            if (i > books.size() - 1) {
                break;
            }
            sb.append(i).append(": ").append(books.get(i));
            if (i < books.size() - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private String searchTitle(String[] args, Session session) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                    SEARCH_TITLE, 1, SEARCH_TITLE + " \"<book-title>\"");
        }

        try {
            BookRequest request = BookRequest.builder().setTitle(args[0]).build();
            List<Book> books  = new ArrayList<>(bookRepository.searchByRequest(request));

            session.setLastRequest(request);
            session.setDisplayedBooks(books);
            session.setCurrentPage(0);
            return printPage(books, session.getCurrentPage());
        } catch (InvalidRequestException | APIResponseException | URISyntaxException e) {
            return String.format("Error occurred while processing request: %s", e.getMessage());
        }
    }

    private String searchAuthor(String[] args, Session session) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SEARCH_AUTHOR, 1,
                    SEARCH_AUTHOR + " \"<book-author>\"");
        }
        try {
            BookRequest request = BookRequest.builder().setAuthor(args[0]).build();
            session.setLastRequest(request);

            List<Book> books  = new ArrayList<>(bookRepository.searchByRequest(request));
            session.setDisplayedBooks(books);
            session.setCurrentPage(0);
            return printPage(books, session.getCurrentPage());
        } catch (InvalidRequestException | APIResponseException | URISyntaxException e) {
            return String.format("Error occurred while processing request: %s", e.getMessage());
        }
    }

    private String searchTitleAndAuthor(String[] args, Session session) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SEARCH_TITLE_AUTHOR, 2,
                    SEARCH_TITLE_AUTHOR + " \"<book-title>\" \"<book-author>\"");
        }
        try {
            BookRequest request = BookRequest.builder()
                    .setTitle(args[0])
                    .setAuthor(args[1])
                    .build();
            session.setLastRequest(request);
            session.setCurrentPage(0);
            List<Book> books  = new ArrayList<>(bookRepository.searchByRequest(request));
            session.setDisplayedBooks(books);

            return printPage(books, session.getCurrentPage());
        } catch (InvalidRequestException | APIResponseException | URISyntaxException e) {
            return String.format("Error occurred while processing request: %s", e.getMessage());
        }
    }

    private String nextPage(String[] args, Session session) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, NEXT_PAGE, 0, NEXT_PAGE);
        }
        if (session.getLastRequest() == null) {
            return "You haven't searched for a book yet";
        }
        if ((session.getDisplayedBooks().size() - 1 ) / BookRequest.BOOKS_PER_PAGE > session.getCurrentPage()) {
            session.incrementCurrentPage();
            return printPage(session.getDisplayedBooks(), session.getCurrentPage());
        }
        try {
            List<Book> booksOnNextPage = bookRepository.searchByRequest(
                    session.getLastRequest(),
                    session.getCurrentPage() + 1);
            if (booksOnNextPage == null || booksOnNextPage.isEmpty()) {
                return "No more books match the search";
            }
            session.incrementCurrentPage();
            List<Book> sessionBooks = new ArrayList<>(session.getDisplayedBooks());
            sessionBooks.addAll(booksOnNextPage);
            session.setDisplayedBooks(sessionBooks);
            return printPage(session.getDisplayedBooks(), session.getCurrentPage());
        } catch (URISyntaxException | APIResponseException e) {
            return "Error executing request: " + e.getMessage();
        }
    }

    private String prevPage(String[] args, Session session) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, PREV_PAGE, 0, PREV_PAGE);
        }
        if (session.getLastRequest() == null) {
            return "You haven't searched for a book yet";
        }
        if (session.getCurrentPage() <= 0) {
            return "No previous page";
        }
        session.decrementCurrentPage();
        return printPage(session.getDisplayedBooks(), session.getCurrentPage());
    }

    private String selectBook(String[] args, Session session) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SELECT_BOOK, 1,
                    SELECT_BOOK + " <book number from list>");
        }
        if (session.getSelectedBook() != null) {
            return String.format("You already have a book selected. To deselect it use: %s", DESELECT_BOOK);
        }
        if (session.getDisplayedBooks() == null) {
            return "You haven't made any book requests";
        }
        if (session.getDisplayedBooks().isEmpty()) {
            return "Your last request was empty";
        }
        try {
            int passedIndex = Integer.parseInt(args[0]);
            if (passedIndex < 0 || passedIndex >= session.getDisplayedBooks().size()) {
                return String.format("Index %d is out of bounds. Displayed books are in range [0, %d]",
                        passedIndex, session.getDisplayedBooks().size() - 1);
            }
            Book book = session.getDisplayedBooks().get(passedIndex);
            BookDetails selectedBook = getBookDetails(book.id());
            session.setSelectedBook(book);
            return selectedBook.toString();
        } catch (APIResponseException | URISyntaxException e) {
            return String.format("Error occurred while processing request: %s", e.getMessage());
        } catch (NumberFormatException e) {
            return "Invalid index passed, please enter a valid number";
        }
    }

    private BookDetails getBookDetails(String id) throws URISyntaxException, APIResponseException {
        if (loadedBookDetails.containsKey(id)) {
            return loadedBookDetails.get(id);
        }
        loadedBookDetails.put(id, bookRepository.getBookInfo(id));
        return loadedBookDetails.get(id);
    }

    private String deselect(String[] args, Session session) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, DESELECT_BOOK, 0, DESELECT_BOOK);
        }
        if (session.getSelectedBook() == null) {
            return "You haven't selected a book";
        }
        session.setSelectedBook(null);
        return "Removed selection";
    }

    private String addToList(String[] args, Session session) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, ADD_TO_LIST, 1, ADD_TO_LIST + " <list-name>");
        }
        if (session.getLoggedUserId() == null || session.getLoggedUserId().isEmpty()) {
            return "You aren't logged in the system";
        }
        if (session.getSelectedBook() == null) {
            return "You haven't selected a book, select a book by first searching the Book Repository";
        }
        try {
            storage.addToList(session.getLoggedUserId(), args[0], session.getSelectedBook());
            return String.format("Successfully added %s to %s", session.getSelectedBook().title(), args[0]);
        } catch (ListNameDoesntExistException e) {
            return e.getMessage();
        }
    }

    private String addFriend(String[] args, Session session) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, ADD_FRIEND, 1, ADD_FRIEND + " <friend-username>");
        }
        if (session.getLoggedUserId() == null || session.getLoggedUserId().isEmpty()) {
            return "You aren't logged in the system";
        }
        try {
            storage.addFriend(session.getLoggedUserId(), args[0]);
            return "Friend added successfully";
        } catch (UserDoesntExistException e) {
            return e.getMessage();
        }
    }

    private String createList(String[] args, Session session) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, CREATE_LIST, 1, CREATE_LIST + " <list-name>");
        }
        if (session.getLoggedUserId() == null || session.getLoggedUserId().isEmpty()) {
            return "You aren't logged in the system";
        }
        try {
            storage.createList(session.getLoggedUserId(), args[0]);
            return "List created successfully";
        } catch (ListNameAlreadyExistsException e) {
            return e.getMessage();
        }
    }

    private String removeList(String[] args, Session session) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REMOVE_LIST, 1, REMOVE_LIST + " <list-name>");
        }
        if (session.getLoggedUserId() == null || session.getLoggedUserId().isEmpty()) {
            return "You aren't logged in the system";
        }
        try {
            storage.removeList(session.getLoggedUserId(), args[0]);
            return "List removed successfully";
        } catch (ListNameDoesntExistException e) {
            return e.getMessage();
        }
    }

    private String removeBook(String[] args, Session session) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REMOVE_BOOK, 2,
                    REMOVE_BOOK + " <list-name> <book index in list>");
        }
        if (session.getLoggedUserId() == null || session.getLoggedUserId().isEmpty()) {
            return "You aren't logged in the system";
        }
        try {
            storage.removeFromList(session.getLoggedUserId(), args[0], Integer.parseInt(args[1]));
            return "Book removed successfully";
        } catch (BookNotInListException | ListNameDoesntExistException e) {
            return e.getMessage();
        } catch (NumberFormatException e) {
            return "Invalid index, please enter a valid number";
        }
    }

    private String recommendBook(String[] args, Session session) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, RECOMMEND_BOOK, 0, RECOMMEND_BOOK);
        }
        if (session.getLoggedUserId() == null || session.getLoggedUserId().isEmpty()) {
            return "You aren't logged in the system";
        }
        if (session.getSelectedBook() == null) {
            return "You haven't selected a book, select a book by first searching the Book Repository";
        }
        storage.recommendBook(session.getLoggedUserId(), session.getSelectedBook());
        return "Book added to recommendations";
    }

    private String viewUserRecommended(String[] args, Session session) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, VIEW_USER_RECOMMENDED, 0, VIEW_USER_RECOMMENDED);
        }
        if (session.getLoggedUserId() == null || session.getLoggedUserId().isEmpty()) {
            return "You aren't logged in the system";
        }
        List<Book> userRecommendations = storage.getUserRecommendations(session.getLoggedUserId());
        if (userRecommendations == null || userRecommendations.isEmpty()) {
            return "No recommended books";
        }
        return printBooksResult(userRecommendations);
    }

    private String viewFriendsRecommended(String[] args, Session session) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, VIEW_FRIENDS_RECOMMENDED, 0,
                    VIEW_FRIENDS_RECOMMENDED);
        }
        if (session.getLoggedUserId() == null || session.getLoggedUserId().isEmpty()) {
            return "You aren't logged in the system";
        }
        Map<String, List<Book>> friendsRecommendation = storage.getFriendsRecommendations(session.getLoggedUserId());
        StringBuilder sb = new StringBuilder();
        for (String s : friendsRecommendation.keySet()) {
            if (friendsRecommendation.get(s).isEmpty()) {
                continue;
            }

            sb.append(s).append(" recommends ");
            sb.append(
                    friendsRecommendation.get(s).stream()
                            .map(Book::toString)
                            .collect(Collectors.joining(", "))
            );
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private String viewList(String[] args, Session session) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, VIEW_LIST, 1, VIEW_LIST + " <list-name>");
        }
        if (session.getLoggedUserId() == null || session.getLoggedUserId().isEmpty()) {
            return "You aren't logged in the system";
        }
        try {
            return printBooksResult(storage.getList(session.getLoggedUserId(), args[0]));
        } catch (ListNameDoesntExistException e) {
            return e.getMessage();
        }
    }
}