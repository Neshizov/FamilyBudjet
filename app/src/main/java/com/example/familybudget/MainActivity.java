package com.example.familybudget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    Button buttonViewCharts, buttonViewIncome, buttonViewSavings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        buttonViewCharts = findViewById(R.id.buttonViewCharts);
        buttonViewIncome = findViewById(R.id.buttonViewIncome);
        buttonViewSavings = findViewById(R.id.buttonViewSavings);

        buttonViewCharts.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChartsActivity.class)));

        buttonViewIncome.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, IncomeActivity.class)));

        buttonViewSavings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SavingsActivity.class)));

    }
}
