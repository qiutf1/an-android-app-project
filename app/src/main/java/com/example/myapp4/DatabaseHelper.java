package com.example.myapp4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_database";
    private static final int DATABASE_VERSION = 3;

    // users 表
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_AVATAR = "avatar";

    // records 表
    public static final String TABLE_RECORDS = "records";
    public static final String COLUMN_RECORD_ID = "_id";
    public static final String COLUMN_RECORD_USER = "username";
    public static final String COLUMN_RECORD_TIMESTAMP = "timestamp";
    public static final String COLUMN_RECORD_AMOUNT = "amount";
    public static final String COLUMN_RECORD_TYPE = "type";
    public static final String COLUMN_RECORD_CATEGORY = "category";
    public static final String COLUMN_RECORD_NOTE = "note";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USERNAME + " TEXT PRIMARY KEY, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_AVATAR + " TEXT);";

    private static final String SQL_CREATE_RECORDS =
            "CREATE TABLE " + TABLE_RECORDS + " (" +
                    COLUMN_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RECORD_USER + " TEXT, " +
                    COLUMN_RECORD_TIMESTAMP + " INTEGER, " +
                    COLUMN_RECORD_AMOUNT + " REAL, " +
                    COLUMN_RECORD_TYPE + " TEXT, " +
                    COLUMN_RECORD_CATEGORY + " TEXT, " +
                    COLUMN_RECORD_NOTE + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_RECORDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_AVATAR + " TEXT;");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 3) {
            try {
                db.execSQL(SQL_CREATE_RECORDS);
            } catch (Exception ignored) {}
        }
    }

    // 插入用户（注册）
    public long insertUser(String username, String password, int avatarResId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_USERNAME, username);
        v.put(COLUMN_PASSWORD, password);
        v.put(COLUMN_AVATAR, String.valueOf(avatarResId)); // 存储头像资源 ID
        return db.insert(TABLE_USERS, null, v);
    }


    public boolean isUserExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = (c != null && c.moveToFirst());
        if (c != null) c.close();
        return exists;
    }

    public boolean checkUserPassword(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{COLUMN_PASSWORD},
                COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        if (c != null && c.moveToFirst()) {
            String stored = c.getString(c.getColumnIndexOrThrow(COLUMN_PASSWORD));
            c.close();
            return stored.equals(password);
        }
        if (c != null) c.close();
        return false;
    }

    // 记录相关 CRUD
    public long insertRecord(String username, long timestamp, double amount, String type, String category, String note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_RECORD_USER, username);
        v.put(COLUMN_RECORD_TIMESTAMP, timestamp);
        v.put(COLUMN_RECORD_AMOUNT, amount);
        v.put(COLUMN_RECORD_TYPE, type);
        v.put(COLUMN_RECORD_CATEGORY, category);
        v.put(COLUMN_RECORD_NOTE, note);
        return db.insert(TABLE_RECORDS, null, v);
    }

    public Cursor getRecordsForUser(String username) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_RECORDS, null, COLUMN_RECORD_USER + "=?", new String[]{username}, null, null, COLUMN_RECORD_TIMESTAMP + " DESC");
    }

    public int updateRecord(long id, double amount, String type, String category, String note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_RECORD_AMOUNT, amount);
        v.put(COLUMN_RECORD_TYPE, type);
        v.put(COLUMN_RECORD_CATEGORY, category);
        v.put(COLUMN_RECORD_NOTE, note);
        return db.update(TABLE_RECORDS, v, COLUMN_RECORD_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteRecord(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_RECORDS, COLUMN_RECORD_ID + "=?", new String[]{String.valueOf(id)});
    }

    // 新增：按条件查询记录
    public Cursor queryRecordsFiltered(String username,
                                       Long startTimestamp, Long endTimestamp,
                                       String type, String category,
                                       Double minAmount, Double maxAmount) {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder sel = new StringBuilder();
        ArrayList<String> args = new ArrayList<>();

        // 必须按用户筛
        sel.append(COLUMN_RECORD_USER).append("=?");
        args.add(username);

        if (startTimestamp != null && endTimestamp != null) {
            sel.append(" AND ").append(COLUMN_RECORD_TIMESTAMP).append(" BETWEEN ? AND ?");
            args.add(String.valueOf(startTimestamp));
            args.add(String.valueOf(endTimestamp));
        }

        if (type != null && !type.equals("全部")) {
            sel.append(" AND ").append(COLUMN_RECORD_TYPE).append("=?");
            args.add(type);
        }

        if (category != null && !category.equals("全部")) {
            sel.append(" AND ").append(COLUMN_RECORD_CATEGORY).append("=?");
            args.add(category);
        }

        if (minAmount != null) {
            sel.append(" AND ").append(COLUMN_RECORD_AMOUNT).append(">=?");
            args.add(String.valueOf(minAmount));
        }

        if (maxAmount != null) {
            sel.append(" AND ").append(COLUMN_RECORD_AMOUNT).append("<=?");
            args.add(String.valueOf(maxAmount));
        }

        String[] selArgs = args.toArray(new String[0]);
        // 按时间降序
        return db.query(TABLE_RECORDS, null, sel.toString(), selArgs, null, null, COLUMN_RECORD_TIMESTAMP + " DESC");
    }
    // ========== 年视图：每月结余 ==========
    public Cursor getMonthlyBalanceForYear(String username, int yearStartTs, int yearEndTs) {
        SQLiteDatabase db = getReadableDatabase();
        // SQLite 没有直接的 YEAR/MONTH 函数，所以我们用 strftime('%m', timestamp/1000, 'unixepoch') 提取月份
        String sql = "SELECT strftime('%m', " + COLUMN_RECORD_TIMESTAMP + "/1000, 'unixepoch') AS month, " +
                "SUM(CASE WHEN " + COLUMN_RECORD_TYPE + "='收入' THEN " + COLUMN_RECORD_AMOUNT + " ELSE 0 END) AS income, " +
                "SUM(CASE WHEN " + COLUMN_RECORD_TYPE + "='支出' THEN " + COLUMN_RECORD_AMOUNT + " ELSE 0 END) AS expense " +
                "FROM " + TABLE_RECORDS +
                " WHERE " + COLUMN_RECORD_USER + "=? AND " + COLUMN_RECORD_TIMESTAMP + " BETWEEN ? AND ? " +
                "GROUP BY month ORDER BY month ASC";
        return db.rawQuery(sql, new String[]{username, String.valueOf(yearStartTs), String.valueOf(yearEndTs)});
    }

    // ========== 月视图：每日结余 ==========
    public Cursor getDailyBalanceForMonth(String username, long monthStartTs, long monthEndTs) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT strftime('%d', " + COLUMN_RECORD_TIMESTAMP + "/1000, 'unixepoch') AS day, " +
                "SUM(CASE WHEN " + COLUMN_RECORD_TYPE + "='收入' THEN " + COLUMN_RECORD_AMOUNT + " ELSE 0 END) AS income, " +
                "SUM(CASE WHEN " + COLUMN_RECORD_TYPE + "='支出' THEN " + COLUMN_RECORD_AMOUNT + " ELSE 0 END) AS expense " +
                "FROM " + TABLE_RECORDS +
                " WHERE " + COLUMN_RECORD_USER + "=? AND " + COLUMN_RECORD_TIMESTAMP + " BETWEEN ? AND ? " +
                "GROUP BY day ORDER BY day ASC";
        return db.rawQuery(sql, new String[]{username, String.valueOf(monthStartTs), String.valueOf(monthEndTs)});
    }

    // ========== 日视图：某日分类饼图 ==========
    public Cursor getCategorySummaryForDay(String username, long dayStartTs, long dayEndTs) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT " + COLUMN_RECORD_CATEGORY + " AS category, " +
                "SUM(" + COLUMN_RECORD_AMOUNT + ") AS total, " +
                COLUMN_RECORD_TYPE + " AS type " +
                "FROM " + TABLE_RECORDS +
                " WHERE " + COLUMN_RECORD_USER + "=? AND " + COLUMN_RECORD_TIMESTAMP + " BETWEEN ? AND ? " +
                "GROUP BY category, type";
        return db.rawQuery(sql, new String[]{username, String.valueOf(dayStartTs), String.valueOf(dayEndTs)});
    }

}
