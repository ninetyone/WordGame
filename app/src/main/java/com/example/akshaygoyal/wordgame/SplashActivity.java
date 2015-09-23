package com.example.akshaygoyal.wordgame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.akshaygoyal.wordgame.data.WordsContract;

public class SplashActivity extends AppCompatActivity {


    public static final String MY_PREFS = "MyPrefs";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPreferences = getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);

        if (!sharedPreferences.contains("count")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("count", 1);
        }
        if (!sharedPreferences.contains("last_updated_row")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("last_updated_row", 1);
        }
        if (!sharedPreferences.contains("last_high_score")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("last_high_score", 20);
        }
        if (!sharedPreferences.contains("score_flag")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("score_flag", false);
        }
        //if (!sharedPreferences.contains("current_score")) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("current_score", 20);
        editor.apply();
        //}

        int count = sharedPreferences.getInt("count", 1);
        Cursor cursor = getContentResolver().query(WordsContract.WordsEntry.CONTENT_URI, new String[]{
                WordsContract.WordsEntry.COLUMN_ID}, WordsContract.WordsEntry._ID + " >= ?", new String[]{String.valueOf(count)}, WordsContract.WordsEntry._ID + " ASC");


        if (cursor == null) {
            new FetchWords(this).execute();
        } else if ((cursor.getCount() - count) < 5) {
            new FetchWords(this).execute();
        }
        int timer = 2000;
        if (sharedPreferences.getBoolean("first_time", true)) {
            timer = 5000;
            sharedPreferences.edit().putBoolean("first_time", false).apply();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, timer);
    }
}
