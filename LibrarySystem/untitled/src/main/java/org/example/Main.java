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

    public static List<Book> libraryCache = new ArrayList<>();

    public static void main(String[] args) {
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
                System.out.print("Kullanıcı Adı: "); String uName = scanner.nextLine();
                System.out.print("Şifre: "); String pass = scanner.nextLine();

                User user = userDAO.findUserByUsername(uName);

                if (user != null && user.getPassword().equals(pass)) {
                    System.out.println("✅ Hoşgeldin: " + user.getUsername());
                    user.showMenu();

                } else {
                    System.out.println("❌ Hatalı bilgiler!");
                }
            }
            else if (choice == 2) {
                System.out.print("TC: "); String tc = scanner.nextLine();
                System.out.print("Kullanıcı Adı: "); String u = scanner.nextLine();
                System.out.print("Şifre: "); String p = scanner.nextLine();
                User newUser = UserFactory.createUser("MEMBER", tc, u, p);
                if (newUser != null && userDAO.addUser(newUser)) System.out.println("✅ Kayıt Başarılı!");
                else System.out.println("❌ Kayıt Başarısız!");
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
            System.out.printf("%-4d %-25s %-15s %-10s\n", b.getId(), title, b.getAuthor(), b.getStatus());
        }
    }
}