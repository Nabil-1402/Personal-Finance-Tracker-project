package org.example.models;

public class TransactionCategory {
    private int id;
    private String categoryName;
    private String categoryColour;

    public TransactionCategory(int id, String categoryName, String categoryColour) {
        this.id = id;
        this.categoryName = categoryName;
        this.categoryColour = categoryColour;
    }

    public int getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColour() {
        return categoryColour;
    }

    public void setCategoryColour(String categoryColour) {
        this.categoryColour = categoryColour;
    }
}
