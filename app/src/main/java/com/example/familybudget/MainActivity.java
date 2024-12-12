package com.example.familybudget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    Button buttonViewIncome, buttonViewCharts, buttonViewSavings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        buttonViewIncome = findViewById(R.id.buttonViewIncome);
        buttonViewCharts = findViewById(R.id.buttonViewCharts);
        buttonViewSavings = findViewById(R.id.buttonViewSavings);

        buttonViewIncome.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, IncomeActivity.class)));

        buttonViewCharts.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChartsActivity.class)));

        buttonViewSavings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SavingsActivity.class)));

    }
}
