package view;

import dao.UserDAO;
import model.User;
import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister; // Kayıt ol butonu da ekleyelim
    private UserDAO userDAO = new UserDAO();

    public LoginUI() {
        setTitle("Kütüphane Sistemi - Giriş");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        // Bileşenler
        add(new JLabel("   Kullanıcı Adı:"));
        txtUsername = new JTextField();
        add(txtUsername);

        add(new JLabel("   Şifre:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        btnLogin = new JButton("GİRİŞ YAP");
        add(btnLogin);

        btnRegister = new JButton("KAYIT OL"); // Basit kayıt butonu
        add(btnRegister);

        // --- GİRİŞ BUTONU ---
        btnLogin.addActionListener(e -> {
            String uName = txtUsername.getText();
            String pass = new String(txtPassword.getPassword());

            User user = userDAO.findUserByUsername(uName);

            if (user != null && user.getPassword().equals(pass)) {
                dispose(); // Giriş penceresini kapat
                // Role göre ekran aç
                if (user.getRole().equalsIgnoreCase("ADMIN")) {
                    new AdminDashboard(user).setVisible(true);
                } else {
                    new MemberDashboard(user).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Hatalı Bilgiler!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- KAYIT BUTONU ---
        btnRegister.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Admin ile iletişime geçiniz veya veritabanına ekleyiniz.\n(Proje kapsamında basitleştirildi)");
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        new LoginUI().setVisible(true);
    }
}