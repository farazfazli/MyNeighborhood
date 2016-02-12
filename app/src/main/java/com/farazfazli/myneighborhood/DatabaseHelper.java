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
    private static final int DATABASE_VERSION = 12;

    private static final String CREATE_ENTRIES = "" +
            "CREATE TABLE IF NOT EXISTS neighborhood (_id INTEGER PRIMARY KEY, " +
            "place TEXT UNIQUE, " +
            "city TEXT, " +
            "state TEXT, " +
            "image TEXT UNIQUE" +
            "favorite TEXT)";

    private static final String ADD_DATA = "" +
            "INSERT INTO neighborhood VALUES" +
            "(NULL, " +
            "?, " +
            "?, " +
            "?, " +
            "?)";

    private static final String table = "neighborhood";
    private static final String[] ALL_COLUMNS = {"_id", "place", "city", "state", "image"};

    private List<String> places = Arrays.asList("Empire State Building", "NYSE Data Center", "Fireworks");
    private List<String> cities = Arrays.asList("New York", "Mahwah", "Philadelphia");
    private List<String> states = Arrays.asList("New York", "New Jersey", "Pennsylvania");
    private List<String> images =
            Arrays.asList("http://ingridwu.dmmdmcfatter.com/wp-content/uploads/2015/01/placeholder.png",
                    "http://ingridwu.dmmdmcfatter.com/wp-content/uploads/2015/01/placeholder.png/",
                    "http://www.ingridwu.dmmdmcfatter.com/wp-content/uploads/2015/01/placeholder.png");

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
                sqLiteStatement.executeInsert();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Insertion Error: " + e.toString());
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = "_id = ?";
        String[] selectionArgs = {"'" + Integer.toString(id) + "'"};
        return db.query(table, ALL_COLUMNS, selection, selectionArgs, null, null, null);
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
