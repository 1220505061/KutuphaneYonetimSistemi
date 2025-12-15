package model;
public abstract class LibraryItem {
    private int id;
    private String title;

    public LibraryItem(String title) {
        this.title = title;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public abstract void displayInfo();
}