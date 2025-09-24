package com.example.myapp4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class RecordAdapter extends BaseAdapter {
    private Context context;
    private List<Record> recordList;
    private LayoutInflater inflater;

    public RecordAdapter(Context context, List<Record> recordList) {
        this.context = context;
        this.recordList = recordList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return recordList.size(); }

    @Override
    public Object getItem(int position) { return recordList.get(position); }

    @Override
    public long getItemId(int position) { return recordList.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_record, parent, false);
            h = new ViewHolder();
            h.ivCategory = convertView.findViewById(R.id.ivCategory);
            h.tvNoteOrCategory = convertView.findViewById(R.id.tvNoteOrCategory);
            h.tvDate = convertView.findViewById(R.id.tvDate);
            h.tvAmount = convertView.findViewById(R.id.tvAmount);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        Record r = recordList.get(position);

        // 显示备注或类别
        h.tvNoteOrCategory.setText(r.getNote().isEmpty() ? r.getCategory() : r.getNote());

        // 日期
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(r.getTimestamp()));
        h.tvDate.setText(dateStr);

        // 金额
        if ("支出".equals(r.getType())) {
            h.tvAmount.setText("-" + r.getAmount());
            h.tvAmount.setTextColor(0xFFE91E63); // 红色
        } else {
            h.tvAmount.setText("+" + r.getAmount());
            h.tvAmount.setTextColor(0xFF4CAF50); // 绿色
        }

        // 类别图标
        int iconRes = getCategoryIcon(r.getCategory());
        h.ivCategory.setImageResource(iconRes);

        return convertView;
    }

    private int getCategoryIcon(String category) {
        switch (category) {
            case "工资": return R.drawable.ic_category_salary;
            case "餐饮": return R.drawable.ic_category_food;
            case "交通": return R.drawable.ic_category_transport;
            case "购物": return R.drawable.ic_category_shopping;
            case "娱乐": return R.drawable.ic_category_entertainment;
            default: return R.drawable.ic_category_shopping;
        }
    }

    public void setRecords(List<Record> list) {
        this.recordList = list;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView ivCategory;
        TextView tvNoteOrCategory, tvDate, tvAmount;
    }
}
