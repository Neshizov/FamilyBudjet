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
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + EXPENSES_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, FAMILY_MEMBER TEXT, CATEGORY TEXT, AMOUNT REAL)");
        db.execSQL("CREATE TABLE " + INCOME_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, FAMILY_MEMBER TEXT, SOURCE TEXT, AMOUNT REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EXPENSES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + INCOME_TABLE);
        onCreate(db);
    }

    public boolean addIncome(String familyMember, String source, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("FAMILY_MEMBER", familyMember);
        contentValues.put("SOURCE", source);
        contentValues.put("AMOUNT", amount);

        long result = db.insert(INCOME_TABLE, null, contentValues); // Use INCOME_TABLE constant here
        return result != -1;
    }



    public boolean insertExpense(String familyMember, String category, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("FAMILY_MEMBER", familyMember);
        contentValues.put("CATEGORY", category);
        contentValues.put("AMOUNT", amount);
        long result = db.insert(EXPENSES_TABLE, null, contentValues);
        return result != -1;
    }

    public void clearIncomes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(INCOME_TABLE, null, null);
    }

    public void deleteIncomeByFamilyMember(String familyMember) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(INCOME_TABLE, "FAMILY_MEMBER = ?", new String[]{familyMember});
    }


    public Cursor getAllIncomes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + INCOME_TABLE, null);
    }


    public boolean insertIncome(String familyMember, String source, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("FAMILY_MEMBER", familyMember);
        contentValues.put("SOURCE", source);
        contentValues.put("AMOUNT", amount);
        long result = db.insert(INCOME_TABLE, null, contentValues);
        return result != -1;
    }

    public Cursor getAllExpenses() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + EXPENSES_TABLE, null);
    }

    public void clearExpenses() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EXPENSES_TABLE, null, null);
    }

    public void deleteExpenseByFamilyMember(String familyMember) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EXPENSES_TABLE, "FAMILY_MEMBER = ?", new String[]{familyMember});
    }

    public void deleteExpenseByFamilyMemberAndCategory(String familyMember, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EXPENSES_TABLE, "FAMILY_MEMBER = ? AND CATEGORY = ?", new String[]{familyMember, category});
    }
}
