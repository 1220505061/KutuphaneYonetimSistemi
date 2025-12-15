package state;

import model.Book;

public class AvailableState implements BookState {
    @Override
    public void borrowBook(Book book) {
        System.out.println("İşlem Başarılı: Kitap ödünç verildi.");
        book.setStatus("LOANED");
        book.setState(new LoanedState());
    }
    @Override
    public void returnBook(Book book) {
        System.out.println("Hata: Bu kitap zaten kütüphanede, iade alınamaz!");
    }
}