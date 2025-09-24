package com.example.myapp4.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapp4.DatabaseHelper;
import com.example.myapp4.R;
import com.example.myapp4.Record;
import com.example.myapp4.RecordAdapter;

import java.util.*;

public class QueryFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvAppName;

    private TextView tvMonth;
    private CheckBox cbAllDate;
    private TextView tvTypeCategory;

    private EditText etMinAmount, etMaxAmount;
    private ImageView ivSearchAmount;

    private ListView lvResults;

    private DatabaseHelper dbHelper;
    private RecordAdapter adapter;
    private String username;

    // 当前筛选条件
    private int selectedYear, selectedMonth;
    private boolean allDate = true;
    private String currentType = "全部";
    private String currentCategory = "全部";
    private Double minAmount = null, maxAmount = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_query, container, false);

        // 绑定顶部主题栏
        ivAvatar = v.findViewById(R.id.ivAvatar);
        tvAppName = v.findViewById(R.id.tvAppName);

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE);
        username = prefs.getString("logged_in_user", null);
        int avatarRes = prefs.getInt("logged_in_avatar", R.drawable.ic_avatar_default);
        ivAvatar.setImageResource(avatarRes);
        tvAppName.setText("思思记账");

        // 绑定查询相关控件
        tvMonth = v.findViewById(R.id.tvMonth);
        cbAllDate = v.findViewById(R.id.cbAllDate);
        tvTypeCategory = v.findViewById(R.id.tvTypeCategory);

        etMinAmount = v.findViewById(R.id.etMinAmount);
        etMaxAmount = v.findViewById(R.id.etMaxAmount);
        ivSearchAmount = v.findViewById(R.id.ivSearchAmount);

        lvResults = v.findViewById(R.id.lvResults);

        dbHelper = new DatabaseHelper(requireContext());

        // 初始化年月
        Calendar c = Calendar.getInstance();
        selectedYear = c.get(Calendar.YEAR);
        selectedMonth = c.get(Calendar.MONTH) + 1;
        updateMonthText();

        // 初始化列表
        adapter = new RecordAdapter(requireContext(), new ArrayList<>());
        lvResults.setAdapter(adapter);

        // 日期选择
        tvMonth.setOnClickListener(v1 -> {
            Calendar cc = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(requireContext(),
                    (dpView, y, m, d) -> {
                        selectedYear = y;
                        selectedMonth = m + 1;
                        updateMonthText();
                        loadFilteredRecords();
                    },
                    cc.get(Calendar.YEAR), cc.get(Calendar.MONTH), cc.get(Calendar.DAY_OF_MONTH));
            // 这里只取年月，忽略日
            dp.show();
        });

        // 所有日期开关
        cbAllDate.setOnCheckedChangeListener((btn, checked) -> {
            allDate = checked;
            loadFilteredRecords();
        });

        // 类型类别选择
        tvTypeCategory.setOnClickListener(v1 -> showTypeCategoryDialog());

        // 金额范围查询
        ivSearchAmount.setOnClickListener(v1 -> {
            String minStr = etMinAmount.getText().toString().trim();
            String maxStr = etMaxAmount.getText().toString().trim();
            minAmount = minStr.isEmpty() ? null : Double.parseDouble(minStr);
            maxAmount = maxStr.isEmpty() ? null : Double.parseDouble(maxStr);
            loadFilteredRecords();
        });

        loadFilteredRecords();

        return v;
    }

    private void updateMonthText() {
        tvMonth.setText(selectedYear + "-" + (selectedMonth < 10 ? "0" + selectedMonth : selectedMonth));
    }

    private void showTypeCategoryDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_type_category, null);
        Spinner spType = dialogView.findViewById(R.id.spType);
        Spinner spCategory = dialogView.findViewById(R.id.spCategory);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.type_array_with_all, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.category_array_with_all, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        // 设置默认值
        int typeIndex = Arrays.asList(getResources().getStringArray(R.array.type_array_with_all)).indexOf(currentType);
        if (typeIndex >= 0) spType.setSelection(typeIndex);
        int catIndex = Arrays.asList(getResources().getStringArray(R.array.category_array_with_all)).indexOf(currentCategory);
        if (catIndex >= 0) spCategory.setSelection(catIndex);

        new AlertDialog.Builder(requireContext())
                .setTitle("选择类型和类别")
                .setView(dialogView)
                .setPositiveButton("确定", (d, w) -> {
                    currentType = spType.getSelectedItem().toString();
                    currentCategory = spCategory.getSelectedItem().toString();
                    tvTypeCategory.setText(currentType + "：" + currentCategory);
                    loadFilteredRecords();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadFilteredRecords() {
        if (username == null) return;

        Long startTs = null, endTs = null;
        if (!allDate) {
            Calendar start = Calendar.getInstance();
            start.set(selectedYear, selectedMonth - 1, 1, 0, 0, 0);
            Calendar end = Calendar.getInstance();
            end.set(selectedYear, selectedMonth - 1, start.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            startTs = start.getTimeInMillis();
            endTs = end.getTimeInMillis();
        }

        Cursor c = dbHelper.queryRecordsFiltered(username, startTs, endTs,
                currentType, currentCategory, minAmount, maxAmount);

        List<Record> list = new ArrayList<>();
        if (c != null) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_ID));
                long ts = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_TIMESTAMP));
                double amount = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_AMOUNT));
                String type = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_TYPE));
                String category = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_CATEGORY));
                String note = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_NOTE));
                list.add(new Record(id, ts, amount, type, category, note));
            }
            c.close();
        }
        adapter.setRecords(list);
    }
}
