package state;

import model.Book;

public interface BookState {
    void borrowBook(Book book);
    void returnBook(Book book);
}