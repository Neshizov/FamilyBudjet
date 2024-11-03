package com.example.familybudget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Button buttonViewCharts, buttonViewIncome, buttonViewSavings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        buttonViewCharts = findViewById(R.id.buttonViewCharts);
        buttonViewIncome = findViewById(R.id.buttonViewIncome);
        buttonViewSavings = findViewById(R.id.buttonViewSavings);

        buttonViewCharts.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChartsActivity.class)));

        buttonViewIncome.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, IncomeActivity.class)));

        buttonViewSavings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SavingsActivity.class)));

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_income) {
                startActivity(new Intent(MainActivity.this, IncomeActivity.class));
            } else if (itemId == R.id.nav_expenses) {
                startActivity(new Intent(MainActivity.this, ChartsActivity.class));
            } else if (itemId == R.id.nav_savings) {
                startActivity(new Intent(MainActivity.this, SavingsActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }
}
