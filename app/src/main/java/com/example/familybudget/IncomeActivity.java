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

public class IncomeActivity extends AppCompatActivity {

    DatabaseHelper db;
    PieChart pieChartTotalIncome;
    Button buttonClearDatabase, buttonDeleteSpecific, buttonAddIncome; // Добавлена кнопка
    EditText editTextFamilyMember, editTextSource, editTextAmount;
    ViewPager viewPager;
    FamilyMemberPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        LinearLayout layout = findViewById(R.id.chartLayout);
        layout.setBackgroundColor(getResources().getColor(R.color.light_blue));

        db = new DatabaseHelper(this);
        pieChartTotalIncome = findViewById(R.id.pieChartIncome);
        viewPager = findViewById(R.id.viewPager);

        buttonClearDatabase = findViewById(R.id.buttonClearDatabase);
        buttonDeleteSpecific = findViewById(R.id.buttonDeleteSpecific);
        buttonAddIncome = findViewById(R.id.buttonAddIncome); // Инициализация кнопки
        editTextFamilyMember = findViewById(R.id.editTextFamilyMember);
        editTextSource = findViewById(R.id.editTextSource);
        editTextAmount = findViewById(R.id.editTextAmount);

        loadIncomeCharts();

        buttonClearDatabase.setOnClickListener(v -> clearIncomeDatabase());
        buttonDeleteSpecific.setOnClickListener(v -> deleteSpecificIncomeFamilyMember());

        // Обработчик для добавления дохода
        buttonAddIncome.setOnClickListener(v -> {
            String familyMember = editTextFamilyMember.getText().toString().trim();
            String source = editTextSource.getText().toString().trim();
            String amountString = editTextAmount.getText().toString().trim();

            if (familyMember.isEmpty() || source.isEmpty() || amountString.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountString);
            if (db.addIncome(familyMember, source, amount)) {
                Toast.makeText(this, "Доход добавлен", Toast.LENGTH_SHORT).show();
                loadIncomeCharts();
            } else {
                Toast.makeText(this, "Ошибка при добавлении дохода", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadIncomeCharts() {
        Cursor cursor = db.getAllIncomes();
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
            pagerAdapter = new FamilyMemberPagerAdapter(this, memberIncomes);
            viewPager.setAdapter(pagerAdapter);
        } else {
            Toast.makeText(this, "Нет доходов для отображения", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawTotalPieChart(Map<String, Double> data) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Общий доход");
        int[] customColors = {ColorTemplate.COLORFUL_COLORS[0], ColorTemplate.COLORFUL_COLORS[1],
                ColorTemplate.COLORFUL_COLORS[2], ColorTemplate.COLORFUL_COLORS[3]};
        dataSet.setColors(customColors);
        PieData pieData = new PieData(dataSet);
        pieChartTotalIncome.setData(pieData);
        pieChartTotalIncome.getDescription().setEnabled(false);
        pieChartTotalIncome.invalidate();
    }

    private void clearIncomeDatabase() {
        db.clearIncomes();
        Toast.makeText(this, "База данных доходов очищена", Toast.LENGTH_SHORT).show();
        loadIncomeCharts();
    }

    private void deleteSpecificIncomeFamilyMember() {
        String familyMember = editTextFamilyMember.getText().toString().trim();
        if (!familyMember.isEmpty()) {
            db.deleteIncomeByFamilyMember(familyMember);
            Toast.makeText(this, "Доходы для " + familyMember + " удалены", Toast.LENGTH_SHORT).show();
            loadIncomeCharts();
        } else {
            Toast.makeText(this, "Введите имя члена семьи", Toast.LENGTH_SHORT).show();
        }
    }
}
