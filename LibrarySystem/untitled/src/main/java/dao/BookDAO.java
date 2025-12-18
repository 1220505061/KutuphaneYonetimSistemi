package dao;

import database.DatabaseConnection;
import model.Book;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
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
        } catch (SQLException e) { return false; }
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Book book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getString("category"), rs.getString("publisher"), rs.getInt("quantity"));
                book.setId(rs.getInt("id"));
                book.setStatus(rs.getString("status"));
                Date sqlDate = rs.getDate("borrow_date");
                if (sqlDate != null) book.setBorrowDate(sqlDate.toLocalDate());
                books.add(book);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    public boolean deleteBook(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM books WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean borrowBook(int bookId, int memberId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE books SET status = 'LOANED', member_id = ?, borrow_date = ? WHERE id = ?")) {
            stmt.setInt(1, memberId);
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setInt(3, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean returnBook(int bookId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sel = "SELECT title, member_id, borrow_date FROM books WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sel);
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                long fine = 0;
                Date bd = rs.getDate("borrow_date");
                if(bd != null) {
                    long d = ChronoUnit.DAYS.between(bd.toLocalDate(), LocalDate.now());
                    if(d>7) fine = (d-7)*5;
                }
                String log = "INSERT INTO logs (member_id, book_title, borrow_date, return_date, fine_paid) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement lps = conn.prepareStatement(log);
                lps.setInt(1, rs.getInt("member_id"));
                lps.setString(2, rs.getString("title"));
                lps.setDate(3, bd);
                lps.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
                lps.setLong(5, fine);
                lps.executeUpdate();
            }
            PreparedStatement up = conn.prepareStatement("UPDATE books SET status = 'AVAILABLE', member_id = NULL, borrow_date = NULL WHERE id = ?");
            up.setInt(1, bookId);
            return up.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<Book> getBooksByMember(int memberId) {
        List<Book> myBooks = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM books WHERE member_id = ?")) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Book b = new Book(rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getString("category"), rs.getString("publisher"), rs.getInt("quantity"));
                b.setId(rs.getInt("id"));
                b.setStatus(rs.getString("status"));
                Date sqlDate = rs.getDate("borrow_date");
                if (sqlDate != null) b.setBorrowDate(sqlDate.toLocalDate());
                myBooks.add(b);
            }
        } catch(Exception e){}
        return myBooks;
    }
    // BU METODU BookDAO.java DOSYASININ İÇİNE EKLE
    public java.util.List<model.Book> searchBooks(String keyword) {
        java.util.List<model.Book> foundBooks = new java.util.ArrayList<>();
        String query = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR isbn LIKE ? OR LOWER(category) LIKE ?";
        try (java.sql.Connection conn = database.DatabaseConnection.getInstance().getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            java.sql.ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                model.Book b = new model.Book(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getString("publisher"),
                        rs.getInt("quantity")
                );
                b.setId(rs.getInt("id"));
                b.setStatus(rs.getString("status"));
                java.sql.Date sqlDate = rs.getDate("borrow_date");
                if (sqlDate != null) b.setBorrowDate(sqlDate.toLocalDate());
                foundBooks.add(b);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return foundBooks;
    }
}