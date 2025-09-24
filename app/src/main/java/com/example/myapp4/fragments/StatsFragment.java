package com.example.myapp4.fragments;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapp4.DatabaseHelper;
import com.example.myapp4.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private PieChart pieChart;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, container, false);

        pieChart = v.findViewById(R.id.pieChart);
        dbHelper = new DatabaseHelper(requireContext());

        setupPieChart();
        loadChartData();

        return v;
    }

    private void setupPieChart() {
        // 去掉默认的说明文字
        pieChart.getDescription().setEnabled(false);

        // 中间显示标题
        pieChart.setDrawHoleEnabled(true);
        pieChart.setCenterText("收支统计");
        pieChart.setCenterTextSize(18f);

        // 设置图例（在底部显示，水平排列）
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieChart.setEntryLabelTextSize(12f); // 饼块上的文字大小
    }

    private void loadChartData() {
        float income = 0f;
        float expense = 0f;

        // 从数据库读取收入和支出
        Cursor c = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_RECORDS,
                new String[]{DatabaseHelper.COLUMN_RECORD_TYPE, DatabaseHelper.COLUMN_RECORD_AMOUNT},
                null, null, null, null, null
        );

        if (c != null) {
            while (c.moveToNext()) {
                String type = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_TYPE));
                double amount = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_AMOUNT));
                if ("收入".equals(type)) {
                    income += amount;
                } else if ("支出".equals(type)) {
                    expense += amount;
                }
            }
            c.close();
        }

        // 转换为 PieEntry
        List<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry(income, "收入"));
        if (expense > 0) entries.add(new PieEntry(expense, "支出"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // 使用内置配色
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // 刷新
    }
}
