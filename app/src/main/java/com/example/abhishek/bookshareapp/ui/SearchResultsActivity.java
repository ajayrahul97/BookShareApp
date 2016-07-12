package com.example.abhishek.bookshareapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ScrollingView;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.abhishek.bookshareapp.R;
import com.example.abhishek.bookshareapp.ui.fragments.BookListFragment;
import com.example.abhishek.bookshareapp.utils.CommonUtilities;

public class SearchResultsActivity extends AppCompatActivity {
    String query;
    String API_KEY = CommonUtilities.API_KEY;
    EditText searchEditText;
    String mode = "all";
    RadioButton r1, r2, r3;
    BookListFragment bookListFragment;
    NestedScrollView scrollingView;
    FloatingActionButton button;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this,MyBooks.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);
        scrollingView =(NestedScrollView)findViewById(R.id.scrollView);
        button = (FloatingActionButton) findViewById(R.id.scroll);

        scrollingView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY>1000){
                    button.setVisibility(View.VISIBLE);
                }else{
                    button.setVisibility(View.INVISIBLE);

                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollingView.fullScroll(View.FOCUS_UP);

            }
        });
        searchEditText = (EditText) findViewById(R.id.searchEditText);
        r1 = (RadioButton) findViewById(R.id.all);
        r2 = (RadioButton) findViewById(R.id.title);
        r3 = (RadioButton) findViewById(R.id.author);
        bookListFragment = new BookListFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.container, bookListFragment)
                .commit();


    }



    public void search(View view) {

        if (r1.isChecked()) {
            mode = "all";
        }
        if (r2.isChecked()) {
            mode = "title";
        }
        else if (r3.isChecked()) {
            mode = "author";
        }

        hideKeyboard();
        query = searchEditText.getText().toString();
        bookListFragment.getBooks(query, mode, API_KEY);
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
