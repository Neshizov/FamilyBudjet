package com.example.familybudget;

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
    PieChart pieChartTotalIncome;
    Button buttonClearDatabase, buttonDeleteSpecific, buttonAddIncome;
    EditText editTextFamilyMember, editTextSource, editTextAmount;
    Spinner spinnerMonth; // Новый элемент для выбора месяца
    ViewPager viewPager;
    FamilyMemberPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        db = new DatabaseHelper(this);
        pieChartTotalIncome = findViewById(R.id.pieChartIncome);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        viewPager = findViewById(R.id.viewPager);
        buttonClearDatabase = findViewById(R.id.buttonClearDatabase);
        buttonDeleteSpecific = findViewById(R.id.buttonDeleteSpecific);
        buttonAddIncome = findViewById(R.id.buttonAddIncome);
        editTextFamilyMember = findViewById(R.id.editTextFamilyMember);
        editTextSource = findViewById(R.id.editTextSource);
        editTextAmount = findViewById(R.id.editTextAmount);

        // Установка массива месяцев в Spinner
        String[] monthArray = {"Январь 2024", "Февраль 2024", "Март 2024", "Апрель 2024", "Май 2024", "Июнь 2024",
                "Июль 2024", "Август 2024", "Сентябрь 2024", "Октябрь 2024", "Ноябрь 2024", "Декабрь 2024"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // Установка текущего месяца по умолчанию
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH); // Получаем текущий месяц (0-11)
        spinnerMonth.setSelection(currentMonth); // Устанавливаем текущий месяц в Spinner

        // Обработчики кнопок
        buttonClearDatabase.setOnClickListener(v -> clearIncomeDatabase());
        buttonDeleteSpecific.setOnClickListener(v -> deleteSpecificIncomeFamilyMember());
        buttonAddIncome.setOnClickListener(v -> addIncome());

        // Загрузка данных для выбранного месяца
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
            Map<String, Map<String, Double>> memberIncomes = new HashMap<>();

            while (cursor.moveToNext()) {
                String member = cursor.getString(1);
                String source = cursor.getString(2);
                double amount = cursor.getDouble(3);

                totalIncomes.put(member, totalIncomes.getOrDefault(member, 0.0) + amount);
                memberIncomes.putIfAbsent(member, new HashMap<>());
                Map<String, Double> sourceMap = memberIncomes.get(member);
                sourceMap.put(source, sourceMap.getOrDefault(source, 0.0) + amount);
            }
            cursor.close();

            drawTotalPieChart(totalIncomes);

            // Обновляем ViewPager с новыми данными
            pagerAdapter = new FamilyMemberPagerAdapter(this, memberIncomes);
            viewPager.setAdapter(pagerAdapter);
        } else {
            // Если нет данных по доходам, рисуем пустой график
            drawEmptyPieChart();
            Toast.makeText(this, "Нет доходов для выбранного месяца", Toast.LENGTH_SHORT).show();

            // Обновляем ViewPager с пустыми данными
            pagerAdapter = new FamilyMemberPagerAdapter(this, new HashMap<>());
            viewPager.setAdapter(pagerAdapter);
        }
    }


    private void drawEmptyPieChart() {
        // Рисуем пустой график с одним сектором для наглядности
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(0f, "Нет данных"));

        PieDataSet dataSet = new PieDataSet(entries, "Общий доход");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChartTotalIncome.setData(pieData);
        pieChartTotalIncome.getDescription().setEnabled(false);
        pieChartTotalIncome.invalidate();
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

        // Проверка на выбор месяца
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
