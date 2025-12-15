package view;

import dao.BookDAO;
import dao.UserDAO;
import model.Book;
import model.User;
import state.AvailableState;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MemberDashboard extends JFrame {
    private User member;
    private BookDAO bookDAO = new BookDAO();
    private UserDAO userDAO = new UserDAO();

    private JTabbedPane tabbedPane;

    // Tablo Modelleri
    private DefaultTableModel allBooksModel;
    private JTable allBooksTable;

    private DefaultTableModel myBooksModel;
    private JTable myBooksTable;

    public MemberDashboard(User member) {
        this.member = member;
        setTitle("Üye Paneli - " + member.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // --- SEKME 1: KİTAP ARA / ÖDÜNÇ AL ---
        JPanel pnlSearch = new JPanel(new BorderLayout());
        JPanel topSearch = new JPanel();
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Ara");
        JButton btnBorrow = new JButton("Ödünç Al");
        JButton btnRefresh = new JButton("Yenile");

        topSearch.add(new JLabel("Arama:"));
        topSearch.add(txtSearch);
        topSearch.add(btnSearch);
        topSearch.add(btnBorrow);
        topSearch.add(btnRefresh);
        pnlSearch.add(topSearch, BorderLayout.NORTH);

        String[] cols = {"ID", "Başlık", "Yazar", "Kategori", "Durum"};
        allBooksModel = new DefaultTableModel(cols, 0);
        allBooksTable = new JTable(allBooksModel);
        pnlSearch.add(new JScrollPane(allBooksTable), BorderLayout.CENTER);

        // --- SEKME 2: ÖDÜNÇ ALDIKLARIM / PROFİL ---
        JPanel pnlMyBooks = new JPanel(new BorderLayout());
        String[] cols2 = {"ID", "Başlık", "Alış Tarihi", "Ceza Durumu"};
        myBooksModel = new DefaultTableModel(cols2, 0);
        myBooksTable = new JTable(myBooksModel);
        pnlMyBooks.add(new JScrollPane(myBooksTable), BorderLayout.CENTER);

        // Profil Güncelle Butonu
        JPanel bottomProfile = new JPanel();
        JButton btnProfile = new JButton("Profilimi Güncelle");
        bottomProfile.add(btnProfile);
        pnlMyBooks.add(bottomProfile, BorderLayout.SOUTH);

        // Sekmeleri Ekle
        tabbedPane.addTab("Kitap Ara & Ödünç Al", pnlSearch);
        tabbedPane.addTab("Kitaplarım & Profil", pnlMyBooks);
        add(tabbedPane);

        // Verileri Yükle
        refreshAllBooks(null);
        refreshMyBooks();

        // --- OLAYLAR ---

        // ARAMA
        btnSearch.addActionListener(e -> refreshAllBooks(txtSearch.getText()));
        btnRefresh.addActionListener(e -> refreshAllBooks(null));

        // ÖDÜNÇ ALMA
        btnBorrow.addActionListener(e -> {
            int row = allBooksTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Listeden bir kitap seçin!");
                return;
            }
            int bookId = (int) allBooksModel.getValueAt(row, 0);
            String status = (String) allBooksModel.getValueAt(row, 4);

            if (!"AVAILABLE".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Bu kitap şu an müsait değil!");
                // Burada Observer (Sıraya girme) mantığı tetiklenebilir
                return;
            }

            // Ödünç Al
            // RAM'deki nesneyi bulmamız lazım state pattern için
            List<Book> books = bookDAO.getAllBooks();
            for(Book b : books) {
                if(b.getId() == bookId) {
                    if(b.getState() instanceof AvailableState) {
                        b.borrowItem(); // State değiştir
                        bookDAO.borrowBook(bookId, member.getId()); // DB Yaz
                        JOptionPane.showMessageDialog(this, "Kitap ödünç alındı!");
                        refreshAllBooks(null);
                        refreshMyBooks();
                    }
                }
            }
        });

        // PROFİL GÜNCELLEME
        btnProfile.addActionListener(e -> {
            String newPhone = JOptionPane.showInputDialog("Yeni Telefon:");
            String newEmail = JOptionPane.showInputDialog("Yeni Email:");
            if(newPhone != null && newEmail != null) {
                userDAO.updateProfile(member.getId(), newPhone, newEmail);
                JOptionPane.showMessageDialog(this, "Güncellendi.");
            }
        });
    }

    private void refreshAllBooks(String keyword) {
        allBooksModel.setRowCount(0);
        List<Book> books;
        if (keyword == null || keyword.isEmpty()) books = bookDAO.getAllBooks();
        else books = bookDAO.searchBooks(keyword);

        for (Book b : books) {
            allBooksModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getCategory(), b.getStatus()});
        }
    }

    private void refreshMyBooks() {
        myBooksModel.setRowCount(0);
        List<Book> myBooks = bookDAO.getBooksByMember(member.getId());
        for (Book b : myBooks) {
            long fine = b.calculateFine();
            String fineStr = fine > 0 ? fine + " TL Ceza" : "Yok";
            myBooksModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getBorrowDate(), fineStr});
        }
    }
}