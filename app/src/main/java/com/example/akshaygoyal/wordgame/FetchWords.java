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
class FetchWords extends AsyncTask<Void, Void, Void> {

    String LOG_TAG = FetchWords.class.getSimpleName();
    Context mContext;


    public FetchWords(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String wordsJsonStr = null;
        try {
            String base_uri = "http://api.wordnik.com:80/v4/words.json/randomWords?";
            Uri builtUri = Uri.parse(base_uri).buildUpon()
                    .appendQueryParameter("hasDictionaryDef", "true")
                    .appendQueryParameter("includePartOfSpeech", "adjective")
                    .appendQueryParameter("minCorpusCount", "0")
                    .appendQueryParameter("maxCorpusCount", "-1")
                    .appendQueryParameter("minDictionaryCount", "100")
                    .appendQueryParameter("maxDictionaryCount", "-1")
                    .appendQueryParameter("minLength", "5")
                    .appendQueryParameter("maxLength", "-1")
                    .appendQueryParameter("limit", "10")
                    .appendQueryParameter("api_key", "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5").build();

            URL url = new URL(builtUri.toString());


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
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            wordsJsonStr = buffer.toString();
            getWordsFromJson(wordsJsonStr);
        }catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            //return null;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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
        return null;
    }

    private void getWordsFromJson(String wordsJsonStr) throws JSONException{

        final String KEY_WORD = "word";
        final String KEY_ID = "id";

        try {
            JSONArray wordArray = new JSONArray(wordsJsonStr);
            Vector<ContentValues> cVVector = new Vector<ContentValues>(wordArray.length());

            for(int i = 0; i < wordArray.length(); i++) {
                JSONObject wordJSONObject = wordArray.getJSONObject(i);
                String word = wordJSONObject.getString(KEY_WORD);
                int id = wordJSONObject.getInt(KEY_ID);


                ContentValues wordValues = new ContentValues();
                wordValues.put(WordsContract.WordsEntry.COLUMN_ID, id);
                wordValues.put(WordsContract.WordsEntry.COLUMN_WORD, word);
                cVVector.add(wordValues);
            }
            int inserted = 0;

            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(WordsContract.WordsEntry.CONTENT_URI, cvArray);
            }
            Log.d(LOG_TAG, "Words insertion Complete. " + inserted + " Inserted");

        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SplashActivity.MY_PREFS, Context.MODE_PRIVATE);
        int count = sharedPreferences.getInt("last_updated_row", 1);
        Cursor cursor = mContext.getContentResolver().query(WordsContract.WordsEntry.CONTENT_URI, null, WordsContract.WordsEntry._ID + " >= ?", new String[]{String.valueOf(count)}, WordsContract.WordsEntry._ID + " ASC");

        new FetchWordInfo(mContext, cursor).execute();
    }
}