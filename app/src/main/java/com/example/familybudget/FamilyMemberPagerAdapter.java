package com.example.familybudget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FamilyMemberPagerAdapter extends PagerAdapter {
    private Context context;
    private List<Map.Entry<String, Map<String, Double>>> familyMemberData;

    public FamilyMemberPagerAdapter(Context context, Map<String, Map<String, Double>> data) {
        this.context = context;
        this.familyMemberData = new ArrayList<>(data.entrySet());
    }

    @Override
    public int getCount() {
        return familyMemberData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.family_member_chart, container, false);
        PieChart pieChart = view.findViewById(R.id.pieChart);

        Map.Entry<String, Map<String, Double>> memberData = familyMemberData.get(position);
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : memberData.getValue().entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, memberData.getKey());
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
