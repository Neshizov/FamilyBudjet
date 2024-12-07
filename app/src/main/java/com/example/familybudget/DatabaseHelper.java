package com.example.familybudget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "family_budget.db";
    private static final String EXPENSES_TABLE = "expenses";
    private static final String INCOME_TABLE = "income";
    private static final String SAVINGS_TABLE = "savings";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    @Override

    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "onCreate called");
        db.execSQL("CREATE TABLE " + EXPENSES_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, FAMILY_MEMBER TEXT, CATEGORY TEXT, AMOUNT REAL, MONTH TEXT)");
        db.execSQL("CREATE TABLE " + INCOME_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, FAMILY_MEMBER TEXT, SOURCE TEXT, AMOUNT REAL, MONTH TEXT)");
        db.execSQL("CREATE TABLE " + SAVINGS_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, FAMILY_MEMBER TEXT, GOAL TEXT, START_DATE TEXT, END_DATE TEXT, TARGET_AMOUNT REAL, CURRENT_AMOUNT REAL)");
    }

    public boolean isGoalReached(String familyMember, String goal) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT TARGET_AMOUNT, CURRENT_AMOUNT FROM " + SAVINGS_TABLE + " WHERE FAMILY_MEMBER = ? AND GOAL = ?",
                new String[]{familyMember, goal}
        );

        boolean goalReached = false;
        if (cursor.moveToFirst()) {
            double targetAmount = cursor.getDouble(0);
            double currentAmount = cursor.getDouble(1);
            goalReached = currentAmount >= targetAmount;
        }
        cursor.close();
        return goalReached;
    }


    public boolean deleteSavingGoal(String familyMember, String goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(SAVINGS_TABLE, "FAMILY_MEMBER = ? AND GOAL = ?", new String[]{familyMember, goal});
        return rowsAffected > 0;
    }
    public Cursor getAllSavings() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + SAVINGS_TABLE, null);
    }

    public int getSavingProgress(String familyMember, String goal) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT TARGET_AMOUNT, CURRENT_AMOUNT FROM " + SAVINGS_TABLE + " WHERE FAMILY_MEMBER = ? AND GOAL = ?",
                new String[]{familyMember, goal}
        );

        int progress = 0;
        if (cursor.moveToFirst()) {
            double targetAmount = cursor.getDouble(0);
            double currentAmount = cursor.getDouble(1);
            if (targetAmount > 0) {
                progress = (int) ((currentAmount / targetAmount) * 100);
            }
        }
        cursor.close();
        return progress;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + EXPENSES_TABLE + " ADD COLUMN MONTH TEXT");
            db.execSQL("ALTER TABLE " + INCOME_TABLE + " ADD COLUMN MONTH TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + SAVINGS_TABLE + " (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "FAMILY_MEMBER TEXT, " +
                    "GOAL TEXT, " +
                    "START_DATE TEXT, " +
                    "END_DATE TEXT, " +
                    "TARGET_AMOUNT REAL, " +
                    "CURRENT_AMOUNT REAL)");
        }
    }


    public Cursor getUniqueFamilyMembers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT FAMILY_MEMBER FROM " + SAVINGS_TABLE, null);
    }


    public boolean addSavingGoal(String familyMember, String goal, String startDate, String endDate, double targetAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("FAMILY_MEMBER", familyMember);
        contentValues.put("GOAL", goal);
        contentValues.put("START_DATE", startDate);
        contentValues.put("END_DATE", endDate);
        contentValues.put("TARGET_AMOUNT", targetAmount);
        contentValues.put("CURRENT_AMOUNT", 0.0); // Начальная сумма

        long result = db.insert(SAVINGS_TABLE, null, contentValues);
        return result != -1;
    }


    public Cursor getSavingsByFamilyMember(String familyMember) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + SAVINGS_TABLE + " WHERE FAMILY_MEMBER = ?", new String[]{familyMember});
    }


    public String getCurrentMonth() {
        return android.text.format.DateFormat.format("MM-yyyy", new java.util.Date()).toString();
    }

    public boolean insertSavingGoal(String familyMember, String goal, double targetAmount, double currentAmount, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("FAMILY_MEMBER", familyMember);
        contentValues.put("GOAL", goal);
        contentValues.put("TARGET_AMOUNT", targetAmount);
        contentValues.put("CURRENT_AMOUNT", currentAmount);
        contentValues.put("START_DATE", startDate);
        contentValues.put("END_DATE", endDate);

        long result = db.insert(SAVINGS_TABLE, null, contentValues);
        return result != -1;
    }

    public SavingGoal getSavingGoal(String familyMember, String goal) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT GOAL, TARGET_AMOUNT, CURRENT_AMOUNT FROM " + SAVINGS_TABLE + " WHERE FAMILY_MEMBER = ? AND GOAL = ?",
                new String[]{familyMember, goal}
        );

        if (cursor.moveToFirst()) {
            String goalName = cursor.getString(0);
            double targetAmount = cursor.getDouble(1);
            double currentAmount = cursor.getDouble(2);
            cursor.close();
            return new SavingGoal(goalName, targetAmount, currentAmount);
        }
        cursor.close();
        return null; // Цель не найдена
    }


    public boolean markGoalAsComplete(String familyMember, String goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CURRENT_AMOUNT", getSavingGoal(familyMember, goal).getTargetAmount()); // Установить текущую сумму равной целевой.

        int rowsAffected = db.update(SAVINGS_TABLE, contentValues, "FAMILY_MEMBER = ? AND GOAL = ?", new String[]{familyMember, goal});
        return rowsAffected > 0;
    }

    public boolean updateSavingGoal(String familyMember, String goal, double newAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CURRENT_AMOUNT", newAmount);

        int rowsAffected = db.update(SAVINGS_TABLE, contentValues, "FAMILY_MEMBER = ? AND GOAL = ?", new String[]{familyMember, goal});
        return rowsAffected > 0;
    }


    public boolean addIncome(String familyMember, String source, double amount, String month) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("FAMILY_MEMBER", familyMember);
        contentValues.put("SOURCE", source);
        contentValues.put("AMOUNT", amount);
        contentValues.put("MONTH", month);

        long result = db.insert(INCOME_TABLE, null, contentValues);
        return result != -1;
    }

    public boolean insertExpense(String familyMember, String category, double amount, String month) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("FAMILY_MEMBER", familyMember);
        contentValues.put("CATEGORY", category);
        contentValues.put("AMOUNT", amount);
        contentValues.put("MONTH", month);

        long result = db.insert(EXPENSES_TABLE, null, contentValues);
        return result != -1;
    }

    // Получение всех доходов
    public Cursor getAllIncomes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + INCOME_TABLE, null);
    }

    public boolean updateCurrentSavings(String familyMember, String goal, double newAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CURRENT_AMOUNT", newAmount);

        int rowsAffected = db.update(SAVINGS_TABLE, contentValues, "FAMILY_MEMBER = ? AND GOAL = ?", new String[]{familyMember, goal});
        return rowsAffected > 0;
    }


    // Получение всех расходов
    public Cursor getAllExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + EXPENSES_TABLE, null);
    }

    // Получение доходов по месяцу
    public Cursor getIncomesByMonth(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + INCOME_TABLE + " WHERE MONTH = ?", new String[]{month});
    }

    // Получение расходов по месяцу
    public Cursor getExpensesByMonth(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + EXPENSES_TABLE + " WHERE MONTH = ?", new String[]{month});
    }

    // Удаление доходов по категории для членя семьи
    public boolean deleteIncomeByCategory(String familyMember, String category, String month) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "family_member = ? AND source = ? AND month = ?";
        String[] whereArgs = { familyMember, category, month };

        int rowsAffected = db.delete(INCOME_TABLE, whereClause, whereArgs);

        return rowsAffected > 0;
    }

    // Удаление доходов по имени члена семьи и месяцу
    public void deleteIncomeByFamilyMemberAndMonth(String familyMember, String month) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(INCOME_TABLE, "FAMILY_MEMBER = ? AND MONTH = ?", new String[]{familyMember, month});
    }

    // Удаление расходов по имени члена семьи и месяцу
    public void deleteExpenseByFamilyMemberAndMonth(String familyMember, String month) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EXPENSES_TABLE, "FAMILY_MEMBER = ? AND MONTH = ?", new String[]{familyMember, month});
    }

    // Очистка всей базы данных доходов
    public void clearIncomes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(INCOME_TABLE, null, null);
    }

    // Очистка всей базы данных расходов
    public void clearExpenses() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EXPENSES_TABLE, null, null);
    }

    // Удаление всех доходов конкретного члена семьи
    public void deleteIncomeByFamilyMember(String familyMember) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(INCOME_TABLE, "FAMILY_MEMBER = ?", new String[]{familyMember});
    }
    // Удаление всех расходов конкретного члена семьи
    public void deleteExpenseByFamilyMember(String familyMember) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EXPENSES_TABLE, "FAMILY_MEMBER = ?", new String[]{familyMember});
    }

    // Удаление расходов конкретного члена семьи по категории
    public void deleteExpenseByFamilyMemberAndCategory(String familyMember, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EXPENSES_TABLE, "FAMILY_MEMBER = ? AND CATEGORY = ?", new String[]{familyMember, category});
    }
}
