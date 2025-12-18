package model;

public abstract class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String tcNo;
    private String email;
    private String phone;

    public User(String username, String password, String tcNo) {
        this.username = username;
        this.password = password;
        this.tcNo = tcNo;
    }
    public abstract void showMenu();

    // Getter - Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getTcNo() { return tcNo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}