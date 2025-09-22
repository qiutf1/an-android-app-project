package com.example.myapp4;

public class Record {
    public long id;
    public long timestamp;
    public double amount;
    public String type;
    public String category;
    public String note;

    public Record(long id, long timestamp, double amount, String type, String category, String note) {
        this.id = id;
        this.timestamp = timestamp;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
    }
}
