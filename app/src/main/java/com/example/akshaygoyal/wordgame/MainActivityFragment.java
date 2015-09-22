package com.example.akshaygoyal.wordgame;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.akshaygoyal.wordgame.data.WordsContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private final String SYNONYM = "Synonym";
    private final String ANTONYM = "Antonym";
    private final String DEFINITION = "Definiton";
    OnGameClearedListener mListener;

    private static final String[] WORDS_COLUMNS = {

            WordsContract.WordsEntry.TABLE_NAME + "." + WordsContract.WordsEntry._ID,
            WordsContract.WordsEntry.COLUMN_ID,
            WordsContract.WordsEntry.COLUMN_WORD,
            WordsContract.WordsEntry.COLUMN_DEFINITION,
            WordsContract.WordsEntry.COLUMN_SYNONYM,
            WordsContract.WordsEntry.COLUMN_ANTONYM,
            WordsContract.WordsEntry.COLUMN_HINTS
    };

    public interface OnGameClearedListener {
        public void onGameCleared(boolean next);
    }

    static final int COL_ID = 0;
    static final int COL_WORD_ID = 1;
    static final int COL_WORD = 2;
    static final int COL_DEFINITION = 3;
    static final int COL_SYNONYM = 4;
    static final int COL_ANTONYM = 5;
    static final int COL_HINTS = 6;
    static int count;
    static int hintCount = 0;
    EditText mAnswer;
    TextView mQuestion, mHint1, mHint2, mHint3;
    ImageButton mSubmit, mHint;
    ImageView hintRemaining1, hintRemaining2, hintRemaining3;

    SharedPreferences sharedPreferences;

    public MainActivityFragment() {

    }

    ArrayList<String> hintsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mQuestion = (TextView) rootView.findViewById(R.id.question);
        mHint1 = (TextView) rootView.findViewById(R.id.hint1);
        mHint2 = (TextView) rootView.findViewById(R.id.hint2);
        mHint3 = (TextView) rootView.findViewById(R.id.hint3);
        mAnswer = (EditText) rootView.findViewById(R.id.answer);
        mSubmit = (ImageButton) rootView.findViewById(R.id.submit);
        mHint = (ImageButton) rootView.findViewById(R.id.hint_btn);
        hintRemaining1 = (ImageView) rootView.findViewById(R.id.hint_remaining_1);
        hintRemaining2 = (ImageView) rootView.findViewById(R.id.hint_remaining_2);
        hintRemaining3 = (ImageView) rootView.findViewById(R.id.hint_remaining_3);

        sharedPreferences = getActivity().getSharedPreferences(MainActivity.MY_PREFS, Context.MODE_PRIVATE);
        count = sharedPreferences.getInt("count", 1);

        Cursor cursor = getActivity().getContentResolver().query(WordsContract.WordsEntry.CONTENT_URI, new String[]{
                WordsContract.WordsEntry.COLUMN_ID}, WordsContract.WordsEntry._ID + " >= ?", new String[]{String.valueOf(count)}, WordsContract.WordsEntry._ID + " ASC");

        if (cursor == null)
            new FetchWords(getActivity()).execute();
        else if ((cursor.getCount() - count) < 5)
            new FetchWords(getActivity()).execute();


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 2000);
        cursor = getActivity().getContentResolver().query(WordsContract.WordsEntry.CONTENT_URI, WORDS_COLUMNS, WordsContract.WordsEntry._ID + " = ?", new String[]{String.valueOf(count)}, null);

        String questionStr;
        final ArrayList<String> answersList = new ArrayList<>();
        ArrayList<String> definitionList = new ArrayList<>();
        ArrayList<String> antonymList = new ArrayList<>();
        ArrayList<String> synonymList = new ArrayList<>();
        ArrayList<String> finalHint = new ArrayList<>();


        if (cursor != null)
            if (cursor.moveToFirst()) {

                answersList.add(cursor.getString(COL_WORD));

                populateList(cursor, DEFINITION, definitionList);
                populateList(cursor, SYNONYM, synonymList);
                populateList(cursor, ANTONYM, antonymList);

                if (synonymList != null)
                    answersList.addAll(synonymList);


                Random random = new Random();
                int rand = 0;
                int listRand;
                if (antonymList.size() != 0 && synonymList.size() != 0) {
                    rand = random.nextInt(3);
                } else if (antonymList.size() == 0 && synonymList.size() != 0) {
                    rand = random.nextInt(2);
                } else if (antonymList.size() == 0 && synonymList.size() == 0) {
                    rand = random.nextInt(1);
                } else if (antonymList.size() != 0 && synonymList.size() == 0) {
                    rand = random.nextInt(1);
                }

                switch (rand) {
                    case 0:
                        listRand = random.nextInt(definitionList.size());
                        mQuestion.setText("Defintion: " + definitionList.get(listRand));
                        hintsList.remove(definitionList.get(listRand));
                        definitionList.remove(listRand);
                        break;
                    case 1:
                        listRand = random.nextInt(synonymList.size());
                        mQuestion.setText("Synonym: " + synonymList.get(listRand));
                        hintsList.remove(synonymList.get(listRand));
                        answersList.remove(synonymList.get(listRand));
                        synonymList.remove(listRand);
                        break;
                    case 2:
                        listRand = random.nextInt(antonymList.size());
                        mQuestion.setText("Antonym: " + antonymList.get(listRand));
                        hintsList.remove(antonymList.get(listRand));
                        antonymList.remove(listRand);
                        break;
                    default:
                        throw new IndexOutOfBoundsException("Case value grater than expected.");
                }

                mSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (answersList.contains(mAnswer.getText().toString().trim())) {
                            Toast.makeText(getActivity(), "Correct", Toast.LENGTH_SHORT).show();
                            sharedPreferences.edit().putInt("count", ++count).apply();
                            hintCount = 0;
                            showDialog();
                        } else {
                            Toast.makeText(getActivity(), "Incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mHint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Random random = new Random();
                        int rand;

                        switch (hintCount) {
                            case 0:
                                rand = random.nextInt(hintsList.size());
                                mHint1.setText(hintsList.get(rand));
                                mHint1.setVisibility(View.VISIBLE);
                                mHint1.setText(hintsList.get(rand));
                                hintsList.remove(rand);
                                hintRemaining1.setImageDrawable(getResources().getDrawable(R.drawable.hint_red));
                                hintCount++;
                                break;
                            case 1:
                                rand = random.nextInt(hintsList.size());
                                mHint2.setText(hintsList.get(rand));
                                mHint2.setVisibility(View.VISIBLE);
                                mHint2.setText(hintsList.get(rand));
                                hintsList.remove(rand);
                                hintRemaining2.setImageDrawable(getResources().getDrawable(R.drawable.hint_red));
                                hintCount++;
                                break;
                            case 2:
                                rand = random.nextInt(hintsList.size());
                                mHint3.setText(hintsList.get(rand));
                                mHint3.setVisibility(View.VISIBLE);
                                mHint3.setText(hintsList.get(rand));
                                hintsList.remove(rand);
                                hintRemaining3.setImageDrawable(getResources().getDrawable(R.drawable.hint_red));
                                hintCount++;
                                break;
                            default:
                                Toast.makeText(getActivity(), "You are out of Hints", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        return rootView;

    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Correct");
        builder.setMessage("Continue to next question?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener = (OnGameClearedListener) getActivity();
                mListener.onGameCleared(true);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void populateList(Cursor cursor, String str, ArrayList arrayList) {

        String temp;
        String splitParam = ",";
        int columnId = 0;

        if (str.equals(SYNONYM)) {
            columnId = COL_SYNONYM;
            splitParam = ",";
        } else if (str.equals(ANTONYM)) {
            columnId = COL_ANTONYM;
            splitParam = ",";
        } else if (str.equals(DEFINITION)) {
            columnId = COL_DEFINITION;
            splitParam = ";;";
        }

        if (!(temp = cursor.getString(columnId)).equals("")) {
            if (temp.contains(splitParam)) {
                int lastIndex = temp.lastIndexOf(splitParam);
                temp = temp.substring(0, lastIndex);
                Collections.addAll(arrayList, temp.split(splitParam));
                hintsList.addAll(arrayList);
            } else
                arrayList = null;

            /*else {
                int lastIndex = temp.lastIndexOf(splitParam);
                temp = temp.substring(lastIndex);
                question.setText(str + ": " + cursor.getString(COL_ANTONYM));
                arrayList = null;
            }*/
        }
    }


}
