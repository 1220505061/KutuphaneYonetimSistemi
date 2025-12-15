package state;

import model.Book;

public class LoanedState implements BookState {

    @Override
    public void borrowBook(Book book) {
        System.out.println("Hata: Bu kitap şu an başkasında! Sıraya girebilirsiniz.");
    }
    @Override
    public void returnBook(Book book) {
        System.out.println("İşlem Başarılı: Kitap iade alındı.");
        book.setStatus("AVAILABLE");
        book.setState(new AvailableState());
        book.notifyObservers();
    }
}