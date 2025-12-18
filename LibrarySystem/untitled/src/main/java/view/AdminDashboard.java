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
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ÜST PANEL
        JPanel topPanel = new JPanel();
        JButton btnAdd = new JButton("Kitap Ekle");
        JButton btnDelete = new JButton("Kitap Sil");
        JButton btnReturn = new JButton("İade Al");
        JButton btnMembers = new JButton("Üyeleri Listele");
        JButton btnLogout = new JButton("Çıkış");

        topPanel.add(btnAdd);
        topPanel.add(btnDelete);
        topPanel.add(btnReturn);
        topPanel.add(btnMembers);
        topPanel.add(btnLogout);
        add(topPanel, BorderLayout.NORTH);

        // ORTA PANEL
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
            JTextField tQty = new JTextField("1"); // Varsayılan stok 1 olsun

            Object[] message = {
                    "Başlık:", tTitle,
                    "Yazar:", tAuthor,
                    "ISBN:", tIsbn,
                    "Kategori:", tCat,
                    "Yayınevi:", tPub,
                    "Stok Adedi:", tQty // Arayüze eklendi
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Yeni Kitap Ekle", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    int quantity = Integer.parseInt(tQty.getText()); // Sayıya çevir
                    Book newBook = new Book(
                            tTitle.getText(),
                            tAuthor.getText(),
                            tIsbn.getText(),
                            tCat.getText(),
                            tPub.getText(),
                            quantity
                    );

                    if(bookDAO.addBook(newBook)) {
                        JOptionPane.showMessageDialog(this, "Kitap Eklendi!");
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
            if(bookDAO.deleteBook(bookId)) {
                JOptionPane.showMessageDialog(this, "Silindi.");
                refreshBookTable();
            }
        });

        // 3. İADE ALMA
        btnReturn.addActionListener(e -> {
            int row = bookTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "İade alınacak kitabı seçin.");
                return;
            }
            int bookId = (int) tableModel.getValueAt(row, 0);

            if(bookDAO.returnBook(bookId)){
                JOptionPane.showMessageDialog(this, "İade alındı ve sisteme işlendi.");
                refreshBookTable();
            }
        });

        // 4. ÜYELERİ GÖRME
        btnMembers.addActionListener(e -> {
            List<Member> members = userDAO.getAllMembers();
            StringBuilder sb = new StringBuilder("--- ÜYE LİSTESİ ---\n");
            for(Member m : members) {
                sb.append(m.getId()).append(" - ").append(m.getUsername()).append(" (").append(m.getTcNo()).append(")\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString());
        });

        // 5. ÇIKIŞ
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginUI().setVisible(true);
        });
    }

    private void refreshBookTable() {
        tableModel.setRowCount(0); // Tabloyu temizle
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