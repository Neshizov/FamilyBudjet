package com.example.familybudget;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SavingsActivity extends AppCompatActivity {

    private Spinner spinnerFamilyMember, spinnerGoal;
    private RecyclerView recyclerViewGoals;
    private PieChart pieChart;
    private EditText editTextGoal, editTextAmount, editTextAddAmount;
    private TextView textStartDate, textEndDate;
    private DatabaseHelper databaseHelper;
    private List<SavingGoal> savingGoals;
    private String selectedFamilyMember = "", selectedGoal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings);

        spinnerFamilyMember = findViewById(R.id.spinnerFamilyMember);
        spinnerGoal = findViewById(R.id.spinnerGoal);
        recyclerViewGoals = findViewById(R.id.recyclerViewGoals);
        pieChart = findViewById(R.id.pieChartSavings);
        editTextGoal = findViewById(R.id.editTextGoal);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextAddAmount = findViewById(R.id.editTextAddAmount);
        textStartDate = findViewById(R.id.textStartDate);
        textEndDate = findViewById(R.id.textEndDate);
        databaseHelper = new DatabaseHelper(this);
        savingGoals = new ArrayList<>();

        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(this));
        loadFamilyMembersIntoSpinner();

        findViewById(R.id.buttonAddGoal).setOnClickListener(v -> addSavingGoal());
        findViewById(R.id.buttonAddToSavings).setOnClickListener(v -> addToSavings());
    }

    private void loadFamilyMembersIntoSpinner() {
        Cursor cursor = databaseHelper.getUniqueFamilyMembers();
        ArrayList<String> familyMembers = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                familyMembers.add(cursor.getString(cursor.getColumnIndexOrThrow("FAMILY_MEMBER")));
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, familyMembers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFamilyMember.setAdapter(adapter);

        spinnerFamilyMember.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFamilyMember = parent.getItemAtPosition(position).toString();
                loadSavingsForFamilyMember(selectedFamilyMember);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSavingsForFamilyMember(String familyMember) {
        Cursor cursor = databaseHelper.getSavingsByFamilyMember(familyMember);
        savingGoals.clear();
        List<String> goals = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String goal = cursor.getString(cursor.getColumnIndexOrThrow("GOAL"));
                double targetAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("TARGET_AMOUNT"));
                double currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("CURRENT_AMOUNT"));
                if (currentAmount < targetAmount) {
                    savingGoals.add(new SavingGoal(goal, targetAmount, currentAmount));
                    goals.add(goal);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        updateRecyclerView();
        updatePieChart();

        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, goals);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(goalAdapter);

        spinnerGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGoal = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateRecyclerView() {
        SavingGoalsAdapter adapter = new SavingGoalsAdapter(savingGoals);
        recyclerViewGoals.setAdapter(adapter);
    }

    private void updatePieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (SavingGoal goal : savingGoals) {
            float percentage = (float) (goal.getCurrentAmount() / goal.getTargetAmount() * 100);
            entries.add(new PieEntry(percentage, goal.getGoal()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Цели накоплений");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void addSavingGoal() {
        String familyMemberInput = ((EditText) findViewById(R.id.editTextFamilyMember)).getText().toString().trim();
        String familyMember = !familyMemberInput.isEmpty() ? familyMemberInput : selectedFamilyMember;

        String goal = editTextGoal.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();

        if (familyMember.isEmpty() || goal.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректную сумму!", Toast.LENGTH_SHORT).show();
            return;
        }

        String startDate = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        String endDate = new SimpleDateFormat("dd.MM.yyyy").format(cal.getTime());

        databaseHelper.insertSavingGoal(familyMember, goal, amount, 0, startDate, endDate);
        loadSavingsForFamilyMember(familyMember);

        Toast.makeText(this, "Цель добавлена!", Toast.LENGTH_SHORT).show();
    }

    private void addToSavings() {
        String amountStr = editTextAddAmount.getText().toString().trim();

        if (selectedGoal.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Выберите цель и введите сумму для добавления!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректную сумму!", Toast.LENGTH_SHORT).show();
            return;
        }

        SavingGoal savingGoal = databaseHelper.getSavingGoal(selectedFamilyMember, selectedGoal);
        if (savingGoal == null) {
            Toast.makeText(this, "Цель не найдена!", Toast.LENGTH_SHORT).show();
            return;
        }

        double newAmount = savingGoal.getCurrentAmount() + amount;
        if (newAmount >= savingGoal.getTargetAmount()) {
            newAmount = savingGoal.getTargetAmount();
            databaseHelper.markGoalAsComplete(selectedFamilyMember, selectedGoal);
        } else {
            databaseHelper.updateSavingGoal(selectedFamilyMember, selectedGoal, newAmount);
        }

        loadSavingsForFamilyMember(selectedFamilyMember);
        Toast.makeText(this, "Сумма добавлена!", Toast.LENGTH_SHORT).show();
    }
}