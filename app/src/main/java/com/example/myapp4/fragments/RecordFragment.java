package com.example.myapp4.fragments;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.myapp4.DatabaseHelper;
import com.example.myapp4.R;
import com.example.myapp4.Record;
import com.example.myapp4.RecordAdapter;

import java.util.*;

public class RecordFragment extends Fragment {

    private EditText etAmount, etNote;
    private Spinner spType, spCategory;
    private TextView tvDate;
    private Button btnPickDate, btnSave;
    private ListView lvRecords;
    private RecordAdapter adapter;
    private DatabaseHelper dbHelper;
    private long selectedTimestamp = System.currentTimeMillis();
    private String username;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_record, container, false);

        etAmount = v.findViewById(R.id.etAmount);
        etNote = v.findViewById(R.id.etNote);
        spType = v.findViewById(R.id.spType);
        spCategory = v.findViewById(R.id.spCategory);
        tvDate = v.findViewById(R.id.tvDate);
        btnPickDate = v.findViewById(R.id.btnPickDate);
        btnSave = v.findViewById(R.id.btnSave);
        lvRecords = v.findViewById(R.id.lvRecords);

        dbHelper = new DatabaseHelper(requireContext());

        // 读取登录用户名（使用 SharedPreferences 存储的 logged_in_user）
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE);
        username = prefs.getString("logged_in_user", null);

        // spinner 初始化（确保 strings.xml 中有 type_array 和 category_array）
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.category_array, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        tvDate.setText(new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selectedTimestamp)));

        btnPickDate.setOnClickListener(view -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(selectedTimestamp);
            new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
                Calendar cc = Calendar.getInstance();
                cc.set(y, m, d, 0, 0, 0);
                selectedTimestamp = cc.getTimeInMillis();
                tvDate.setText(new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selectedTimestamp)));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 适配器（初始空列表）
        adapter = new RecordAdapter(requireContext(), new ArrayList<>());
        lvRecords.setAdapter(adapter);

        btnSave.setOnClickListener(view -> saveRecord());

        loadRecords();

        return v;
    }

    private void saveRecord() {
        String amtS = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amtS)) {
            Toast.makeText(getContext(), "请输入金额", Toast.LENGTH_SHORT).show();
            return;
        }
        double amt;
        try { amt = Double.parseDouble(amtS); } catch (Exception e) {
            Toast.makeText(getContext(), "金额格式错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username == null) {
            Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = spType.getSelectedItem().toString();
        String cat = spCategory.getSelectedItem().toString();
        String note = etNote.getText().toString().trim();

        new Thread(() -> {
            long id = dbHelper.insertRecord(username, selectedTimestamp, amt, type, cat, note);
            requireActivity().runOnUiThread(() -> {
                if (id != -1) {
                    Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
                    etAmount.setText("");
                    etNote.setText("");
                    loadRecords();
                } else Toast.makeText(getContext(), "保存失败", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

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
            requireActivity().runOnUiThread(() -> adapter.setRecords(list));
        }).start();
    }
}
