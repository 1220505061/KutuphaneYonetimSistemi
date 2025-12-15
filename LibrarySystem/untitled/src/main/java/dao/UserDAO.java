package dao;

import database.DatabaseConnection;
import factory.UserFactory;
import model.Log;     // YENİ: Log modelini import ettik
import model.Member;  // YENİ: Member modelini import ettik
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // 1. KULLANICI EKLEME
    public boolean addUser(User user) {
        String query = "INSERT INTO users (tc_no, username, password, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getTcNo());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Ekleme hatası: " + e.getMessage());
            return false;
        }
    }
    // 2. KULLANICI BULMA (Giriş işlemleri için)
    public User findUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        User user = null;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = UserFactory.createUser(
                        rs.getString("role"),
                        rs.getString("tc_no"),
                        rs.getString("username"),
                        rs.getString("password")
                );
                if (user != null) {
                    user.setId(rs.getInt("id"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Arama hatası: " + e.getMessage());
        }
        return user;
    }
    // 3. PROFİL GÜNCELLEME
    public boolean updateProfile(int userId, String newPhone, String newEmail) {
        String query = "UPDATE users SET phone = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newPhone);
            stmt.setString(2, newEmail);
            stmt.setInt(3, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Profil güncelleme hatası: " + e.getMessage());
            return false;
        }
    }
    // 4. TÜM ÜYELERİ LİSTELE (Sadece 'MEMBER' rolündekiler)
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String query = "SELECT * FROM users WHERE role = 'MEMBER'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                // Member nesnesi oluşturuyoruz
                Member m = new Member(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("tc_no") // Veritabanındaki sütun adı
                );
                m.setId(rs.getInt("id"));
                m.setEmail(rs.getString("email"));
                m.setPhone(rs.getString("phone"));

                members.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    // 5. ÜYE ARAMA (İsim veya TC No ile)
    public Member searchMember(String keyword) {
        String query = "SELECT * FROM users WHERE role = 'MEMBER' AND (username LIKE ? OR tc_no LIKE ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Member m = new Member(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("tc_no")
                );
                m.setId(rs.getInt("id"));
                m.setEmail(rs.getString("email"));
                m.setPhone(rs.getString("phone"));
                return m;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 6. ÜYENİN GEÇMİŞİNİ GETİRME (LOG TABLOSUNDAN)
    public List<Log> getMemberHistory(int memberId) {
        List<Log> history = new ArrayList<>();
        String query = "SELECT * FROM logs WHERE member_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                Date bDate = rs.getDate("borrow_date");
                Date rDate = rs.getDate("return_date");

                Log log = new Log(
                        rs.getString("book_title"),
                        (bDate != null) ? bDate.toLocalDate() : null,
                        (rDate != null) ? rDate.toLocalDate() : null,
                        rs.getInt("fine_paid")
                );
                history.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}