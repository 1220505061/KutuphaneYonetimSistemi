package dao;

import database.DatabaseConnection;
import model.Book;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit; // Gün farkı hesaplamak için gerekli
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // 1. KİTAP EKLEME
    public boolean addBook(Book book) {
        String query = "INSERT INTO books (title, author, isbn, category, publisher, quantity, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getCategory());
            stmt.setString(5, book.getPublisher());
            stmt.setInt(6, book.getQuantity());
            stmt.setString(7, book.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Ekleme hatası: " + e.getMessage());
            return false;
        }
    }

    // 2. TÜM KİTAPLARI LİSTELEME
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Book book = new Book(
                        rs.getString("title"), rs.getString("author"), rs.getString("isbn"),
                        rs.getString("category"), rs.getString("publisher"), rs.getInt("quantity")
                );
                book.setId(rs.getInt("id"));
                book.setStatus(rs.getString("status"));

                Date sqlDate = rs.getDate("borrow_date");
                if (sqlDate != null) {
                    book.setBorrowDate(sqlDate.toLocalDate());
                }

                books.add(book);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    // 3. KİTAP SİLME
    public boolean deleteBook(int id) {
        String query = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // 4. KİTAP ÖDÜNÇ VERME
    public boolean borrowBook(int bookId, int memberId) {
        String query = "UPDATE books SET status = 'LOANED', member_id = ?, borrow_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, memberId);
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setInt(3, bookId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 5. KİTAP İADE ALMA (GÜNCELLENDİ - Loglama Sistemi Eklendi)
    public boolean returnBook(int bookId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Transaction başlatıyoruz (İki işlem aynı anda yapılmalı)

            // A) Önce kitabın bilgilerini (Kimin aldığını ve tarihini) alalım
            String selectSql = "SELECT title, member_id, borrow_date FROM books WHERE id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, bookId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                String title = rs.getString("title");
                int memberId = rs.getInt("member_id");
                Date borrowDate = rs.getDate("borrow_date");

                // B) Ceza hesapla (7 günü geçtiyse gün başına 5 TL)
                long fine = 0;
                if (borrowDate != null) {
                    long days = ChronoUnit.DAYS.between(borrowDate.toLocalDate(), LocalDate.now());
                    if (days > 7) {
                        fine = (days - 7) * 5;
                    }
                }

                // C) LOG tablosuna geçmiş kaydı olarak ekle
                String logSql = "INSERT INTO logs (member_id, book_title, borrow_date, return_date, fine_paid) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement logStmt = conn.prepareStatement(logSql);
                logStmt.setInt(1, memberId);
                logStmt.setString(2, title);
                logStmt.setDate(3, borrowDate);
                logStmt.setDate(4, java.sql.Date.valueOf(LocalDate.now())); // İade tarihi = Bugün
                logStmt.setLong(5, fine);
                logStmt.executeUpdate();
            }

            // D) Kitabı "AVAILABLE" durumuna getir ve kullanıcıdan düş
            String updateSql = "UPDATE books SET status = 'AVAILABLE', member_id = NULL, borrow_date = NULL WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, bookId);
            updateStmt.executeUpdate();

            conn.commit(); // İşlemleri onayla
            return true;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {} // Hata varsa geri al
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }

    // 6. ÜYENİN KENDİ KİTAPLARINI GETİRME
    public List<Book> getBooksByMember(int memberId) {
        List<Book> myBooks = new ArrayList<>();
        String query = "SELECT * FROM books WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Book b = new Book(
                        rs.getString("title"), rs.getString("author"), rs.getString("isbn"),
                        rs.getString("category"), rs.getString("publisher"), rs.getInt("quantity")
                );
                b.setId(rs.getInt("id"));
                b.setStatus(rs.getString("status"));

                Date sqlDate = rs.getDate("borrow_date");
                if (sqlDate != null) {
                    b.setBorrowDate(sqlDate.toLocalDate());
                }

                myBooks.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return myBooks;
    }

    // 7. ARAMA METODU
    public List<Book> searchBooks(String keyword) {
        List<Book> foundBooks = new ArrayList<>();
        String query = "SELECT * FROM books WHERE " +
                "LOWER(title) LIKE ? OR " +
                "LOWER(author) LIKE ? OR " +
                "isbn LIKE ? OR " +
                "LOWER(category) LIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword.toLowerCase() + "%";

            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Book b = new Book(
                        rs.getString("title"), rs.getString("author"), rs.getString("isbn"),
                        rs.getString("category"), rs.getString("publisher"), rs.getInt("quantity")
                );
                b.setId(rs.getInt("id"));
                b.setStatus(rs.getString("status"));

                Date sqlDate = rs.getDate("borrow_date");
                if (sqlDate != null) {
                    b.setBorrowDate(sqlDate.toLocalDate());
                }

                foundBooks.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return foundBooks;
    }
}