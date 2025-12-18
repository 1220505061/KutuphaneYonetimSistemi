package model;

import dao.BookDAO;
import dao.UserDAO;
import factory.UserFactory;
import org.example.Main;
import java.util.List;
import java.util.Scanner;

public class Librarian extends User {
    private BookDAO bookDAO = new BookDAO();
    private UserDAO userDAO = new UserDAO();

    public Librarian(String username, String password, String tcNo) {
        super(username, password, tcNo);
        setRole("ADMIN");
    }

    @Override
    public void showMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- PERSONEL PANELİ ---");
            System.out.println("1. Kitap Yönetimi");
            System.out.println("2. Üye Yönetimi");
            System.out.println("0. Çıkış Yap");
            System.out.print("Seçim: ");

            int c;
            try { c = scanner.nextInt(); scanner.nextLine(); } catch (Exception e) { scanner.nextLine(); continue; }

            if (c == 0) break;
            if (c == 1) bookOps(scanner);
            else if (c == 2) memberOps(scanner);
        }
    }

    private void bookOps(Scanner scanner) {
        System.out.println("1. Ekle | 2. İade Al | 3. Sil");
        int c = scanner.nextInt(); scanner.nextLine();
        if(c==1) {
            System.out.print("Başlık: "); String t = scanner.nextLine();
            System.out.print("Yazar: "); String a = scanner.nextLine();
            Book b = new Book(t, a, "123", "Genel", "Yayınevi", 1);
            if(bookDAO.addBook(b)) {
                System.out.println("Eklendi.");
                Main.libraryCache = bookDAO.getAllBooks();
            }
        } else if(c==2) {
            Main.listBooks();
            System.out.print("İade ID: "); int id = scanner.nextInt();
            for(Book b : Main.libraryCache) {
                if(b.getId() == id) {
                    b.returnItem();
                    bookDAO.returnBook(id);
                    System.out.println("İade alındı.");
                }
            }
        } else if(c==3) {
            Main.listBooks();
            System.out.print("Silinecek ID: "); int id = scanner.nextInt();
            if(bookDAO.deleteBook(id)) {
                Main.libraryCache.removeIf(b -> b.getId() == id);
                System.out.println("Silindi.");
            }
        }
    }

    private void memberOps(Scanner scanner) {
        System.out.println("1. Kayıt | 2. Listele | 3. Detay");
        int c = scanner.nextInt(); scanner.nextLine();
        if(c==1) {
            System.out.print("TC: "); String tc = scanner.nextLine();
            System.out.print("Kullanıcı: "); String u = scanner.nextLine();
            System.out.print("Şifre: "); String p = scanner.nextLine();
            User nu = UserFactory.createUser("MEMBER", tc, u, p);
            if(userDAO.addUser(nu)) System.out.println("Kaydedildi.");
        } else if(c==2) {
            for(Member m : userDAO.getAllMembers()) System.out.println(m.getId() + " - " + m.getUsername());
        } else if(c==3) {
            System.out.print("Üye Ara: "); String k = scanner.nextLine();
            Member m = userDAO.searchMember(k);
            if(m!=null) {
                System.out.println("Bulundu: " + m.getUsername());
                List<Log> logs = userDAO.getMemberHistory(m.getId());
                for(Log l : logs) System.out.println(l);
            }
        }
    }
}