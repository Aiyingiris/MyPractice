package com.example.mycalendarapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mycalendarapp.model.Event;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CalendarDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_EVENTS = "events";

    // 列名
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESC = "description";
    private static final String KEY_START = "startTime";
    private static final String KEY_END = "endTime";
    private static final String KEY_REMIND = "remindTime";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT,"
                + KEY_DESC + " TEXT,"
                + KEY_START + " INTEGER,"
                + KEY_END + " INTEGER,"
                + KEY_REMIND + " INTEGER" + ")";
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    // 添加日程
    public long addEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, event.getTitle());
        values.put(KEY_DESC, event.getDescription());
        values.put(KEY_START, event.getStartTime());
        values.put(KEY_END, event.getEndTime());
        values.put(KEY_REMIND, event.getRemindTime());

        long id = db.insert(TABLE_EVENTS, null, values);
        db.close();
        return id;
    }

    // 获取特定日期的所有日程
    public List<Event> getEventsByDay(long startOfDay, long endOfDay) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询逻辑：开始时间在当天范围内
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS + " WHERE " +
                KEY_START + " >= " + startOfDay + " AND " +
                KEY_START + " < " + endOfDay + " ORDER BY " + KEY_START + " ASC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event(
                        cursor.getString(1), // title
                        cursor.getString(2), // desc
                        cursor.getLong(3),   // start
                        cursor.getLong(4),   // end
                        cursor.getLong(5)    // remind
                );
                event.setId(cursor.getInt(0));
                eventList.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventList;
    }

    // 删除日程
    public boolean deleteEvent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_EVENTS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;  // 根据实际删除的行数返回结果
    }

    public boolean updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, event.getTitle());
        values.put(KEY_DESC, event.getDescription());
        values.put(KEY_START, event.getStartTime());
        values.put(KEY_END, event.getEndTime());
        values.put(KEY_REMIND, event.getRemindTime());

        int rows = db.update(TABLE_EVENTS, values, KEY_ID + " = ?", new String[]{String.valueOf(event.getId())});
        db.close();
        return rows > 0;
    }
}



