package com.example.florinnechita.contentproviderex;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by florinnechita on 13/02/17.
 */


// http://www.vogella.com/tutorials/AndroidSQLite/article.html#sqlitedatabase
//https://www.youtube.com/watch?v=bOibA743RDI
//    http://www.mysamplecode.com/2012/11/android-database-content-provider.html
//    https://github.com/ArtemMikhaylov/my_android_notes/wiki/SQLite,-ContentProvider,-CursorLoader
public class MyTodoContentProvider extends ContentProvider {

    private TodoDatabaseHelper database;

    private static final int TODOS = 10;
    private static final int TODO_ID = 20;

    private static final String AUTHORITY = "com.example.florinnechita.contentproviderex";
    private static final String BASE_PATH = "todos";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/todos";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/todo";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, TODOS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TODO_ID);
    }

    @Override
    public boolean onCreate() {
        database = new TodoDatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(TodoTable.TABLE_TODO);

        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case TODOS:
                break;
            case TODO_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(TodoTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);


        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDb = database.getWritableDatabase();

        long id = 0;
        switch (uriType) {
            case TODOS:
                id = sqlDb.insert(TodoTable.TABLE_TODO, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqLiteDb = database.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType) {
            case TODOS:
                rowsDeleted = sqLiteDb.delete(TodoTable.TABLE_TODO, selection, selectionArgs);
                break;
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqLiteDb.delete(TodoTable.TABLE_TODO, TodoTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqLiteDb.delete(TodoTable.TABLE_TODO, TodoTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDb = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case TODOS:
                rowsUpdated = sqlDb.update(TodoTable.TABLE_TODO, values, selection, selectionArgs);
                break;
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDb.update(TodoTable.TABLE_TODO, values, TodoTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDb.update(TodoTable.TABLE_TODO, values, TodoTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {TodoTable.COLUMN_CATEGORY, TodoTable.COLUMN_SUMMARY, TodoTable.COLUMN_DESCRIPTION, TodoTable.COLUMN_ID};

        if (projection != null) {
            HashSet<String> reqestedColumns = new HashSet<>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(available));

            // check if all columns which are requested are available
            if (!availableColumns.containsAll(reqestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }

        }


    }


}
