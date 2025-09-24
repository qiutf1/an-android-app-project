package com.example.myapp4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordAdapter extends BaseAdapter {
    private Context context;
    private List<Record> recordList;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public RecordAdapter(Context context, List<Record> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    public void setRecords(List<Record> newRecords) {
        this.recordList = newRecords;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return recordList == null ? 0 : recordList.size();
    }

    @Override
    public Object getItem(int position) {
        return recordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return recordList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_record, parent, false);
            vh = new ViewHolder();
            vh.tvAmount = convertView.findViewById(R.id.tvAmount);
            vh.tvType = convertView.findViewById(R.id.tvType);
            vh.tvCategory = convertView.findViewById(R.id.tvCategory);
            vh.tvDate = convertView.findViewById(R.id.tvDate);
            vh.tvNote = convertView.findViewById(R.id.tvNote);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        Record r = recordList.get(position);
        vh.tvAmount.setText(String.format(Locale.getDefault(), "金额: %.2f", r.getAmount()));
        vh.tvType.setText("类型: " + (r.getType() == null ? "" : r.getType()));
        vh.tvCategory.setText("类别: " + (r.getCategory() == null ? "" : r.getCategory()));
        vh.tvNote.setText("备注: " + (r.getNote() == null ? "" : r.getNote()));
        String dateStr = sdf.format(new Date(r.getTimestamp()));
        vh.tvDate.setText("日期: " + dateStr);

        return convertView;
    }

    static class ViewHolder {
        TextView tvAmount, tvType, tvCategory, tvDate, tvNote;
    }
}
