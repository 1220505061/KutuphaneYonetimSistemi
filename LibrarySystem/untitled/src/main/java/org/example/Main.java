package org.example;

import dao.BookDAO;
import dao.UserDAO;
import factory.UserFactory;
import model.Book;
import model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static UserDAO userDAO = new UserDAO();
    static BookDAO bookDAO = new BookDAO();
    static Scanner scanner = new Scanner(System.in);

    // Kitap önbelleği
    public static List<Book> libraryCache = new ArrayList<>();

    public static void main(String[] args) {
        // Başlangıçta verileri çek
        libraryCache = bookDAO.getAllBooks();
        System.out.println("Sistem başlatıldı. Veriler veritabanından yüklendi.");

        while (true) {
            System.out.println("\n--- 📚 KÜTÜPHANE SİSTEMİ (KONSOL) ---");
            System.out.println("1. Giriş Yap");
            System.out.println("2. Kayıt Ol");
            System.out.println("0. Çıkış");
            System.out.print("Seçiminiz: ");

            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Satır sonu karakterini temizle
            } catch (Exception e) {
                scanner.nextLine();
                continue;
            }

            if (choice == 0) {
                System.out.println("Çıkış yapılıyor...");
                break;
            }

            if (choice == 1) {
                // --- GİRİŞ YAPMA ---
                System.out.print("Kullanıcı Adı: "); String uName = scanner.nextLine();
                System.out.print("Şifre: "); String pass = scanner.nextLine();

                User user = userDAO.findUserByUsername(uName);

                if (user != null && user.getPassword().equals(pass)) {
                    System.out.println("✅ Hoşgeldin: " + user.getUsername());
                    user.showMenu();
                    libraryCache = bookDAO.getAllBooks();
                } else {
                    System.out.println("❌ Hatalı kullanıcı adı veya şifre!");
                }
            }
            else if (choice == 2) {
                // --- KAYIT OLMA ---
                System.out.println("\n--- YENİ KULLANICI KAYDI ---");
                System.out.print("TC Kimlik No: "); String tc = scanner.nextLine();
                System.out.print("Kullanıcı Adı: "); String u = scanner.nextLine();
                System.out.print("Şifre: "); String p = scanner.nextLine();

                System.out.println("Hesap Türü:");
                System.out.println("1. Normal Üye");
                System.out.println("2. Yönetici (Admin)");
                System.out.print("Seçim (1 veya 2): ");

                int roleChoice = 1;
                try { roleChoice = scanner.nextInt(); scanner.nextLine(); }
                catch(Exception e) { scanner.nextLine(); }

                String role = "MEMBER"; // Varsayılan

                if (roleChoice == 2) {
                    System.out.print("🔒 Yönetici Güvenlik Kodu: ");
                    String code = scanner.nextLine();
                    if (code.equals("1234")) {
                        role = "ADMIN";
                        System.out.println("🔓 Yönetici yetkisi onaylandı.");
                    } else {
                        System.out.println("⚠️ Hatalı kod! Güvenlik nedeniyle 'Normal Üye' olarak kaydediliyorsunuz.");
                    }
                }

                // Factory ile nesneyi üret
                User newUser = UserFactory.createUser(role, tc, u, p);

                // Veritabanına kaydet
                if (newUser != null && userDAO.addUser(newUser)) {
                    System.out.println("✅ Kayıt Başarılı! (" + role + ")");
                    System.out.println("Ana menüden giriş yapabilirsiniz.");
                } else {
                    System.out.println("❌ Kayıt Başarısız!");
                    System.out.println("(İpucu: TC veya Kullanıcı Adı daha önce alınmış olabilir.)");
                }
            }
        }
    }

    // Kitapları listeleme metodu
    public static void listBooks() {
        System.out.println("\n--- MEVCUT KİTAPLAR ---");
        System.out.printf("%-4s %-25s %-15s %-10s\n", "ID", "BAŞLIK", "YAZAR", "DURUM");
        System.out.println("---------------------------------------------------------");
        for (Book b : libraryCache) {
            String title = b.getTitle();
            if(title.length() > 23) title = title.substring(0,23) + "..";
            System.out.printf("%-4d %-25s %-15s %-10s\n", b.getId(), title, b.getAuthor(), b.getStatus());
        }
    }
}