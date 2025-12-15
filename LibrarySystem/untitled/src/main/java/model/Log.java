package model;

import java.time.LocalDate;

public class Log {
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private int fine;

    public Log(String bookTitle, LocalDate borrowDate, LocalDate returnDate, int fine) {
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.fine = fine;
    }

    @Override
    public String toString() {
        return String.format("Kitap: %-20s | Alış: %s | İade: %s | Ceza: %d TL",
                bookTitle, borrowDate, returnDate, fine);
    }
}