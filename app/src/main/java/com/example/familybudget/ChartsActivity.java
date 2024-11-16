package com.example.familybudget;

import android.content.Intent;  // Импортируем для работы с Intent
import android.database.Cursor;
import android.view.View;
import android.os.Bundle;
import android.widget.*;
import java.util.Calendar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChartsActivity extends AppCompatActivity {

    DatabaseHelper db;
    PieChart pieChartTotal;
    Button buttonClearDatabase, buttonDeleteSpecific, buttonDeleteByCategory, buttonAddExpense, buttonBackToMenu;
    EditText editTextFamilyMember, editTextCategory, editTextAmount;
    Spinner spinnerMonth;
    ViewPager viewPager;
    FamilyMemberPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        db = new DatabaseHelper(this);
        pieChartTotal = findViewById(R.id.pieChartTotal);
        viewPager = findViewById(R.id.viewPager);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        buttonClearDatabase = findViewById(R.id.buttonClearDatabase);
        buttonDeleteSpecific = findViewById(R.id.buttonDeleteSpecific);
        buttonDeleteByCategory = findViewById(R.id.buttonDeleteByCategory);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu);  // Добавляем кнопку "В меню"
        editTextFamilyMember = findViewById(R.id.editTextFamilyMember);
        editTextCategory = findViewById(R.id.editTextCategory);
        editTextAmount = findViewById(R.id.editTextAmount);

        // Установка массива месяцев в Spinner
        String[] monthArray = {"Январь 2024", "Февраль 2024", "Март 2024", "Апрель 2024", "Май 2024", "Июнь 2024",
                "Июль 2024", "Август 2024", "Сентябрь 2024", "Октябрь 2024", "Ноябрь 2024", "Декабрь 2024"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // Установка текущего месяца по умолчанию
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        spinnerMonth.setSelection(currentMonth);

        // Установка обработчика выбора месяца
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMonth = spinnerMonth.getSelectedItem().toString();
                loadCharts(selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Обработчики кнопок
        buttonClearDatabase.setOnClickListener(v -> clearDatabase());
        buttonDeleteSpecific.setOnClickListener(v -> deleteSpecificFamilyMember());
        buttonDeleteByCategory.setOnClickListener(v -> deleteByCategory());
        buttonAddExpense.setOnClickListener(v -> addExpense());

        // Обработчик кнопки "В меню"
        buttonBackToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ChartsActivity.this, MainActivity.class);  // Переход в главное меню
            startActivity(intent);  // Запуск activity
            finish();  // Завершаем текущую activity (ChartsActivity)
        });
    }

    private void loadCharts(String selectedMonth) {
        Cursor cursor = db.getExpensesByMonth(selectedMonth);
        Map<String, Double> totalExpenses = new HashMap<>();
        Map<String, Map<String, Double>> memberExpenses = new HashMap<>();

        while (cursor.moveToNext()) {
            String member = cursor.getString(1);
            String category = cursor.getString(2);
            double amount = cursor.getDouble(3);

            totalExpenses.put(member, totalExpenses.getOrDefault(member, 0.0) + amount);
            memberExpenses.putIfAbsent(member, new HashMap<>());
            memberExpenses.get(member).put(category, memberExpenses.get(member).getOrDefault(category, 0.0) + amount);
        }

        drawTotalPieChart(totalExpenses);
        pagerAdapter = new FamilyMemberPagerAdapter(this, memberExpenses);
        viewPager.setAdapter(pagerAdapter);
    }

    private void drawTotalPieChart(Map<String, Double> data) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Общий расход");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChartTotal.setData(pieData);
        pieChartTotal.getDescription().setEnabled(false);
        pieChartTotal.invalidate();
    }

    private void clearDatabase() {
        db.clearExpenses();
        Toast.makeText(this, "База данных очищена", Toast.LENGTH_SHORT).show();
        loadCharts(spinnerMonth.getSelectedItem().toString());
    }

    private void addExpense() {
        String familyMember = editTextFamilyMember.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();
        String selectedMonth = spinnerMonth.getSelectedItem().toString();

        if (!familyMember.isEmpty() && !category.isEmpty() && !amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                db.insertExpense(familyMember, category, amount, selectedMonth);
                Toast.makeText(this, "Расход добавлен", Toast.LENGTH_SHORT).show();
                loadCharts(selectedMonth);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректное значение для суммы", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSpecificFamilyMember() {
        String familyMember = editTextFamilyMember.getText().toString().trim();
        String selectedMonth = spinnerMonth.getSelectedItem().toString();

        if (!familyMember.isEmpty()) {
            db.deleteExpenseByFamilyMemberAndMonth(familyMember, selectedMonth);
            Toast.makeText(this, "Расходы для " + familyMember + " удалены", Toast.LENGTH_SHORT).show();
            loadCharts(selectedMonth);
        } else {
            Toast.makeText(this, "Введите имя члена семьи", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteByCategory() {
        String familyMember = editTextFamilyMember.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        String selectedMonth = spinnerMonth.getSelectedItem().toString();

        if (!familyMember.isEmpty() && !category.isEmpty()) {
            db.deleteExpenseByFamilyMemberAndMonth(familyMember, selectedMonth);
            Toast.makeText(this, "Расходы для " + familyMember + " удалены", Toast.LENGTH_SHORT).show();
            loadCharts(selectedMonth);
        } else {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
        }
    }
}
