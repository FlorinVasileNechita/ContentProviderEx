package com.example.florinnechita.contentproviderex;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TodoTable {

    public static final String TABLE_TODO = "todo";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_SUMMARY = "summary";
    public static final String COLUMN_DESCRIPTION = "description";


    private static final String DATABASE_CREATE = "create table "
            + TABLE_TODO
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_CATEGORY + " text not null, "
            + COLUMN_SUMMARY + " text not null,"
            + COLUMN_DESCRIPTION
            + " text not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.v(TodoTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + " , which will destory all old data");
        database.execSQL("DROP TABLE IF EXITS " + TABLE_TODO);
        onCreate(database);
    }


}
