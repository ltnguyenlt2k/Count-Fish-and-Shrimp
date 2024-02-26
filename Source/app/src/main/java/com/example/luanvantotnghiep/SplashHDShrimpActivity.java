package com.example.luanvantotnghiep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashHDShrimpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_hdshrimp);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextActivity();
            }
        }, 3000);
    }

    private void nextActivity() {
        Intent intent = new Intent(this, CountShrimpActivity.class);
        startActivity(intent);
        finish();
    }
}