package com.classpulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.classpulse.R;
import com.classpulse.utils.PrefsManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#5A8BD0"));
        setContentView(R.layout.activity_logo_splash);

        PrefsManager prefs = new PrefsManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (prefs.hasSeenOnboarding()) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, OnboardingActivity.class));
            }
            finish();
        }, 2000);
    }
}