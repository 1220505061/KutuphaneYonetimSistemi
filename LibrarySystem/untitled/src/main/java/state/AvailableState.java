package state;

import model.Book;

public class AvailableState implements BookState {
    @Override
    public void borrowBook(Book book) {
        System.out.println("Kitap ödünç veriliyor...");
        book.setState(new LoanedState());
    }

    @Override
    public void returnBook(Book book) {
        System.out.println("Kitap zaten kütüphanede!");
    }
}