package com.example.akshaygoyal.wordgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnGameClearedListener  {

    public static final String MY_PREFS = "MyPrefs" ;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
        if (!sharedPreferences.contains("count")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("count", 1);
            editor.commit();
        }
        if (!sharedPreferences.contains("last_updated_row")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("last_updated_row", 1);
            editor.commit();
        }
        if (!sharedPreferences.contains("last_high_score")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("last_high_score", 20);
            editor.commit();
        }
        if (!sharedPreferences.contains("score_flag")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("score_flag", false);
            editor.commit();
        }
        //if (!sharedPreferences.contains("current_score")) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("current_score", 20);
        editor.commit();
        //}

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new MainActivityFragment(), "Game Fragment")
                .commit();
        setContentView(R.layout.activity_main);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGameCleared(boolean next) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new MainActivityFragment(), "Game Fragment")
                .commit();
    }
}
