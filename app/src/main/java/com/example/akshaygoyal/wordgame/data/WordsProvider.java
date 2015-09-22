package com.example.akshaygoyal.wordgame.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by akshaygoyal on 9/21/15.
 */
public class WordsProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WordsDbHelper mOpenHelper;

    static final int WORDS = 100;
    static final int WORDS_WITH_ID = 101;

    private static final String sIdSelection = WordsContract.WordsEntry.TABLE_NAME + "." + WordsContract.WordsEntry._ID + " = ? ";
    private static final SQLiteQueryBuilder sWordQueryBuilder;

    static{
        sWordQueryBuilder = new SQLiteQueryBuilder();
        sWordQueryBuilder.setTables(WordsContract.WordsEntry.TABLE_NAME);
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WordsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, WordsContract.PATH_WORDS, WORDS);
        matcher.addURI(authority, WordsContract.PATH_WORDS + "/*", WORDS_WITH_ID);

        return matcher;
    }

    private Cursor getWordsById(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WordsContract.WordsEntry.getIdFromUri(uri);

        return sWordQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sIdSelection,
                new String[]{locationSetting},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WordsDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WORDS:
                return WordsContract.WordsEntry.CONTENT_TYPE;
            case WORDS_WITH_ID:
                return WordsContract.WordsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "words/#"
            case WORDS_WITH_ID:
            {
                retCursor = getWordsById(uri, projection, sortOrder);
                break;
            }
            //words
            case WORDS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WordsContract.WordsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WORDS: {
                long _id = db.insert(WordsContract.WordsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = WordsContract.WordsEntry.buildWordsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // if selection == null, it deletes all rows
        if ( null == selection ) selection = "1";
        switch (match) {
            case WORDS:
                rowsDeleted = db.delete(
                        WordsContract.WordsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case WORDS:
                rowsUpdated = db.update(WordsContract.WordsEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case WORDS_WITH_ID:
                rowsUpdated = db.update(WordsContract.WordsEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WORDS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(WordsContract.WordsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

}
