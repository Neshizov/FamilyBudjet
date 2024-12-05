package com.example.familybudget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "family_budget.db";
    private static final String EXPENSES_TABLE = "expenses";
    private static final String INCOME_TABLE = "income";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + EXPENSES_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, FAMILY_MEMBER TEXT, CATEGORY TEXT, AMOUNT REAL, MONTH TEXT)");
        db.execSQL("CREATE TABLE " + INCOME_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, FAMILY_MEMBER TEXT, SOURCE TEXT, AMOUNT REAL, MONTH TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + EXPENSES_TABLE + " ADD COLUMN MONTH TEXT");
            db.execSQL("ALTER TABLE " + INCOME_TABLE + " ADD COLUMN MONTH TEXT");
        }
    }

    public String getCurrentMonth() {
        return android.text.format.DateFormat.format("MM-yyyy", new java.util.Date()).toString();
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
