package com.example.akshaygoyal.wordgame.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by akshaygoyal on 9/21/15.
 */
public class WordsContract {

    public static final String CONTENT_AUTHORITY = "com.example.akshaygoyal.wordgame";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_WORDS = "words";



    public static final class WordsEntry implements BaseColumns {

        public static final String TABLE_NAME = "words";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_WORD = "word";
        public static final String COLUMN_DEFINITION = "definition";
        public static final String COLUMN_SYNONYM = "synonym";
        public static final String COLUMN_ANTONYM = "antonym";
        public static final String COLUMN_HINTS = "hints";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WORDS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WORDS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WORDS;

        public static Uri buildWordsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUriFromId(int id) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

}
