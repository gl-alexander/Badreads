package bg.sofia.uni.fmi.mjt.goodreads.storage;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.exception.BookNotInListException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.InvalidCredentials;
import bg.sofia.uni.fmi.mjt.goodreads.exception.ListNameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.ListNameDoesntExistException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.UserDoesntExistException;
import bg.sofia.uni.fmi.mjt.goodreads.exception.UsernameAlreadyExistsException;

import java.util.List;
import java.util.Map;

public interface Storage {
    boolean exists(String userId);

    String register(String username, String password)
            throws UsernameAlreadyExistsException, ListNameAlreadyExistsException;

    String login(String username, String password) throws InvalidCredentials;

    List<Book> getList(String userId, String listName) throws ListNameDoesntExistException;

    void createList(String userId, String listName) throws ListNameAlreadyExistsException;

    void removeList(String userId, String listName) throws ListNameDoesntExistException;

    void addToList(String userId, String listName, Book book) throws ListNameDoesntExistException;

    void removeFromList(String userId, String listName, int index)
            throws ListNameDoesntExistException, BookNotInListException;

    void addFriend(String userId, String friendUsername) throws UserDoesntExistException;

    List<String> getUserFriends(String userId);

    void recommendBook(String userId, Book book);

    Map<String, List<Book>> getFriendsRecommendations(String userId);

    List<Book> getUserRecommendations(String userId);
}