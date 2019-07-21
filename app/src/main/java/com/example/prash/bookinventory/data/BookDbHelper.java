package com.example.prash.bookinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.prash.bookinventory.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    // Name and version of database
    public static final String DATABASE_NAME = "Bookinventory.db";
    public static final int DATABASE_VERSION = 1;

    //Constructor for SQLite Helper class
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //To create a table if it does not exist
    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME +
                "(" + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL," +
                BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL DEFAULT 0," +
                BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                BookEntry.COLUMN_SUPPLIER_NAME + " TEXT," +
                BookEntry.COLUMN_SUPPLIER_PHONE + " TEXT);";

        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    //To override database definition if there is upgrade to table.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldver, int newver) {

    }
}
