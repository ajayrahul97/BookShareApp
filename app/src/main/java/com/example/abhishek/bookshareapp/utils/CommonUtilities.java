package com.example.abhishek.bookshareapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class CommonUtilities {

    public static final String goodreads_api_url="https://www.goodreads.com/";
    public static final String API_KEY ="OIPSMQ3VvFcBzdZiP61oA";//#gitignore
    public static final String local_books_api_url = "http://192.168.43.80:8000/";

    public static String getUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("Token", Context.MODE_PRIVATE);
        return preferences.getString("id", null);
    }

    public static String getUserName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("Token", Context.MODE_PRIVATE);
        String name = preferences.getString("first_name", null) + " " + preferences.getString("last_name", null);
        return name;
    }

}
