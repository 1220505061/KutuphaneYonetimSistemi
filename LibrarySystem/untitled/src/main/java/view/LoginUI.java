package view;

import dao.UserDAO;
import factory.UserFactory;
import model.User;
import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private UserDAO userDAO = new UserDAO();

    public LoginUI() {
        setTitle("Kütüphane Sistemi - Giriş");
        setSize(400, 300); // Pencereyi biraz büyüttüm
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

        btnRegister = new JButton("KAYIT OL");
        add(btnRegister);

        // --- GİRİŞ BUTONU ---
        btnLogin.addActionListener(e -> {
            String uName = txtUsername.getText();
            String pass = new String(txtPassword.getPassword());

            User user = userDAO.findUserByUsername(uName);

            if (user != null && user.getPassword().equals(pass)) {
                dispose();
                if (user.getRole().equalsIgnoreCase("ADMIN")) {
                    new AdminDashboard(user).setVisible(true);
                } else {
                    new MemberDashboard(user).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Hatalı Kullanıcı Adı veya Şifre!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- KAYIT BUTONU---
        btnRegister.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5)); // Satır sayısını artırdık

            JTextField regTc = new JTextField();
            JTextField regUser = new JTextField();
            JTextField regPass = new JTextField();

            // Rol Seçimi İçin Açılır Liste
            String[] roles = {"Normal Üye", "Yönetici (Admin)"};
            JComboBox<String> cmbRole = new JComboBox<>(roles);

            panel.add(new JLabel("TC Kimlik No:"));
            panel.add(regTc);
            panel.add(new JLabel("Kullanıcı Adı:"));
            panel.add(regUser);
            panel.add(new JLabel("Şifre:"));
            panel.add(regPass);
            panel.add(new JLabel("Hesap Türü:"));
            panel.add(cmbRole);

            int result = JOptionPane.showConfirmDialog(null, panel,
                    "Yeni Üye Kaydı", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String tc = regTc.getText().trim();
                String u = regUser.getText().trim();
                String p = regPass.getText().trim();
                String selectedRole = (String) cmbRole.getSelectedItem();
                String roleKey = "MEMBER"; // Varsayılan

                if(tc.isEmpty() || u.isEmpty() || p.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!");
                    return;
                }

                // EĞER ADMİN SEÇİLDİYSE GÜVENLİK KODU SOR
                if (selectedRole.contains("Admin")) {
                    String secretCode = JOptionPane.showInputDialog(this, "Yönetici kaydı için güvenlik kodunu giriniz:");
                    // Güvenlik kodu: 1234
                    if (secretCode != null && secretCode.equals("1234")) {
                        roleKey = "ADMIN";
                    } else {
                        JOptionPane.showMessageDialog(this, "Hatalı Güvenlik Kodu! Standart üye olarak kaydedilecek.");
                        roleKey = "MEMBER";
                    }
                }

                // FACTORY İLE OLUŞTUR
                User newUser = UserFactory.createUser(roleKey, tc, u, p);

                if (newUser != null && userDAO.addUser(newUser)) {
                    String msg = roleKey.equals("ADMIN") ? "YÖNETİCİ kaydı başarılı!" : "Üye kaydı başarılı!";
                    JOptionPane.showMessageDialog(this, msg + "\nGiriş yapabilirsiniz.");
                } else {
                    JOptionPane.showMessageDialog(this, "Kayıt Başarısız! (TC veya Kullanıcı Adı kullanılıyor olabilir)", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        new LoginUI().setVisible(true);
    }
}