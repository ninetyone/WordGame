package com.example.akshaygoyal.wordgame;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        TextView question = (TextView) getActivity().findViewById(R.id.question);
        TextView hint1 = (TextView) getActivity().findViewById(R.id.hint1);
        TextView hint2 = (TextView) getActivity().findViewById(R.id.hint2);
        TextView hin3 = (TextView) getActivity().findViewById(R.id.hint3);
        EditText answer = (EditText) getActivity().findViewById(R.id.answer);
        Button submit = (Button) getActivity().findViewById(R.id.submit);

        new FetchWords().execute();


        return rootView;

    }

    class FetchWords extends AsyncTask<Void, Void, Void> {

        String LOG_TAG = FetchWords.class.getSimpleName();


        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String wordsJsonStr = null;
            try {
                String base_uri = "http://api.wordnik.com:80/v4/words.json/randomWords?";
                Uri builtUri = Uri.parse(base_uri).buildUpon()
                        .appendQueryParameter("hasDictionaryDef", "true")
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
                for(int i = 0; i < wordArray.length(); i++) {
                    JSONObject wordJSONObject = wordArray.getJSONObject(i);
                    String word = wordJSONObject.getString(KEY_WORD);
                    int id = wordJSONObject.getInt(KEY_ID);
                    Log.i(LOG_TAG, word);
                }


            }catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }
}
