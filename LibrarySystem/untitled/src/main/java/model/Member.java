package model;

import dao.BookDAO;
import dao.UserDAO;
import observer.IObserver;
import org.example.Main;
import state.AvailableState;
import java.util.List;
import java.util.Scanner;

public class Member extends User implements IObserver {
    private BookDAO bookDAO = new BookDAO();
    private UserDAO userDAO = new UserDAO();

    public Member(String username, String password, String tcNo) {
        super(username, password, tcNo);
        setRole("MEMBER");
    }

    @Override
    public void update(String message) {
        System.out.println("\n🔔 [BİLDİRİM] Sayın " + getUsername() + ", " + message);
    }

    @Override
    public void showMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- ÜYE PANELİ (" + getUsername() + ") ---");
            System.out.println("1. Kitapları Listele ve Ödünç Al");
            System.out.println("2. Ödünç Aldığım Kitaplar (Durum & Ceza)");
            System.out.println("3. Sırada Beklediklerim");
            System.out.println("4. Profilim");
            System.out.println("0. Çıkış Yap");
            System.out.print("Seçim: ");

            int c;
            try { c = scanner.nextInt(); scanner.nextLine(); } catch (Exception e) { scanner.nextLine(); continue; }

            if (c == 0) break;

            if (c == 1) {
                Main.listBooks();
                System.out.print("Ödünç almak istediğiniz Kitap ID: ");
                int id = scanner.nextInt(); scanner.nextLine();
                Book selectedBook = null;
                for (Book b : Main.libraryCache) { if (b.getId() == id) selectedBook = b; }

                if (selectedBook != null) {
                    if (selectedBook.getState() instanceof AvailableState) {
                        selectedBook.borrowItem();
                        bookDAO.borrowBook(id, getId());
                        System.out.println("✅ Kitabı ödünç aldınız.");
                    } else {
                        System.out.println("⚠️ Kitap başkasında! Sıraya girmek ister misiniz? (E/H)");
                        if (scanner.nextLine().equalsIgnoreCase("E")) {
                            if (selectedBook.isWaiting(getUsername())) System.out.println("Zaten sıradasınız.");
                            else {
                                selectedBook.addObserver(this);
                                System.out.println("✅ Sıraya alındınız.");
                            }
                        }
                    }
                } else System.out.println("❌ Geçersiz ID.");
            } else if (c == 2) {
                System.out.println("\n--- ÖDÜNÇ ALDIĞINIZ KİTAPLAR ---");
                List<Book> myBooks = bookDAO.getBooksByMember(getId());
                if (myBooks.isEmpty()) System.out.println("Kitap yok.");
                else {
                    for (Book b : myBooks) {
                        long fine = b.calculateFine();
                        String fineStr = fine > 0 ? fine + " TL" : "Yok";
                        System.out.println("- " + b.getTitle() + " | Ceza: " + fineStr);
                    }
                }
            } else if (c == 3) {
                boolean waitingAny = false;
                for (Book b : Main.libraryCache) {
                    if (b.isWaiting(getUsername())) {
                        System.out.println("- " + b.getTitle());
                        waitingAny = true;
                    }
                }
                if (!waitingAny) System.out.println("Sırada değilsiniz.");
            } else if (c == 4) {
                System.out.println("TC: " + getTcNo() + " | Tel: " + getPhone());
                System.out.println("1. Güncelle | 0. Geri");
                if(scanner.nextInt() == 1) {
                    scanner.nextLine();
                    System.out.print("Yeni Tel: "); String ph = scanner.nextLine();
                    System.out.print("Yeni Email: "); String em = scanner.nextLine();
                    if(userDAO.updateProfile(getId(), ph, em)) {
                        setPhone(ph); setEmail(em);
                        System.out.println("Güncellendi.");
                    }
                }
            }
        }
    }
}