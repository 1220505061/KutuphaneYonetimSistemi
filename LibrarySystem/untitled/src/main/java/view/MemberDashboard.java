package view;

import dao.BookDAO;
import dao.UserDAO;
import model.Book;
import model.Member;
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
    private DefaultTableModel allBooksModel;
    private JTable allBooksTable;
    private DefaultTableModel myBooksModel;
    private JTable myBooksTable;

    public MemberDashboard(User member) {
        this.member = member;
        setTitle("Üye Paneli - " + member.getUsername());
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ana düzen
        setLayout(new BorderLayout());

        // --- 1. ÜST PANEL (HEADER) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Kenar boşlukları

        // Sol tarafta Hoşgeldin yazısı
        JLabel lblWelcome = new JLabel("Hoşgeldin, " + member.getUsername());
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Sağ tarafta Çıkış Butonu
        JButton btnLogout = new JButton("Çıkış Yap");

        topPanel.add(lblWelcome, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. SEKMELER (ORTA KISIM) ---
        tabbedPane = new JTabbedPane();

        // --- SEKME A: KİTAP ARA / ÖDÜNÇ AL ---
        JPanel pnlSearch = new JPanel(new BorderLayout());
        JPanel searchBarPanel = new JPanel();

        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Ara");
        JButton btnBorrow = new JButton("Ödünç Al");
        JButton btnRefresh = new JButton("Listeyi Yenile");

        searchBarPanel.add(new JLabel("Kitap Ara:"));
        searchBarPanel.add(txtSearch);
        searchBarPanel.add(btnSearch);
        searchBarPanel.add(btnBorrow);
        searchBarPanel.add(btnRefresh);
        pnlSearch.add(searchBarPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Başlık", "Yazar", "Kategori", "Durum"};
        allBooksModel = new DefaultTableModel(cols, 0);
        allBooksTable = new JTable(allBooksModel);
        pnlSearch.add(new JScrollPane(allBooksTable), BorderLayout.CENTER);

        // --- SEKME B: ÖDÜNÇ ALDIKLARIM / PROFİL ---
        JPanel pnlMyBooks = new JPanel(new BorderLayout());
        String[] cols2 = {"ID", "Başlık", "Alış Tarihi", "Ceza Durumu"};
        myBooksModel = new DefaultTableModel(cols2, 0);
        myBooksTable = new JTable(myBooksModel);
        pnlMyBooks.add(new JScrollPane(myBooksTable), BorderLayout.CENTER);

        // Profil Butonu
        JPanel bottomProfile = new JPanel();
        JButton btnProfile = new JButton("Profilimi Güncelle");
        bottomProfile.add(btnProfile);
        pnlMyBooks.add(bottomProfile, BorderLayout.SOUTH);

        // Sekmeleri Ekle
        tabbedPane.addTab("Kitap İşlemleri", pnlSearch);
        tabbedPane.addTab("Hesabım", pnlMyBooks);

        add(tabbedPane, BorderLayout.CENTER);

        // --- VERİLERİ YÜKLE ---
        refreshAllBooks(null);
        refreshMyBooks();

        // *** YENİ: EKRAN AÇILIR AÇILMAZ BİLDİRİMLERİ KONTROL ET ***
        List<String> notes = userDAO.getAndClearNotifications(member.getId());
        for(String note : notes) {
            JOptionPane.showMessageDialog(this, note, "📢 YENİ BİLDİRİM", JOptionPane.INFORMATION_MESSAGE);
        }

        // --- OLAYLAR ---

        // ÇIKIŞ BUTONU
        btnLogout.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(this,
                    "Oturumu kapatmak istiyor musunuz?",
                    "Çıkış",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                dispose();
                new LoginUI().setVisible(true);
            }
        });

        // ARAMA
        btnSearch.addActionListener(e -> refreshAllBooks(txtSearch.getText()));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            refreshAllBooks(null);
        });

        // ÖDÜNÇ ALMA & SIRAYA GİRME (GÜNCELLENDİ)
        btnBorrow.addActionListener(e -> {
            int row = allBooksTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Lütfen listeden bir kitap seçin.");
                return;
            }
            int bookId = (int) allBooksModel.getValueAt(row, 0);

            Book selectedBook = null;
            List<Book> books = bookDAO.getAllBooks();
            for(Book b : books) {
                if(b.getId() == bookId) { selectedBook = b; break; }
            }

            if (selectedBook != null) {
                if (selectedBook.getState() instanceof AvailableState) {
                    // KİTAP MÜSAİTSE AL
                    selectedBook.borrowItem();
                    bookDAO.borrowBook(bookId, member.getId());
                    JOptionPane.showMessageDialog(this, "Kitap ödünç alındı!");
                    refreshAllBooks(null);
                    refreshMyBooks();
                } else {
                    // KİTAP DOLUYSA -> SIRAYA GİR (Veritabanı Destekli)
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Kitap başkasında. Sıraya girmek ister misiniz?", "Sıra", JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        // 1. Zaten sırada mı? (Veritabanından kontrol)
                        if (bookDAO.isWaiting(bookId, member.getId())) {
                            JOptionPane.showMessageDialog(this, "Zaten bu kitap için sıradasınız.");
                        } else {
                            // 2. Değilse veritabanına ekle
                            bookDAO.addToWaitlist(bookId, member.getId());
                            JOptionPane.showMessageDialog(this, "Sıraya eklendiniz! Kitap gelince bildirim alacaksınız.");
                        }
                    }
                }
            }
        });

        // PROFİL
        btnProfile.addActionListener(e -> {
            String ph = JOptionPane.showInputDialog(this, "Yeni Telefon:", member.getPhone());
            String em = JOptionPane.showInputDialog(this, "Yeni Email:", member.getEmail());
            if(ph != null && em != null) {
                if(userDAO.updateProfile(member.getId(), ph, em)) {
                    member.setPhone(ph); member.setEmail(em);
                    JOptionPane.showMessageDialog(this, "Bilgiler güncellendi.");
                }
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
            String date = b.getBorrowDate() != null ? b.getBorrowDate().toString() : "-";
            myBooksModel.addRow(new Object[]{b.getId(), b.getTitle(), date, fineStr});
        }
    }
}