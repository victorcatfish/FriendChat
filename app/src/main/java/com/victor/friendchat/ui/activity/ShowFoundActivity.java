package com.victor.friendchat.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.victor.friendchat.R;

public class ShowFoundActivity extends AppCompatActivity {

    public static ShowFoundActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_luntan);
        instance = this;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        instance = null;
    }
}
