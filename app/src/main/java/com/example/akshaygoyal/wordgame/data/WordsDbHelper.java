package com.example.akshaygoyal.wordgame.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.akshaygoyal.wordgame.data.WordsContract.WordsEntry;;

/**
 * Created by akshaygoyal on 9/21/15.
 */
public class WordsDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "words.db";

    public WordsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_WORD_TABLE = "CREATE TABLE " + WordsEntry.TABLE_NAME + " (" +
                WordsEntry._ID + " INTEGER PRIMARY KEY, " +
                WordsEntry.COLUMN_ID + " TEXT UNIQUE NOT NULL, " +
                WordsEntry.COLUMN_WORD + " TEXT NOT NULL, " +
                WordsEntry.COLUMN_DEFINITION + " TEXT, " +
                WordsEntry.COLUMN_SYNONYM + " TEXT, " +
                WordsEntry.COLUMN_ANTONYM + " TEXT, " +
                WordsEntry.COLUMN_HINTS + " TEXT" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_WORD_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WordsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
