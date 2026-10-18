package com.akhilesh.smartremainder;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reminders.db";
    private static final int DATABASE_VERSION = 4; // Incremented version

    public static final String TABLE_NAME = "reminders";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PURPOSE = "purpose";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DATE = "date"; // Added column
    public static final String COLUMN_REPEAT = "repeat_type";
    public static final String COLUMN_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PURPOSE + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_REPEAT + " TEXT, " +
                COLUMN_STATUS + " TEXT DEFAULT 'Pending')";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        if (oldVersion < 2) {
            db.execSQL(
                    "ALTER TABLE " + TABLE_NAME +
                    " ADD COLUMN " + COLUMN_STATUS +
                    " TEXT DEFAULT 'Pending'"
            );
        }
        if (oldVersion < 4) {
            db.execSQL(
                    "ALTER TABLE " + TABLE_NAME +
                    " ADD COLUMN " + COLUMN_DATE +
                    " TEXT"
            );
        }
    }

    public long addReminder(
            String purpose,
            String time,
            String date,
            String repeat) {

        SQLiteDatabase db =
                this.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(COLUMN_PURPOSE, purpose);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_REPEAT, repeat);
        values.put(COLUMN_STATUS, "Pending");

        return db.insert(
                TABLE_NAME,
                null,
                values
        );
    }

    public void updateStatus(
            long id,
            String status) {

        SQLiteDatabase db =
                this.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(COLUMN_STATUS, status);

        db.update(
                TABLE_NAME,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();
    }

    public void deleteReminder(long id) {

        SQLiteDatabase db =
                this.getWritableDatabase();

        db.delete(
                TABLE_NAME,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();
    }
}