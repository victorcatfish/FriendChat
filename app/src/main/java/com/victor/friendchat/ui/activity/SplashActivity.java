package com.victor.friendchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.victor.friendchat.R;
import com.victor.friendchat.global.MyApplication;
import com.victor.friendchat.uitl.UIUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_splash);

        new Thread() {
            @Override
            public void run() {
                super.run();
                SystemClock.sleep(3000);
                UIUtils.runningOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

            }
        }.start();

    }
}
