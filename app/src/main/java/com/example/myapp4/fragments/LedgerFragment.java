package com.example.myapp4.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.myapp4.DatabaseHelper;
import com.example.myapp4.R;
import com.example.myapp4.Record;
import com.example.myapp4.RecordAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LedgerFragment extends Fragment {

    private ListView listView;
    private List<Record> recordList;
    private RecordAdapter adapter;
    private DatabaseHelper dbHelper;
    private String currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ledger, container, false);

        listView = v.findViewById(R.id.listView);
        recordList = new ArrayList<>();
        adapter = new RecordAdapter(requireContext(), recordList);
        listView.setAdapter(adapter);

        dbHelper = new DatabaseHelper(requireContext());

        // 从 SharedPreferences 读取登录用户名（请确保 LoginActivity 已写入）
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE);
        currentUser = prefs.getString("logged_in_user", null);

        loadRecords();

        // 长按删除
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Record record = recordList.get(position);
            new AlertDialog.Builder(requireContext())
                    .setTitle("删除确认")
                    .setMessage("确定要删除该记录吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        // 使用 long id
                        long rid = record.getId();
                        dbHelper.deleteRecord(rid);   // 注意 DatabaseHelper.deleteRecord(long)
                        loadRecords();
                        Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });

        // 点击修改（弹出输入框）
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Record record = recordList.get(position);
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_record, null);
            final EditText etAmount = dialogView.findViewById(R.id.etAmount);
            final EditText etType = dialogView.findViewById(R.id.etType);
            final EditText etCategory = dialogView.findViewById(R.id.etCategory);
            final EditText etNote = dialogView.findViewById(R.id.etNote);

            etAmount.setText(String.valueOf(record.getAmount()));
            etType.setText(record.getType());
            etCategory.setText(record.getCategory());
            etNote.setText(record.getNote());

            new AlertDialog.Builder(requireContext())
                    .setTitle("修改账单")
                    .setView(dialogView)
                    .setPositiveButton("保存", (dialog, which) -> {
                        String amtS = etAmount.getText().toString().trim();
                        if (amtS.isEmpty()) {
                            Toast.makeText(requireContext(), "金额不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        double amount;
                        try {
                            amount = Double.parseDouble(amtS);
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "金额格式错误", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String type = etType.getText().toString().trim();
                        String category = etCategory.getText().toString().trim();
                        String note = etNote.getText().toString().trim();

                        long recordId = record.getId(); // long
                        int rows = dbHelper.updateRecord(recordId, amount, type, category, note); // 已修改为 long id 版本
                        if (rows > 0) {
                            Toast.makeText(requireContext(), "修改成功", Toast.LENGTH_SHORT).show();
                            loadRecords();
                        } else {
                            Toast.makeText(requireContext(), "修改失败", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        return v;
    }

    private void loadRecords() {
        recordList.clear();
        if (currentUser == null) return;

        Cursor cursor = dbHelper.getRecordsForUser(currentUser);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 正确使用 long / double 类型读取
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_ID));
                long ts = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_TIMESTAMP));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_AMOUNT));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_TYPE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_CATEGORY));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECORD_NOTE));

                recordList.add(new Record(id, ts, amount, type, category, note));
            }
            cursor.close();
        }

        adapter.setRecords(recordList);
    }
}
