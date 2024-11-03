package com.example.familybudget;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
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
    Button buttonClearDatabase, buttonDeleteSpecific, buttonDeleteByCategory, buttonAddExpense;
    EditText editTextFamilyMember, editTextCategory, editTextAmount;
    ViewPager viewPager;
    FamilyMemberPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        LinearLayout layout = findViewById(R.id.chartLayout);
        layout.setBackgroundColor(getResources().getColor(R.color.light_blue));

        db = new DatabaseHelper(this);
        pieChartTotal = findViewById(R.id.pieChartTotal);
        viewPager = findViewById(R.id.viewPager);

        buttonClearDatabase = findViewById(R.id.buttonClearDatabase);
        buttonDeleteSpecific = findViewById(R.id.buttonDeleteSpecific);
        buttonDeleteByCategory = findViewById(R.id.buttonDeleteByCategory);
        buttonAddExpense = findViewById(R.id.buttonAddExpense); // Кнопка для добавления расхода
        editTextFamilyMember = findViewById(R.id.editTextFamilyMember);
        editTextCategory = findViewById(R.id.editTextCategory);
        editTextAmount = findViewById(R.id.editTextAmount); // Поле для ввода суммы

        loadCharts();

        buttonClearDatabase.setOnClickListener(v -> clearDatabase());
        buttonDeleteSpecific.setOnClickListener(v -> deleteSpecificFamilyMember());
        buttonDeleteByCategory.setOnClickListener(v -> deleteByCategory());
        buttonAddExpense.setOnClickListener(v -> addExpense()); // Привязка метода добавления
    }

    private void loadCharts() {
        Cursor cursor = db.getAllExpenses();
        Map<String, Double> totalExpenses = new HashMap<>();
        Map<String, Map<String, Double>> memberExpenses = new HashMap<>();

        while (cursor.moveToNext()) {
            String member = cursor.getString(1);
            String category = cursor.getString(2);
            double amount = cursor.getDouble(3);

            totalExpenses.put(member, totalExpenses.getOrDefault(member, 0.0) + amount);

            memberExpenses.putIfAbsent(member, new HashMap<>());
            Map<String, Double> categoryMap = memberExpenses.get(member);
            categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + amount);
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
        int[] customColors = {ColorTemplate.COLORFUL_COLORS[0], ColorTemplate.COLORFUL_COLORS[1],
                ColorTemplate.COLORFUL_COLORS[2], ColorTemplate.COLORFUL_COLORS[3]};
        dataSet.setColors(customColors);
        PieData pieData = new PieData(dataSet);
        pieChartTotal.setData(pieData);
        pieChartTotal.getDescription().setEnabled(false);
        pieChartTotal.invalidate();
    }

    private void clearDatabase() {
        db.clearExpenses();
        Toast.makeText(this, "База данных очищена", Toast.LENGTH_SHORT).show();
        loadCharts();
    }

    private void addExpense() {
        String familyMember = editTextFamilyMember.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();

        if (!familyMember.isEmpty() && !category.isEmpty() && !amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                db.insertExpense(familyMember, category, amount);
                Toast.makeText(this, "Расход добавлен", Toast.LENGTH_SHORT).show();
                loadCharts(); // Обновление графиков
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректное значение для суммы", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSpecificFamilyMember() {
        String familyMember = editTextFamilyMember.getText().toString().trim();
        if (!familyMember.isEmpty()) {
            db.deleteExpenseByFamilyMember(familyMember);
            Toast.makeText(this, "Расходы для " + familyMember + " удалены", Toast.LENGTH_SHORT).show();
            loadCharts();
        } else {
            Toast.makeText(this, "Введите имя члена семьи", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteByCategory() {
        String familyMember = editTextFamilyMember.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        if (!familyMember.isEmpty() && !category.isEmpty()) {
            db.deleteExpenseByFamilyMemberAndCategory(familyMember, category);
            Toast.makeText(this, "Расходы для " + familyMember + " по категории " + category + " удалены", Toast.LENGTH_SHORT).show();
            loadCharts();
        } else {
            Toast.makeText(this, "Введите имя члена семьи и категорию", Toast.LENGTH_SHORT).show();
        }
    }
}
