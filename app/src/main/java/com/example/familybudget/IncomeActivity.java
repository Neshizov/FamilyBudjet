package com.example.familybudget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class IncomeActivity extends AppCompatActivity {

    DatabaseHelper db;
    PieChart pieChartTotalIncome, pieChartCategoryIncome;
    Button buttonClearDatabase, buttonDeleteSpecific, buttonAddIncome, buttonBackToMenu, buttonDeleteByCategory; // Добавлена кнопка для возврата в меню
    EditText editTextFamilyMember, editTextSource, editTextAmount;
    Spinner spinnerMonth;


    ViewPager viewPager;
    FamilyMemberPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        db = new DatabaseHelper(this);
        pieChartCategoryIncome = findViewById(R.id.pieChartCategoryIncome);
        pieChartTotalIncome = findViewById(R.id.pieChartIncome);
        spinnerMonth = findViewById(R.id.spinnerMonth);

        viewPager = findViewById(R.id.viewPager);
        buttonClearDatabase = findViewById(R.id.buttonClearDatabase);
        buttonDeleteByCategory = findViewById(R.id.buttonDeleteByCategory);
        buttonDeleteSpecific = findViewById(R.id.buttonDeleteSpecific);
        buttonAddIncome = findViewById(R.id.buttonAddIncome);
        editTextFamilyMember = findViewById(R.id.editTextFamilyMember);
        editTextSource = findViewById(R.id.editTextSource);
        editTextAmount = findViewById(R.id.editTextAmount);
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu);


        buttonBackToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(IncomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        buttonDeleteByCategory.setOnClickListener(v -> deleteIncomeByCategory());

        String[] monthArray = {"Январь 2024", "Февраль 2024", "Март 2024", "Апрель 2024", "Май 2024", "Июнь 2024",
                "Июль 2024", "Август 2024", "Сентябрь 2024", "Октябрь 2024", "Ноябрь 2024", "Декабрь 2024"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        spinnerMonth.setSelection(currentMonth);

        buttonClearDatabase.setOnClickListener(v -> clearIncomeDatabase());
        buttonDeleteSpecific.setOnClickListener(v -> deleteSpecificIncomeFamilyMember());
        buttonAddIncome.setOnClickListener(v -> addIncome());

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMonth = spinnerMonth.getSelectedItem().toString();
                loadIncomeCharts(selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadIncomeCharts(String selectedMonth) {
        Cursor cursor = db.getIncomesByMonth(selectedMonth);
        if (cursor != null && cursor.getCount() > 0) {
            Map<String, Double> totalIncomes = new HashMap<>();
            Map<String, Double> categoryIncomes = new HashMap<>();
            Map<String, Map<String, Double>> memberIncomes = new HashMap<>();

            while (cursor.moveToNext()) {
                String member = cursor.getString(1);
                String category = cursor.getString(2);
                double amount = cursor.getDouble(3);

                totalIncomes.put(member, totalIncomes.getOrDefault(member, 0.0) + amount);
                categoryIncomes.put(category, categoryIncomes.getOrDefault(category, 0.0) + amount);

                memberIncomes.putIfAbsent(member, new HashMap<>());
                Map<String, Double> sourceMap = memberIncomes.get(member);
                sourceMap.put(category, sourceMap.getOrDefault(category, 0.0) + amount);
            }
            cursor.close();

            drawTotalPieChart(totalIncomes);
            drawCategoryPieChart(categoryIncomes);

            pagerAdapter = new FamilyMemberPagerAdapter(this, memberIncomes);
            viewPager.setAdapter(pagerAdapter);
        } else {
            drawEmptyPieChart();
            Toast.makeText(this, "Нет доходов для выбранного месяца", Toast.LENGTH_SHORT).show();
            pagerAdapter = new FamilyMemberPagerAdapter(this, new HashMap<>());
            viewPager.setAdapter(pagerAdapter);
        }
    }

    private void deleteIncomeByCategory() {
        String familyMember = editTextFamilyMember.getText().toString().trim();
        String selectedMonth = spinnerMonth.getSelectedItem().toString();
        String category = editTextSource.getText().toString().trim();

        if (!familyMember.isEmpty() && !category.isEmpty()) {
            db.deleteIncomeByCategory(familyMember, category, selectedMonth);
            Toast.makeText(this, "Доходы для " + familyMember + " по категории " + category + " удалены", Toast.LENGTH_SHORT).show();
            loadIncomeCharts(selectedMonth);
        } else {
            Toast.makeText(this, "Пожалуйста, укажите члена семьи и категорию", Toast.LENGTH_SHORT).show();
        }
    }



    private void drawCategoryPieChart(Map<String, Double> data) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Доходы по категориям");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChartCategoryIncome.setData(pieData);
        pieChartCategoryIncome.getDescription().setEnabled(false);
        pieChartCategoryIncome.invalidate();
    }


    private void drawEmptyPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(0f, "Нет данных"));

        PieDataSet dataSet = new PieDataSet(entries, "Доходы");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChartTotalIncome.setData(pieData);
        pieChartCategoryIncome.setData(pieData);
        pieChartTotalIncome.getDescription().setEnabled(false);
        pieChartCategoryIncome.getDescription().setEnabled(false);
        pieChartTotalIncome.invalidate();
        pieChartCategoryIncome.invalidate();
    }

    private void drawTotalPieChart(Map<String, Double> data) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Общий доход");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChartTotalIncome.setData(pieData);
        pieChartTotalIncome.getDescription().setEnabled(false);
        pieChartTotalIncome.invalidate();
    }

    private void clearIncomeDatabase() {
        db.clearIncomes();
        Toast.makeText(this, "База данных доходов очищена", Toast.LENGTH_SHORT).show();
        loadIncomeCharts(spinnerMonth.getSelectedItem().toString());
    }

    private void addIncome() {
        String familyMember = editTextFamilyMember.getText().toString().trim();
        String source = editTextSource.getText().toString().trim();
        String amountString = editTextAmount.getText().toString().trim();

        Object selectedMonthObject = spinnerMonth.getSelectedItem();
        if (selectedMonthObject == null) {
            Toast.makeText(this, "Пожалуйста, выберите месяц", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedMonth = selectedMonthObject.toString();

        if (!familyMember.isEmpty() && !source.isEmpty() && !amountString.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountString);
                if (db.addIncome(familyMember, source, amount, selectedMonth)) {
                    Toast.makeText(this, "Доход добавлен", Toast.LENGTH_SHORT).show();
                    loadIncomeCharts(selectedMonth);
                } else {
                    Toast.makeText(this, "Ошибка при добавлении дохода", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректное значение для суммы", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSpecificIncomeFamilyMember() {
        String familyMember = editTextFamilyMember.getText().toString().trim();
        String selectedMonth = spinnerMonth.getSelectedItem().toString();

        if (!familyMember.isEmpty()) {
            db.deleteIncomeByFamilyMemberAndMonth(familyMember, selectedMonth);
            Toast.makeText(this, "Доходы для " + familyMember + " удалены", Toast.LENGTH_SHORT).show();
            loadIncomeCharts(selectedMonth);
        } else {
            Toast.makeText(this, "Пожалуйста, укажите члена семьи", Toast.LENGTH_SHORT).show();
        }
    }
}
