package com.example.familybudget;

public class Expense {
    private String familyMember;
    private String category;
    private double amount;

    public Expense(String familyMember, String category, double amount) {
        this.familyMember = familyMember;
        this.category = category;
        this.amount = amount;
    }

    public String getFamilyMember() {
        return familyMember;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }
}
