package com.example.myapp4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.VH> {

    private List<Record> records = new ArrayList<>();
    private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Record r = records.get(position);
        String amt = String.format(Locale.getDefault(), "%.2f", r.amount);
        if ("Expense".equalsIgnoreCase(r.type) || "支出".equalsIgnoreCase(r.type)) holder.tvAmount.setText("-" + amt);
        else holder.tvAmount.setText(amt);

        holder.tvType.setText(r.type);
        holder.tvCategory.setText(r.category + (r.note != null && !r.note.isEmpty() ? " · " + r.note : ""));
        holder.tvDate.setText(fmt.format(new Date(r.timestamp)));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public void setRecords(List<Record> list) {
        this.records = list;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAmount, tvCategory, tvDate, tvType;
        VH(View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}
