package com.example.myapp4;

public class Record {
    private long id;
    private long timestamp;
    private double amount;
    private String type;
    private String category;
    private String note;

    public Record(long id, long timestamp, double amount, String type, String category, String note) {
        this.id = id;
        this.timestamp = timestamp;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
    }

    public long getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
}
