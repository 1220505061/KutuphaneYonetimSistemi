package model;

import observer.IObserver;

public class Member extends User implements IObserver {

    public Member(String tcNo, String username, String password) {
        super(tcNo, username, password, "MEMBER");
    }

    @Override
    public void showDashboard() {
        System.out.println("--- ÜYE PANELİ ---");
        System.out.println("1. Kitap Ara ve Ödünç Al");
        System.out.println("2. Kitap İade Et");
        System.out.println("0. Çıkış");
    }
    @Override
    public void update(String message) {
        System.out.println("🔔 SAYIN " + getUsername().toUpperCase() + ", BİLDİRİMİNİZ VAR: " + message);
    }
}