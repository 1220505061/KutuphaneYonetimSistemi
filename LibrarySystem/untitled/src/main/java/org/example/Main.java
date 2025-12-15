package org.example;

import dao.BookDAO;
import dao.UserDAO;
import factory.UserFactory;
import model.Book;
import model.Member;
import model.User;
import state.AvailableState;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static UserDAO userDAO = new UserDAO();
    static BookDAO bookDAO = new BookDAO();
    static Scanner scanner = new Scanner(System.in);

    static List<Book> libraryCache = new ArrayList<>();

    public static void main(String[] args) {
        // Sistemi başlatırken kitapları yükle
        libraryCache = bookDAO.getAllBooks();
        System.out.println("Sistem başlatıldı. Veriler yüklendi.");

        while (true) {
            System.out.println("\n--- KÜTÜPHANE SİSTEMİ ---");
            System.out.println("1. Giriş Yap");
            System.out.println("2. Kayıt Ol (Bireysel)");
            System.out.println("0. Çıkış");
            System.out.print("Seçiminiz: ");

            int choice;
            try { choice = scanner.nextInt(); scanner.nextLine(); }
            catch (Exception e) { scanner.nextLine(); continue; }

            if (choice == 0) break;

            if (choice == 1) {
                // GİRİŞ İŞLEMİ
                System.out.print("Kullanıcı Adı: "); String uName = scanner.nextLine();
                System.out.print("Şifre: "); String pass = scanner.nextLine();

                User user = userDAO.findUserByUsername(uName);

                if (user != null && user.getPassword().equals(pass)) {
                    System.out.println("✅ Hoşgeldin: " + user.getUsername());
                    if (user.getRole().equalsIgnoreCase("ADMIN")) adminMenu();
                    else memberMenu((Member) user);
                } else {
                    System.out.println("❌ Hatalı bilgiler!");
                }
            }
            else if (choice == 2) {
                // KAYIT İŞLEMİ (Bireysel)
                System.out.println("\n--- KAYIT OL ---");
                System.out.print("TC: "); String tc = scanner.nextLine();
                System.out.print("Kullanıcı Adı: "); String u = scanner.nextLine();
                System.out.print("Şifre: "); String p = scanner.nextLine();
                // Bireysel kayıtta varsayılan MEMBER olur
                User newUser = UserFactory.createUser("MEMBER", tc, u, p);
                if (newUser != null && userDAO.addUser(newUser)) System.out.println("✅ Kayıt Başarılı! Giriş yapabilirsiniz.");
                else System.out.println("❌ Kayıt Başarısız! (TC veya Kullanıcı Adı dolu olabilir)");
            }
        }
    }

    // --- PERSONEL (ADMIN) ANA MENÜSÜ ---
    public static void adminMenu() {
        while (true) {
            System.out.println("\n--- PERSONEL PANELİ ---");
            System.out.println("1. Kitap Yönetimi (Ekle / Sil / İade)");
            System.out.println("2. Üye Yönetimi (Listele / Ara / Detay)"); // YENİ EKLENDİ
            System.out.println("0. Çıkış Yap");
            System.out.print("Seçim: ");

            int c = scanner.nextInt(); scanner.nextLine();

            if (c == 0) break;

            if (c == 1) adminBookOperations();   // Kitap işlemlerini ayırdık
            else if (c == 2) adminMemberOperations(); // Üye işlemlerini ayırdık
        }
    }

    // --- KİTAP İŞLEMLERİ  ---
    public static void adminBookOperations() {
        System.out.println("\n--- KİTAP YÖNETİMİ ---");
        System.out.println("1. Yeni Kitap Ekle");
        System.out.println("2. Kitap İade Al");
        System.out.println("3. Kitap Sil");
        System.out.println("0. Geri Dön");
        System.out.print("Seçim: ");

        int c = scanner.nextInt(); scanner.nextLine();

        if (c == 1) {
            System.out.println("--- YENİ KİTAP EKLE ---");
            System.out.print("Başlık: "); String t = scanner.nextLine();
            System.out.print("Yazar: "); String a = scanner.nextLine();
            System.out.print("ISBN: "); String i = scanner.nextLine();
            System.out.print("Kategori: "); String cat = scanner.nextLine();
            System.out.print("Yayınevi: "); String pub = scanner.nextLine();
            System.out.print("Stok Adedi: "); int q = scanner.nextInt(); scanner.nextLine();

            Book b = new Book(t, a, i, cat, pub, q);
            if (bookDAO.addBook(b)) {
                System.out.println("✅ Kitap Eklendi!");
                libraryCache = bookDAO.getAllBooks(); // Cache güncelle
            } else {
                System.out.println("❌ Hata oluştu.");
            }
        }
        else if (c == 2) {
            listBooks();
            System.out.print("İade alınacak Kitap ID: ");
            int id = scanner.nextInt(); scanner.nextLine();

            // Önce RAM'deki nesneyi bulup durumunu düzeltelim (Observer tetiklensin)
            for (Book b : libraryCache) {
                if (b.getId() == id) {
                    b.returnItem();
                    // Veritabanında iade al (Logs tablosuna yazar)
                    if(bookDAO.returnBook(id)) {
                        System.out.println("✅ Kitap iade alındı ve geçmişe işlendi.");
                    }
                    break;
                }
            }
        }
        else if (c == 3) {
            listBooks();
            System.out.print("Silinecek Kitap ID: ");
            int id = scanner.nextInt(); scanner.nextLine();
            if (bookDAO.deleteBook(id)) {
                libraryCache.removeIf(b -> b.getId() == id);
                System.out.println("✅ Kitap silindi.");
            }
        }
    }

    // --- ÜYE İŞLEMLERİ ---
    public static void adminMemberOperations() {
        System.out.println("\n--- ÜYE YÖNETİMİ ---");
        System.out.println("1. Yeni Üye Kaydı Yap");
        System.out.println("2. Tüm Üyeleri Listele");
        System.out.println("3. Üye Ara ve Detay Gör (Geçmiş/Ceza)");
        System.out.println("0. Geri Dön");
        System.out.print("Seçim: ");

        int c = scanner.nextInt(); scanner.nextLine();

        if (c == 1) {
            System.out.print("TC: "); String tc = scanner.nextLine();
            System.out.print("Kullanıcı Adı: "); String u = scanner.nextLine();
            System.out.print("Şifre: "); String p = scanner.nextLine();

            User newUser = UserFactory.createUser("MEMBER", tc, u, p);
            if(userDAO.addUser(newUser)) System.out.println("✅ Üye sisteme kaydedildi.");
            else System.out.println("❌ Kayıt başarısız.");
        }
        else if (c == 2) {
            System.out.println("\n--- ÜYE LİSTESİ ---");
            List<Member> members = userDAO.getAllMembers();
            System.out.printf("%-5s %-15s %-15s %-15s\n", "ID", "KULLANICI", "TC NO", "TELEFON");
            System.out.println("-------------------------------------------------------");
            for(Member m : members) {
                System.out.printf("%-5d %-15s %-15s %-15s\n",
                        m.getId(), m.getUsername(), m.getTcNo(), (m.getPhone()==null ? "-" : m.getPhone()));
            }
        }
        else if (c == 3) {
            System.out.print("Aranacak Üye (Adı veya TC): ");
            String keyword = scanner.nextLine();
            Member m = userDAO.searchMember(keyword);

            if (m == null) {
                System.out.println("❌ Üye bulunamadı.");
            } else {
                System.out.println("\n========== ÜYE DETAY KARTI ==========");
                System.out.println("ID: " + m.getId());
                System.out.println("Kullanıcı Adı: " + m.getUsername());
                System.out.println("TC No: " + m.getTcNo());
                System.out.println("İletişim: " + (m.getPhone()!=null ? m.getPhone() : "-") + " | " + (m.getEmail()!=null ? m.getEmail() : "-"));

                // i. MEVCUT KİTAPLAR
                System.out.println("\n[1] ŞU AN ELİNDEKİ KİTAPLAR:");
                List<Book> currentBooks = bookDAO.getBooksByMember(m.getId());
                if (currentBooks.isEmpty()) System.out.println("   - Üzerinde kitap yok.");
                else {
                    for(Book b : currentBooks) {
                        long fine = b.calculateFine();
                        String status = (fine > 0) ? "GECİKMİŞ ("+fine+" TL)" : "Normal";
                        String bDate = (b.getBorrowDate() != null) ? b.getBorrowDate().toString() : "-";
                        System.out.println("   - " + b.getTitle() + " (Alış: " + bDate + ") -> " + status);
                    }
                }

                // ii. GEÇMİŞ (LOGLAR)
                System.out.println("\n[2] GEÇMİŞ KİTAP HAREKETLERİ:");
                List<model.Log> history = userDAO.getMemberHistory(m.getId());
                if (history.isEmpty()) System.out.println("   - Geçmiş kayıt bulunamadı.");
                else {
                    for(model.Log log : history) {
                        System.out.println("   - " + log.toString());
                    }
                }
                System.out.println("=====================================");
            }
        }
    }

    // --- ÜYE ANA MENÜSÜ ---
    public static void memberMenu(Member member) {
        while (true) {
            System.out.println("\n--- ÜYE PANELİ (" + member.getUsername() + ") ---");
            System.out.println("1. Kitapları Listele ve Ödünç Al");
            System.out.println("2. Ödünç Aldığım Kitaplar (Durum & Ceza)");
            System.out.println("3. Sırada Beklediklerim");
            System.out.println("4. Profilim");
            System.out.println("0. Çıkış Yap");
            System.out.print("Seçim: ");

            int c;
            try { c = scanner.nextInt(); scanner.nextLine(); }
            catch (Exception e) { scanner.nextLine(); continue; }

            if (c == 0) break;

            if (c == 1) {
                listBooks();
                System.out.print("Ödünç almak istediğiniz Kitap ID: ");
                int id = scanner.nextInt(); scanner.nextLine();

                Book selectedBook = null;
                for (Book b : libraryCache) { if (b.getId() == id) selectedBook = b; }

                if (selectedBook != null) {
                    if (selectedBook.getState() instanceof AvailableState) {
                        selectedBook.borrowItem();
                        bookDAO.borrowBook(id, member.getId());
                        System.out.println("✅ Kitabı ödünç aldınız. İyi okumalar!");
                    } else {
                        System.out.println("⚠️ Kitap başkasında! Sıraya girmek ister misiniz? (E/H)");
                        if (scanner.nextLine().equalsIgnoreCase("E")) {
                            if (selectedBook.isWaiting(member.getUsername())) {
                                System.out.println("Zaten bu kitap için sıradasınız.");
                            } else {
                                selectedBook.addObserver(member);
                                System.out.println("✅ Sıraya alındınız. Kitap gelince bildirim alacaksınız.");
                            }
                        }
                    }
                } else {
                    System.out.println("❌ Geçersiz ID.");
                }
            }
            else if (c == 2) {
                System.out.println("\n--- ÖDÜNÇ ALDIĞINIZ KİTAPLAR ---");
                List<Book> myBooks = bookDAO.getBooksByMember(member.getId());

                if (myBooks.isEmpty()) {
                    System.out.println("Şu an üzerinizde kitap bulunmamaktadır.");
                } else {
                    System.out.printf("%-4s %-20s %-12s %-12s %-10s\n", "ID", "KİTAP ADI", "ALIŞ T.", "İADE T.", "CEZA");
                    System.out.println("----------------------------------------------------------------");

                    for (Book b : myBooks) {
                        String bDate = (b.getBorrowDate() != null) ? b.getBorrowDate().toString() : "-";
                        String dDate = (b.getBorrowDate() != null) ? b.getBorrowDate().plusDays(7).toString() : "-";
                        long fine = b.calculateFine();
                        String fineStr = (fine > 0) ? fine + " TL" : "Yok";

                        String title = b.getTitle();
                        if (title.length() > 18) title = title.substring(0, 18) + "..";

                        System.out.printf("%-4d %-20s %-12s %-12s %-10s\n",
                                b.getId(), title, bDate, dDate, fineStr);
                    }
                    System.out.println("----------------------------------------------------------------");
                    System.out.println("* İade süresi 7 gündür. Geciken her gün için 5 TL ceza uygulanır.");
                }
            }
            else if (c == 3) {
                System.out.println("\n--- SIRADA BEKLEDİĞİNİZ KİTAPLAR ---");
                boolean waitingAny = false;
                for (Book b : libraryCache) {
                    if (b.isWaiting(member.getUsername())) {
                        System.out.println("- " + b.getTitle() + " (Durumu: " + b.getStatus() + ")");
                        waitingAny = true;
                    }
                }
                if (!waitingAny) System.out.println("Herhangi bir kitap için sırada değilsiniz.");
            }
            else if (c == 4) {
                System.out.println("\n--- PROFİL BİLGİLERİ ---");
                System.out.println("Kullanıcı Adı: " + member.getUsername());
                System.out.println("TC No: " + member.getTcNo());
                System.out.println("E-Posta: " + (member.getEmail() == null ? "-" : member.getEmail()));
                System.out.println("Telefon: " + (member.getPhone() == null ? "-" : member.getPhone()));
                System.out.println("-------------------------");
                System.out.println("1. Bilgileri Güncelle | 0. Geri Dön");
                System.out.print("Seçim: ");

                int pChoice = scanner.nextInt(); scanner.nextLine();

                if (pChoice == 1) {
                    System.out.print("Yeni Telefon: "); String phone = scanner.nextLine();
                    System.out.print("Yeni E-Posta: "); String email = scanner.nextLine();

                    if (userDAO.updateProfile(member.getId(), phone, email)) {
                        member.setPhone(phone);
                        member.setEmail(email);
                        System.out.println("✅ Profil güncellendi!");
                    } else {
                        System.out.println("❌ Güncelleme başarısız.");
                    }
                }
            }
        }
    }

    public static void listBooks() {
        System.out.println("\n--- KÜTÜPHANE ENVANTERİ ---");
        System.out.printf("%-4s %-25s %-15s %-10s\n", "ID", "BAŞLIK", "YAZAR", "DURUM");
        System.out.println("---------------------------------------------------------");
        for (Book b : libraryCache) {
            String title = b.getTitle();
            if(title.length() > 23) title = title.substring(0,23) + "..";

            System.out.printf("%-4d %-25s %-15s %-10s\n",
                    b.getId(), title, b.getAuthor(), b.getStatus());
        }
    }
}