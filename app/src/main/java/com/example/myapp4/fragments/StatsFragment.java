package com.example.myapp4.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapp4.DatabaseHelper;
import com.example.myapp4.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvAppName;
    private TextView tvAccountName; // ✅ 新增账号显示
    private RadioGroup rgViewType;
    private FrameLayout statsContainer;
    private DatabaseHelper dbHelper;
    private String username;

    // 当前选择（默认当前日期）
    private int selectedYear, selectedMonth, selectedDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);

        ivAvatar = root.findViewById(R.id.ivAvatar);
        tvAppName = root.findViewById(R.id.tvAppName);
        tvAccountName = root.findViewById(R.id.tvAccountName); // ✅ 绑定账号 TextView
        rgViewType = root.findViewById(R.id.rgViewType);
        statsContainer = root.findViewById(R.id.statsContainer);

        // DB & user
        dbHelper = new DatabaseHelper(requireContext());
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", getActivity().MODE_PRIVATE);
        username = prefs.getString("logged_in_user", null);
        int avatarRes = prefs.getInt("logged_in_avatar", R.drawable.ic_avatar_default);

        ivAvatar.setImageResource(avatarRes);
        tvAppName.setText("思思记账");
        tvAccountName.setText("账号: " + (username == null ? "未登录" : username));

        // 当前日期
        Calendar c = Calendar.getInstance();
        selectedYear = c.get(Calendar.YEAR);
        selectedMonth = c.get(Calendar.MONTH) + 1;
        selectedDay = c.get(Calendar.DAY_OF_MONTH);

        // 初始：年视图
        showYearView();

        // 单选切换
        rgViewType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbYear) {
                showYearView();
            } else if (checkedId == R.id.rbMonth) {
                showMonthView();
            } else if (checkedId == R.id.rbDay) {
                showDayView();
            }
        });

        return root;
    }

    // ---------------- 年视图 ----------------
    private void showYearView() {
        View yearView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_stats_year, statsContainer, false);
        statsContainer.removeAllViews();
        statsContainer.addView(yearView);

        TextView tvYear = yearView.findViewById(R.id.tvYear);

        int pickYearId = getResources().getIdentifier("btnPickYear", "id", requireContext().getPackageName());
        View btnPickYear = pickYearId != 0 ? yearView.findViewById(pickYearId) : null;
        if (btnPickYear != null) {
            btnPickYear.setOnClickListener(v -> showYearPicker(tvYear, yearView));
        }

        tvYear.setText(String.valueOf(selectedYear));
        updateYearStats(yearView);
    }

    private void showYearPicker(TextView tvYear, View root) {
        NumberPicker np = new NumberPicker(requireContext());
        np.setMinValue(2000);
        np.setMaxValue(2100);
        np.setValue(selectedYear);

        new AlertDialog.Builder(requireContext())
                .setTitle("选择年份")
                .setView(np)
                .setPositiveButton("确定", (d, w) -> {
                    selectedYear = np.getValue();
                    tvYear.setText(String.valueOf(selectedYear));
                    updateYearStats(root);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateYearStats(View root) {
        TextView tvIncome = root.findViewById(R.id.tvIncome);
        TextView tvExpense = root.findViewById(R.id.tvExpense);
        TextView tvBalance = root.findViewById(R.id.tvBalance);
        LineChart lineChart = root.findViewById(R.id.lineChartYear);

        lineChart.clear();
        lineChart.getDescription().setEnabled(false);

        float totalIncome = 0f;
        float totalExpense = 0f;
        List<Entry> entries = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            Calendar start = Calendar.getInstance();
            start.set(selectedYear, month - 1, 1, 0, 0, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = Calendar.getInstance();
            end.set(selectedYear, month - 1, start.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            end.set(Calendar.MILLISECOND, 999);

            Cursor c = dbHelper.queryRecordsFiltered(username, start.getTimeInMillis(), end.getTimeInMillis(), null, null, null, null);

            double monthIncome = 0;
            double monthExpense = 0;
            if (c != null) {
                while (c.moveToNext()) {
                    String type = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_TYPE));
                    double amt = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_AMOUNT));
                    if ("收入".equals(type)) monthIncome += amt;
                    else if ("支出".equals(type)) monthExpense += amt;
                }
                c.close();
            }

            totalIncome += (float) monthIncome;
            totalExpense += (float) monthExpense;
            float balance = (float) (monthIncome - monthExpense);
            entries.add(new Entry(month, balance));
        }

        tvIncome.setText(String.format(Locale.getDefault(), "收入: %.2f", totalIncome));
        tvExpense.setText(String.format(Locale.getDefault(), "支出: %.2f", totalExpense));
        tvBalance.setText(String.format(Locale.getDefault(), "结余: %.2f", totalIncome - totalExpense));

        LineDataSet set = new LineDataSet(entries, "月结余");
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setValueTextSize(10f);
        set.setColors(ColorTemplate.MATERIAL_COLORS);

        LineData data = new LineData(set);
        lineChart.setData(data);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12, true);

        lineChart.invalidate();
    }

    // ---------------- 月视图 ----------------
    private void showMonthView() {
        View monthView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_stats_month, statsContainer, false);
        statsContainer.removeAllViews();
        statsContainer.addView(monthView);

        TextView tvMonth = monthView.findViewById(R.id.tvMonth);

        int pickMonthId = getResources().getIdentifier("btnPickMonth", "id", requireContext().getPackageName());
        View btnPickMonth = pickMonthId != 0 ? monthView.findViewById(pickMonthId) : null;
        if (btnPickMonth != null) {
            btnPickMonth.setOnClickListener(v -> showMonthPicker(tvMonth, monthView));
        }

        tvMonth.setText(String.format(Locale.getDefault(), "%04d-%02d", selectedYear, selectedMonth));
        updateMonthStats(monthView);
    }

    private void showMonthPicker(TextView tvMonth, View root) {
        NumberPicker npY = new NumberPicker(requireContext());
        NumberPicker npM = new NumberPicker(requireContext());
        npY.setMinValue(2000);
        npY.setMaxValue(2100);
        npY.setValue(selectedYear);
        npM.setMinValue(1);
        npM.setMaxValue(12);
        npM.setValue(selectedMonth);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.addView(npY);
        layout.addView(npM);

        new AlertDialog.Builder(requireContext())
                .setTitle("选择年月")
                .setView(layout)
                .setPositiveButton("确定", (d, w) -> {
                    selectedYear = npY.getValue();
                    selectedMonth = npM.getValue();
                    tvMonth.setText(String.format(Locale.getDefault(), "%04d-%02d", selectedYear, selectedMonth));
                    updateMonthStats(root);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateMonthStats(View root) {
        TextView tvIncome = root.findViewById(R.id.tvIncome);
        TextView tvExpense = root.findViewById(R.id.tvExpense);
        TextView tvBalance = root.findViewById(R.id.tvBalance);
        LineChart lineChart = root.findViewById(R.id.lineChartMonth);

        lineChart.clear();
        lineChart.getDescription().setEnabled(false);

        Calendar cal = Calendar.getInstance();
        cal.set(selectedYear, selectedMonth - 1, 1);
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        float totalIncome = 0f;
        float totalExpense = 0f;
        List<Entry> entries = new ArrayList<>();

        for (int day = 1; day <= days; day++) {
            Calendar start = Calendar.getInstance();
            start.set(selectedYear, selectedMonth - 1, day, 0, 0, 0);
            start.set(Calendar.MILLISECOND, 0);
            Calendar end = Calendar.getInstance();
            end.set(selectedYear, selectedMonth - 1, day, 23, 59, 59);
            end.set(Calendar.MILLISECOND, 999);

            Cursor c = dbHelper.queryRecordsFiltered(username, start.getTimeInMillis(), end.getTimeInMillis(), null, null, null, null);
            double dayIncome = 0;
            double dayExpense = 0;
            if (c != null) {
                while (c.moveToNext()) {
                    String type = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_TYPE));
                    double amt = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_AMOUNT));
                    if ("收入".equals(type)) dayIncome += amt;
                    else if ("支出".equals(type)) dayExpense += amt;
                }
                c.close();
            }
            totalIncome += (float) dayIncome;
            totalExpense += (float) dayExpense;
            entries.add(new Entry(day, (float) (dayIncome - dayExpense)));
        }

        tvIncome.setText(String.format(Locale.getDefault(), "收入: %.2f", totalIncome));
        tvExpense.setText(String.format(Locale.getDefault(), "支出: %.2f", totalExpense));
        tvBalance.setText(String.format(Locale.getDefault(), "结余: %.2f", totalIncome - totalExpense));

        LineDataSet set = new LineDataSet(entries, "日结余");
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setValueTextSize(10f);
        set.setColors(ColorTemplate.MATERIAL_COLORS);

        LineData data = new LineData(set);
        lineChart.setData(data);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(days, 10), true);

        lineChart.invalidate();
    }

    // ---------------- 日视图 ----------------
    private void showDayView() {
        View dayView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_stats_day, statsContainer, false);
        statsContainer.removeAllViews();
        statsContainer.addView(dayView);

        TextView tvDayMonth = dayView.findViewById(R.id.tvDayMonth);

        int pickDayId = getResources().getIdentifier("btnPickDay", "id", requireContext().getPackageName());
        View btnPickDay = pickDayId != 0 ? dayView.findViewById(pickDayId) : null;
        if (btnPickDay != null) {
            btnPickDay.setOnClickListener(v -> {
                DatePickerDialog dp = new DatePickerDialog(requireContext(), (view, y, m, d) -> {
                    selectedYear = y;
                    selectedMonth = m + 1;
                    selectedDay = d;
                    tvDayMonth.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay));
                    updateDayStats(dayView);
                }, selectedYear, selectedMonth - 1, selectedDay);
                dp.show();
            });
        }

        tvDayMonth.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay));
        updateDayStats(dayView);
    }

    private void updateDayStats(View root) {
        TextView tvIncome = root.findViewById(R.id.tvIncome);
        TextView tvExpense = root.findViewById(R.id.tvExpense);
        TextView tvBalance = root.findViewById(R.id.tvBalance);
        PieChart pieChart = root.findViewById(R.id.pieChartDay);

        pieChart.clear();
        pieChart.getDescription().setEnabled(false);

        float totalIncome = 0f;
        float totalExpense = 0f;
        Map<String, Float> categoryMap = new HashMap<>();

        Calendar start = Calendar.getInstance();
        start.set(selectedYear, selectedMonth - 1, selectedDay, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance();
        end.set(selectedYear, selectedMonth - 1, selectedDay, 23, 59, 59);
        end.set(Calendar.MILLISECOND, 999);

        Cursor c = dbHelper.queryRecordsFiltered(username, start.getTimeInMillis(), end.getTimeInMillis(), null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                String type = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_TYPE));
                String category = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_CATEGORY));
                double amt = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_AMOUNT));
                if ("收入".equals(type)) totalIncome += (float) amt;
                else if ("支出".equals(type)) totalExpense += (float) amt;

                float old = categoryMap.containsKey(category) ? categoryMap.get(category) : 0f;
                categoryMap.put(category, old + (float) amt);
            }
            c.close();
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> e : categoryMap.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
        }

        tvIncome.setText(String.format(Locale.getDefault(), "收入: %.2f", totalIncome));
        tvExpense.setText(String.format(Locale.getDefault(), "支出: %.2f", totalExpense));
        tvBalance.setText(String.format(Locale.getDefault(), "结余: %.2f", totalIncome - totalExpense));

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(ColorTemplate.MATERIAL_COLORS);
        ds.setValueTextSize(12f);
        PieData pd = new PieData(ds);
        pieChart.setData(pd);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setWordWrapEnabled(true);

        pieChart.invalidate();
    }
}
