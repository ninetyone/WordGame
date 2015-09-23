package com.example.akshaygoyal.wordgame;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.akshaygoyal.wordgame.data.WordsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by akshaygoyal on 9/22/15.
 */
public class FetchWordInfo extends AsyncTask<Void, Void, Void> {


    String LOG_TAG = FetchWordInfo.class.getSimpleName();
    Context mContext;
    Cursor mCursor;
    SharedPreferences mSharedPreferences;


    public FetchWordInfo(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    int id = mCursor.getInt(0);
                    String word = mCursor.getString(2);

                    //limit=3&includeRelated=true&sourceDictionaries=wordnet&useCanonical=false&includeTags=false&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";
                    String base_uri_definition = "http://api.wordnik.com:80/v4/word.json/" + word + "/definitions?";
                    Uri builtUri_defintion = Uri.parse(base_uri_definition).buildUpon()
                            .appendQueryParameter("limit", "4")
                            .appendQueryParameter("includeRelated", "false")
                            .appendQueryParameter("sourceDictionaries", "wordnet")
                            .appendQueryParameter("useCanonical", "false")
                            .appendQueryParameter("includeTags", "false")
                            .appendQueryParameter("api_key", "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5").build();

                    //"http://api.wordnik.com:80/v4/word.json/" + word + "/relatedWords?useCanonical=false&limitPerRelationshipType=10&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";
                    String base_uri_related_words = "http://api.wordnik.com:80/v4/word.json/" + word + "/relatedWords?";
                    Uri builtUri_related_words = Uri.parse(base_uri_related_words).buildUpon()
                            .appendQueryParameter("useCanonical", "false")
                            .appendQueryParameter("relationshipType", "synonym&antonym")
                            .appendQueryParameter("limitPerRelationshipType", "4")
                            .appendQueryParameter("api_key", "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5").build();


                    String definitionJsonStr = connect(builtUri_defintion);
                    String relatedWordsJsonStr = connect(builtUri_related_words);

                    try {
                        getWordInfoFromJson(definitionJsonStr, relatedWordsJsonStr, id);

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        e.printStackTrace();
                    }
                } while (mCursor.moveToNext());
            }
        }

        return null;
    }

    private void getWordInfoFromJson(String definitionJsonStr, String relatedWordsJsonStr, int id) throws JSONException {
        final String KEY_DEF = "text";
        final String KEY_TYPE = "relationshipType";
        final String KEY_WORDS = "words";
        final String KEY_ANTONYM = "antonym";
        final String KEY_SYNONYM = "synonym";


        try {
            JSONArray wordDefintionArray = new JSONArray(definitionJsonStr);
            Vector<ContentValues> cVVector = new Vector<ContentValues>(wordDefintionArray.length());

            String definitions = "";
            String synonyms = "";
            String antonyms = "";

            for (int i = 0; i < wordDefintionArray.length(); i++) {
                JSONObject wordJSONObject = wordDefintionArray.getJSONObject(i);
                definitions += wordJSONObject.getString(KEY_DEF) + ";;";
            }


            JSONArray relatedWordArray = new JSONArray(relatedWordsJsonStr);

            for (int i = 0; i < relatedWordArray.length(); i++) {

                JSONObject wordJSONObject = relatedWordArray.getJSONObject(i);
                String relationType = wordJSONObject.getString(KEY_TYPE);

                String rawStr = "";
                if (relationType.equals(KEY_SYNONYM)) {
                    rawStr = wordJSONObject.getString(KEY_WORDS);
                    rawStr = rawStr.replaceAll("[\\[\\]\"]", "");
                    synonyms += rawStr;
                }
                if (relationType.equals(KEY_ANTONYM)) {
                    rawStr = wordJSONObject.getString(KEY_WORDS);
                    rawStr = rawStr.replaceAll("[\\[\\]\"]", "");
                    antonyms += rawStr;
                }

            }

            ContentValues wordValues = new ContentValues();
            wordValues.put(WordsContract.WordsEntry.COLUMN_DEFINITION, definitions);
            wordValues.put(WordsContract.WordsEntry.COLUMN_SYNONYM, synonyms);
            wordValues.put(WordsContract.WordsEntry.COLUMN_ANTONYM, antonyms);

            int updated = 0;

            updated = mContext.getContentResolver().update(WordsContract.WordsEntry.buildUriFromId(id), wordValues, WordsContract.WordsEntry._ID + " = ?", new String[]{String.valueOf(id)});
            mSharedPreferences = mContext.getSharedPreferences(SplashActivity.MY_PREFS, Context.MODE_PRIVATE);
            mSharedPreferences.edit().putInt("last_updated_row", ++id).apply();

            Log.i(LOG_TAG, "Entry Updated " + id);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private String connect(Uri builtUri) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String wordInfoJsonStr = null;

        try {

            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            Log.i(LOG_TAG, builtUri.toString());

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            wordInfoJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return wordInfoJsonStr;
    }

}
