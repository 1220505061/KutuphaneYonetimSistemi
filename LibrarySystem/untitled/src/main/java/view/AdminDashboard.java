package view;

import dao.BookDAO;
import dao.UserDAO;
import model.Book;
import model.Member;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame {
    private User adminUser;
    private BookDAO bookDAO = new BookDAO();
    private UserDAO userDAO = new UserDAO();

    private JTable bookTable;
    private DefaultTableModel tableModel;

    public AdminDashboard(User adminUser) {
        this.adminUser = adminUser;
        setTitle("Personel Paneli - " + adminUser.getUsername());
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- ÜST PANEL (BUTONLAR) ---
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(230, 230, 230)); // Hafif gri arka plan

        JButton btnAdd = new JButton("Kitap Ekle");
        JButton btnDelete = new JButton("Kitap Sil");
        JButton btnReturn = new JButton("İade Al (Bildirimli)");
        JButton btnMembers = new JButton("Üyeleri Listele");

        // ÇIKIŞ BUTONU (RENK KODLARI SİLİNDİ - STANDART GÖRÜNÜM)
        JButton btnLogout = new JButton("Çıkış Yap");

        topPanel.add(btnAdd);
        topPanel.add(btnDelete);
        topPanel.add(btnReturn);
        topPanel.add(btnMembers);
        topPanel.add(btnLogout);
        add(topPanel, BorderLayout.NORTH);

        // --- ORTA PANEL (TABLO) ---
        String[] columns = {"ID", "Başlık", "Yazar", "ISBN", "Durum", "Stok"};
        tableModel = new DefaultTableModel(columns, 0);
        bookTable = new JTable(tableModel);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        refreshBookTable(); // Verileri yükle

        // --- BUTON OLAYLARI ---

        // 1. KİTAP EKLEME
        btnAdd.addActionListener(e -> {
            JTextField tTitle = new JTextField();
            JTextField tAuthor = new JTextField();
            JTextField tIsbn = new JTextField();
            JTextField tCat = new JTextField();
            JTextField tPub = new JTextField();
            JTextField tQty = new JTextField("1");

            Object[] message = {
                    "Başlık:", tTitle,
                    "Yazar:", tAuthor,
                    "ISBN:", tIsbn,
                    "Kategori:", tCat,
                    "Yayınevi:", tPub,
                    "Stok Adedi:", tQty
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Yeni Kitap Ekle", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    int quantity = Integer.parseInt(tQty.getText());
                    Book newBook = new Book(
                            tTitle.getText(),
                            tAuthor.getText(),
                            tIsbn.getText(),
                            tCat.getText(),
                            tPub.getText(),
                            quantity
                    );

                    if(bookDAO.addBook(newBook)) {
                        JOptionPane.showMessageDialog(this, "Kitap Başarıyla Eklendi!");
                        refreshBookTable();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Stok adedi sayı olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 2. KİTAP SİLME
        btnDelete.addActionListener(e -> {
            int row = bookTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Lütfen silinecek kitabı seçin.");
                return;
            }
            int bookId = (int) tableModel.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Bu kitabı silmek istediğinize emin misiniz?", "Silme Onayı", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                if(bookDAO.deleteBook(bookId)) {
                    JOptionPane.showMessageDialog(this, "Kitap silindi.");
                    refreshBookTable();
                }
            }
        });

        // 3. İADE ALMA (OBSERVER PATTERN - BİLDİRİMLİ)
        btnReturn.addActionListener(e -> {
            int row = bookTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "İade alınacak kitabı seçin.");
                return;
            }
            int bookId = (int) tableModel.getValueAt(row, 0);
            String bookTitle = (String) tableModel.getValueAt(row, 1);

            // Veritabanında iade işlemini yap
            if(bookDAO.returnBook(bookId)){

                // --- BİLDİRİM SİSTEMİ (OBSERVER) ---
                List<Integer> waitingMembers = bookDAO.getWaitingMembers(bookId);

                // Bildirim gönder
                for(int memId : waitingMembers) {
                    String msg = "📢 MÜJDE! Beklediğiniz '" + bookTitle + "' kitabı kütüphaneye geri döndü.";
                    bookDAO.addNotification(memId, msg);
                }

                // Bekleme listesini temizle
                bookDAO.clearWaitlist(bookId);

                String resultMsg = "İade işlemi başarılı.";
                if(!waitingMembers.isEmpty()) {
                    resultMsg += "\nSırada bekleyen " + waitingMembers.size() + " üyeye bildirim gönderildi!";
                }

                JOptionPane.showMessageDialog(this, resultMsg);
                refreshBookTable();
            } else {
                JOptionPane.showMessageDialog(this, "İade işlemi sırasında hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 4. ÜYELERİ LİSTELEME
        btnMembers.addActionListener(e -> {
            List<Member> members = userDAO.getAllMembers();
            if(members.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sistemde kayıtlı üye yok.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("ID  | TC NO       | KULLANICI ADI | İLETİŞİM\n");
            sb.append("--------------------------------------------------\n");
            for(Member m : members) {
                String phone = m.getPhone() != null ? m.getPhone() : "-";
                String email = m.getEmail() != null ? m.getEmail() : "-";
                sb.append(String.format("%-3d | %-11s | %-13s | %s / %s\n",
                        m.getId(), m.getTcNo(), m.getUsername(), phone, email));
            }

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Üye Listesi", JOptionPane.INFORMATION_MESSAGE);
        });

        // 5. ÇIKIŞ YAP
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Çıkış yapmak istiyor musunuz?", "Çıkış", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginUI().setVisible(true);
            }
        });
    }

    private void refreshBookTable() {
        tableModel.setRowCount(0);
        List<Book> books = bookDAO.getAllBooks();
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getIsbn(),
                    b.getStatus(),
                    b.getQuantity()
            });
        }
    }
}