package com.example.familybudget;

public class SavingGoal {
    private String goal;
    private double targetAmount;
    private double currentAmount;

    public SavingGoal(String goal, double targetAmount, double currentAmount) {
        this.goal = goal;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
    }

    public String getGoal() {
        return goal;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }
}
