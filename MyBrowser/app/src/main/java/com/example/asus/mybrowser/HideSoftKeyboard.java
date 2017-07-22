package com.example.asus.mybrowser;

import android.app.Activity;

import android.view.inputmethod.InputMethodManager;


/**
 * Created by Asus on 2016/11/7.
 */
public class HideSoftKeyboard {
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)

                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}



