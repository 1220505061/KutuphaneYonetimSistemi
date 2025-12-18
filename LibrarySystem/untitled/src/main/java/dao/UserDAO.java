package dao;

import database.DatabaseConnection;
import factory.UserFactory;
import model.Log;
import model.Member;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public boolean addUser(User user) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (tc_no, username, password, role) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, user.getTcNo());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public User findUserByUsername(String username) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User u = UserFactory.createUser(rs.getString("role"), rs.getString("tc_no"), rs.getString("username"), rs.getString("password"));
                if(u!=null) {
                    u.setId(rs.getInt("id"));
                    u.setPhone(rs.getString("phone"));
                    u.setEmail(rs.getString("email"));
                }
                return u;
            }
        } catch (SQLException e) {}
        return null;
    }

    public boolean updateProfile(int id, String ph, String em) {
        try(Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET phone=?, email=? WHERE id=?")) {
            stmt.setString(1, ph); stmt.setString(2, em); stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }

    public List<Member> getAllMembers() {
        List<Member> list = new ArrayList<>();
        try(Connection c = DatabaseConnection.getInstance().getConnection(); Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT * FROM users WHERE role='MEMBER'");
            while(rs.next()) {
                Member m = new Member(rs.getString("username"), rs.getString("password"), rs.getString("tc_no"));
                m.setId(rs.getInt("id"));
                list.add(m);
            }
        } catch(Exception e){}
        return list;
    }

    public Member searchMember(String keyword) {
        try(Connection c=DatabaseConnection.getInstance().getConnection(); PreparedStatement p=c.prepareStatement("SELECT * FROM users WHERE role='MEMBER' AND username LIKE ?")) {
            p.setString(1, "%"+keyword+"%");
            ResultSet rs=p.executeQuery();
            if(rs.next()) {
                Member m = new Member(rs.getString("username"), rs.getString("password"), rs.getString("tc_no"));
                m.setId(rs.getInt("id"));
                return m;
            }
        } catch(Exception e){}
        return null;
    }

    public List<Log> getMemberHistory(int mid) {
        List<Log> list = new ArrayList<>();
        try(Connection c=DatabaseConnection.getInstance().getConnection(); PreparedStatement p=c.prepareStatement("SELECT * FROM logs WHERE member_id=?")) {
            p.setInt(1, mid);
            ResultSet rs=p.executeQuery();
            while(rs.next()) {
                Date bd = rs.getDate("borrow_date"); Date rd = rs.getDate("return_date");
                list.add(new Log(rs.getString("book_title"), bd!=null?bd.toLocalDate():null, rd!=null?rd.toLocalDate():null, rs.getInt("fine_paid")));
            }
        } catch(Exception e){}
        return list;
    }
    // OKUNMAMIŞ BİLDİRİMLERİ GETİR VE SİL
    public java.util.List<String> getAndClearNotifications(int memberId) {
        java.util.List<String> msgs = new java.util.ArrayList<>();
        try (java.sql.Connection c = database.DatabaseConnection.getInstance().getConnection()) {
            // 1. Mesajları Çek
            try(java.sql.PreparedStatement p = c.prepareStatement("SELECT message FROM notifications WHERE member_id=?")) {
                p.setInt(1, memberId);
                java.sql.ResultSet rs = p.executeQuery();
                while(rs.next()) msgs.add(rs.getString("message"));
            }
            // 2. Mesajları Sil (Okundu say)
            if(!msgs.isEmpty()){
                try(java.sql.PreparedStatement p2 = c.prepareStatement("DELETE FROM notifications WHERE member_id=?")) {
                    p2.setInt(1, memberId);
                    p2.executeUpdate();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return msgs;
    }
}