package model;

public abstract class User {
    private int id;
    private String tcNo;
    private String username;
    private String password;
    private String role;
    private String email;
    private String phone;

    // Constructor'ı BOZMADIK ki diğer sınıflar hata vermesin
    public User(String tcNo, String username, String password, String role) {
        this.tcNo = tcNo;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Her alt sınıfın (Member/Librarian) kendi panelini göstermesi için zorunlu metot
    public abstract void showDashboard();

    //GETTER / SETTER METOTLARI

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }
    public String getTcNo() { return tcNo; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}