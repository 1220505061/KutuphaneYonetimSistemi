package model;

public class Librarian extends User {

    public Librarian(String tcNo, String username, String password) {
        super(tcNo, username, password, "ADMIN");
    }

    @Override
    public void showDashboard() {
        System.out.println("--- PERSONEL PANELİ ---");
        System.out.println("1. Kitap Ekle");
        System.out.println("2. Kitap Sil/Güncelle");
        System.out.println("3. Üye Ekle");
        System.out.println("4. Raporları Gör");
        System.out.println("0. Çıkış");
    }
}