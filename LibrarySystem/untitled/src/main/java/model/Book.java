package model;

import observer.IObserver;
import state.AvailableState;
import state.BookState;
import state.LoanedState;

import java.time.LocalDate; // Tarih işlemleri için eklendi
import java.time.temporal.ChronoUnit; // Gün farkı hesaplamak için eklendi
import java.util.ArrayList;
import java.util.List;

public class Book extends LibraryItem {
    private String author;
    private String isbn;
    private String publisher;
    private String category;
    private int quantity;

    private String status;
    private BookState state;

    //  Kitabın ne zaman ödünç alındığı
    private LocalDate borrowDate;

    // Observer Listesi (Sırada bekleyen üyeler)
    private List<IObserver> observers = new ArrayList<>();

    public Book(String title, String author, String isbn, String category, String publisher, int quantity) {
        super(title);
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.publisher = publisher;
        this.quantity = quantity;
        this.status = "AVAILABLE";
        this.state = new AvailableState(); // Varsayılan durum: Müsait
        this.borrowDate = null; // Başlangıçta tarih yok
    }

    // CEZA HESAPLAMA MANTIĞI
    public long calculateFine() {
        // Eğer kitap ödünç alınmamışsa veya tarih girilmemişse ceza yok
        if (borrowDate == null) return 0;

        // Bugün ile alış tarihi arasındaki gün farkını bul
        long daysBetween = ChronoUnit.DAYS.between(borrowDate, LocalDate.now());

        // Eğer 7 günü geçtiyse ceza uygula
        if (daysBetween > 7) {
            long overdueDays = daysBetween - 7;
            return overdueDays * 5; // GÜNLÜK 5 BİRİM (TL) CEZA
        }
        return 0; // Gecikme yoksa ceza 0
    }

    //  TARİH İÇİN GETTER / SETTER
    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    // STATE PATTERN METODLARI

    public void setState(BookState state) {
        this.state = state;
    }

    public BookState getState() {
        return state;
    }

    public void borrowItem() {
        state.borrowBook(this);
    }

    public void returnItem() {
        state.returnBook(this);
    }

    // GETTER / SETTER

    public String getStatus() {
        return status;
    }

    // Veritabanından gelen String duruma göre State nesnesini de güncelliyoruz
    public void setStatus(String status) {
        this.status = status;
        if (status.equalsIgnoreCase("AVAILABLE")) {
            this.state = new AvailableState();
        } else {
            this.state = new LoanedState();
        }
    }

    //  OBSERVER (GÖZLEMCİ) METODLARI

    public void addObserver(IObserver observer) {
        observers.add(observer);
        System.out.println("Kullanıcı bekleme listesine alındı.");
    }

    public void removeObserver(IObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (IObserver observer : observers) {
            observer.update(getTitle() + " kitabı artık kütüphanede müsait!");
        }
        // Bildirim gönderdikten sonra listeyi temizliyoruz
        observers.clear();
    }

    // --- REZERVASYON KONTROLÜ ---
    public boolean isWaiting(String username) {
        for (IObserver obs : observers) {
            if (obs instanceof Member) {
                if (((Member) obs).getUsername().equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void displayInfo() {
        System.out.println("ID: " + getId() + " | Kitap: " + getTitle() + " | Yazar: " + author + " | Durum: " + status);
    }

    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getPublisher() { return publisher; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
}