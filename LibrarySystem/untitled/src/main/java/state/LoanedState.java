package state;

import model.Book;

public class LoanedState implements BookState {
    @Override
    public void borrowBook(Book book) {
        System.out.println("HATA: Bu kitap şu an başkasında!");
    }

    @Override
    public void returnBook(Book book) {
        System.out.println("Kitap iade alındı.");
        book.setState(new AvailableState());
        book.notifyObservers(); // Bekleyenlere haber ver
    }
}