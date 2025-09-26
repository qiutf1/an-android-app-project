package com.example.myapp4.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapp4.DatabaseHelper;
import com.example.myapp4.R;
import com.example.myapp4.Record;
import com.example.myapp4.RecordAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.*;

public class RecordFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvAppName;
    private TextView tvAccountName; // ✅ 新增账号显示
    private TextView tabAll, tabExpense, tabIncome;
    private ListView lvRecords;
    private FloatingActionButton fabAdd;

    private RecordAdapter adapter;
    private DatabaseHelper dbHelper;
    private String username;
    private List<Record> allRecords = new ArrayList<>();
    private String currentFilter = "全部";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_record, container, false);

        // 绑定顶部主题栏
        ivAvatar = v.findViewById(R.id.ivAvatar);
        tvAppName = v.findViewById(R.id.tvAppName);
        tvAccountName = v.findViewById(R.id.tvAccountName);

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE);
        username = prefs.getString("logged_in_user", null);
        int avatarRes = prefs.getInt("logged_in_avatar", R.drawable.ic_avatar_default);

        ivAvatar.setImageResource(avatarRes);
        tvAppName.setText("思思记账");
        tvAccountName.setText("账号: " + (username == null ? "未登录" : username));

        // 绑定其他控件
        tabAll = v.findViewById(R.id.tabAll);
        tabExpense = v.findViewById(R.id.tabExpense);
        tabIncome = v.findViewById(R.id.tabIncome);
        lvRecords = v.findViewById(R.id.lvRecords);
        fabAdd = v.findViewById(R.id.fabAdd);

        dbHelper = new DatabaseHelper(requireContext());

        // 适配器
        adapter = new RecordAdapter(requireContext(), new ArrayList<>());
        lvRecords.setAdapter(adapter);

        // Tab 切换
        tabAll.setOnClickListener(v1 -> { currentFilter = "全部"; applyFilter(); });
        tabExpense.setOnClickListener(v1 -> { currentFilter = "支出"; applyFilter(); });
        tabIncome.setOnClickListener(v1 -> { currentFilter = "收入"; applyFilter(); });

        // 新增账单
        fabAdd.setOnClickListener(v1 -> showAddOrEditDialog(null));

        // 点击修改
        lvRecords.setOnItemClickListener((parent, view, position, id) -> {
            Record r = (Record) adapter.getItem(position);
            showAddOrEditDialog(r);
        });

        // 长按删除
        lvRecords.setOnItemLongClickListener((parent, view, position, id) -> {
            Record r = (Record) adapter.getItem(position);
            new AlertDialog.Builder(requireContext())
                    .setTitle("删除记录")
                    .setMessage("确定要删除这条记录吗？")
                    .setPositiveButton("删除", (d, w) -> {
                        dbHelper.deleteRecord(r.getId());
                        loadRecords();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });

        loadRecords();

        return v;
    }

    /** 加载数据库记录 */
    private void loadRecords() {
        if (username == null) return;
        new Thread(() -> {
            Cursor c = dbHelper.getRecordsForUser(username);
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
            allRecords = list;
            requireActivity().runOnUiThread(this::applyFilter);
        }).start();
    }

    /** 应用筛选条件 */
    private void applyFilter() {
        List<Record> filtered = new ArrayList<>();
        for (Record r : allRecords) {
            if ("全部".equals(currentFilter)) filtered.add(r);
            else if (r.getType().equals(currentFilter)) filtered.add(r);
        }
        adapter.setRecords(filtered);
    }

    /** 弹出添加或编辑对话框 */
    private void showAddOrEditDialog(@Nullable Record record) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_record, null);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Spinner spType = dialogView.findViewById(R.id.spType);
        Spinner spCategory = dialogView.findViewById(R.id.spCategory);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);

        // 类型选择
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        // 类别选择
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.category_array, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        // 用数组包装，避免 lambda final 限制
        final long[] selectedTs = {System.currentTimeMillis()};
        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selectedTs[0])));

        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(selectedTs[0]);
            new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
                Calendar cc = Calendar.getInstance();
                cc.set(y, m, d, 0, 0, 0);
                selectedTs[0] = cc.getTimeInMillis();
                tvDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selectedTs[0])));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 如果是修改，填充旧数据
        if (record != null) {
            etAmount.setText(String.valueOf(record.getAmount()));
            etNote.setText(record.getNote());
            tvDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(record.getTimestamp())));
            spType.setSelection("支出".equals(record.getType()) ? 0 : 1);
            String[] categories = getResources().getStringArray(R.array.category_array);
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(record.getCategory())) {
                    spCategory.setSelection(i);
                    break;
                }
            }
            selectedTs[0] = record.getTimestamp();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(record == null ? "新增账单" : "修改账单")
                .setView(dialogView)
                .setPositiveButton("保存", (d, w) -> {
                    String amtS = etAmount.getText().toString().trim();
                    if (TextUtils.isEmpty(amtS)) {
                        Toast.makeText(getContext(), "请输入金额", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double amt = Double.parseDouble(amtS);
                    String type = spType.getSelectedItem().toString();
                    String cat = spCategory.getSelectedItem().toString();
                    String note = etNote.getText().toString().trim();

                    if (record == null) {
                        dbHelper.insertRecord(username, selectedTs[0], amt, type, cat, note);
                    } else {
                        dbHelper.updateRecord(record.getId(), amt, type, cat, note);
                    }
                    loadRecords();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
