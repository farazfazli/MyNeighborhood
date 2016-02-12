package com.farazfazli.myneighborhood;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Created by faraz on 2/8/16.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "neighborhood.db";
    private static final int DATABASE_VERSION = 21;

    private static final String CREATE_ENTRIES = "" +
            "CREATE TABLE IF NOT EXISTS neighborhood (_id INTEGER PRIMARY KEY, " +
            "place TEXT UNIQUE, " +
            "city TEXT, " +
            "state TEXT, " +
            "image TEXT UNIQUE, " +
            "favorite TEXT)";

    private static final String ADD_DATA = "" +
            "INSERT INTO neighborhood VALUES" +
            "(NULL, " +
            "?, " +
            "?, " +
            "?, " +
            "?, " +
            "?)";

    private static final String table = "neighborhood";
    private static final String[] ALL_COLUMNS = {"_id", "place", "city", "state", "image", "favorite"};

    private List<String> places = Arrays.asList("Empire State Building", "Times Square", "Brooklyn Bridge");
    private List<String> cities = Arrays.asList("Manhattan", "New York", "Brooklyn");
    private List<String> states = Arrays.asList("New York", "New York", "New York");
    private List<String> images =
            Arrays.asList("https://farm2.staticflickr.com/1614/24560281139_e7d303c178_m.jpg",
                    "https://farm2.staticflickr.com/1660/24832573065_f6d76f05a1_m.jpg",
                    "https://farm3.staticflickr.com/2587/4060252487_da8c9e65c5_m.jpg");

    private static DatabaseHelper helper = null;

    private static final String TAG = "[" + DatabaseHelper.class.getCanonicalName() + "]";

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (helper == null) {
            helper = new DatabaseHelper(context.getApplicationContext());
        }
        return helper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SQLiteStatement sqLiteStatement = db.compileStatement(CREATE_ENTRIES);
        sqLiteStatement.execute();
        insertData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading, deleting all data!");
        db.execSQL("DROP TABLE IF EXISTS neighborhood");
        onCreate(db);
    }

    public void insertData(SQLiteDatabase db) {
        /*
            This is my favorite method. Here I am using a "prepared" statement
            so that I can execute multiple inserts with improved performance &
            security.
         */
        try {
            db.beginTransaction();
            SQLiteStatement sqLiteStatement = db.compileStatement(ADD_DATA);
            for (int i = 0; i < places.size(); i++) {
                sqLiteStatement.clearBindings();
                sqLiteStatement.bindString(1, places.get(i));
                sqLiteStatement.bindString(2, cities.get(i));
                sqLiteStatement.bindString(3, states.get(i));
                sqLiteStatement.bindString(4, images.get(i));
                sqLiteStatement.bindString(5, "NOTSTARRED");
                sqLiteStatement.executeInsert();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Insertion Error: " + e.toString());
        } finally {
            db.endTransaction();
        }
    }

    public void modifyFavorite(int id, boolean star) {
        SQLiteDatabase db = getWritableDatabase();
        if (star) {
            db.execSQL("UPDATE " + table + " SET favorite = 'STARRED' WHERE _id = " + id);
        } else {
            db.execSQL("UPDATE " + table + " SET favorite = 'NOTSTARRED' WHERE _id = " + id);
        }
    }

    public Cursor getById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = "_id = " + id;
        return db.query(table, ALL_COLUMNS, selection, null, null, null, null);
    }

    public Cursor getFavorited(boolean favorited) {
        SQLiteDatabase db = getReadableDatabase();
        if (favorited) {
            return db.query(table, ALL_COLUMNS, "favorite = " + "'STARRED'", null, null, null, null);
        } else {
            return getAllRows();
        }
    }

    public Cursor getAllRows() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(table, ALL_COLUMNS, null, null, null, null, null);
    }

    public Cursor search(String query) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = "";
        for (int i = 1; i < ALL_COLUMNS.length; i++) {
            selection += ALL_COLUMNS[i] + " LIKE ?";
            if (i < ALL_COLUMNS.length - 1) {
                selection += " OR ";
            }
        }
        String[] selectionArgs = {"%" + query + "%", "%" + query + "%", "%" + query + "%", "%" + query + "%"};
        return db.query(table, ALL_COLUMNS, selection, selectionArgs, null, null, null);
    }
}
