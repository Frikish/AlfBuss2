package com.alfsimen.bybuss;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    class Row extends Object {
        public String title;
        public String date;
        public String content;
        public int id;
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "bybuss";
    private static final String DATABASE_TABLE = "history";
    private static final String DATABASE_TABLE_URL = "history_url";
    private static final String DATABASE_TABLE_ANSWER = "answer";

    private static final String DATABASE_CREATE =
            "CREATE TABLE " + DATABASE_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "content TEXT," +
                    "date TEXT" +
                    ");";

    private static final String DATABASE_CREATE_URL =
            "CREATE TABLE " + DATABASE_TABLE_URL + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "content TEXT," +
                    "date TEXT" +
                    ");";

    public void close() {
        getWritableDatabase().close();
    }

    public void createRow(String title, String link, String description) {
        ContentValues cv = new ContentValues();

        cv.put("title", title);
        cv.put("link", link);
        cv.put("description", description);

        getWritableDatabase().insert(DATABASE_TABLE, null, cv);
    }

    public void deleteRow(long rowId) {
        getWritableDatabase().delete(DATABASE_TABLE, "_id=" + rowId, null);
    }

    public Row fetchRow(int rowId) {
        Row row = new Row();
        Cursor c = getWritableDatabase().query(true, DATABASE_TABLE, new String[]{"_id", "title", "content", "date"}, "_id=" + rowId, null, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            row.id = c.getInt(0);
            row.title = c.getString(1);
            row.content = c.getString(2);
            row.date = c.getString(3);
            c.close();
            return row;
        } else {
            row.id = -1;
            row.title = row.content = row.date = null;
        }
        c.close();
        return row;
    }

    public int getCount() {
        Cursor c = getWritableDatabase().query(DATABASE_TABLE, new String[]{"_id", "title", "content", "date"}, null, null, null, null, null);
        return c.getCount();
    }

    public Boolean checkRow(String title) {
        Cursor c = getWritableDatabase().query(true, DATABASE_TABLE, new String[]{"_id", "title", "content", "date"}, "title='" + title + "'", null, null, null, null, null);
        if (c.getCount() > 0) {
            c.close();
            return true;
        }
        c.close();
        return false;
    }

    public Cursor getAllRows() {
        try {
            return getWritableDatabase().query(DATABASE_TABLE, new String[]{"_id", "title", "content", "date"}, null, null, null, null, null);
        } catch (SQLiteException e) {
            //Log.e("Exception on query", e.toString());
            return null;
        }
    }

    // HISTORY STUFF

    public Cursor getAllHistoryRows() {
        try {
            return getReadableDatabase().query(DATABASE_TABLE_URL, new String[]{"_id", "title", "content", "date"}, null, null, null, null, null);
        } catch (SQLiteException e) {
           // Log.e("Exception on query", e.toString());
            getReadableDatabase().close();
            return null;
        }
    }

    public Boolean checkHistoryItem(String url) {
        Cursor c = getWritableDatabase().query(true, DATABASE_TABLE_URL, new String[]{"_id", "title", "content", "date"}, "link='" + url + "'", null, null, null, null, null);
        if (c.getCount() > 0) {
            c.close();
            return true;
        }
        c.close();
        return false;
    }

    public void createHistoryItem(String title, String content) {
        ContentValues cv = new ContentValues();

        cv.put("title", title);
        cv.put("content", content);
        cv.put("date", "00");

        getWritableDatabase().insert(DATABASE_TABLE_URL, null, cv);
    }

    public Row fetchHistoryRow(long rowId) {
        Row row = new Row();
        Cursor c = getWritableDatabase().query(true, DATABASE_TABLE_URL, new String[]{"_id", "title", "content", "date"}, "_id=" + rowId, null, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            row.id = c.getInt(0);
            row.title = c.getString(1);
            row.content = c.getString(2);
            row.date = c.getString(3);
            c.close();
            return row;
        } else {
            row.id = -1;
            row.title = row.content = row.date = null;
        }
        c.close();
        return row;
    }

    public int getHistoryItemId(String item) {
        Cursor c = getReadableDatabase().query(true, DATABASE_TABLE_URL, new String[]{"_id", "title"}, "title='" + item + "'", null, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            c.close();
            return c.getInt(0);
        } else {
            c.close();
            return -1;
        }
    }

    public int deleteHistoryRow(long itemId) {
        return getWritableDatabase().delete(DATABASE_TABLE_URL, "_id=" + itemId, null);
    }

    public int deleteHistoryRow(String search) {
        return getWritableDatabase().delete(DATABASE_TABLE_URL, "title=" + search, null);
    }

    // ANSWER STUFF

    public void overWriteLastAnswer(String answer) {
        getWritableDatabase().delete(DATABASE_TABLE_ANSWER, null, null);
        ContentValues cv = new ContentValues();
        cv.put("content", answer);
        getWritableDatabase().insert(DATABASE_TABLE_ANSWER, null, cv);
    }

    public String getLastAnswer() {
        Cursor c = getWritableDatabase().query(DATABASE_TABLE_ANSWER, new String[] {"content"}, null, null, null, null, null);
        if(c.getCount() > 0) {
            c.moveToFirst();
            return c.getString(0);
        }
        else
            return "";
    }

    //THE REST

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        db.execSQL(DATABASE_CREATE_URL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //android.util.Log.w("Constants", "Upgrading database, which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CREATE);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CREATE_URL);
        onCreate(db);
    }
}