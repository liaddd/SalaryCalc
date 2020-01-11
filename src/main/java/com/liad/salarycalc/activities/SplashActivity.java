package com.liad.salarycalc.activities;

import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseUser;
import com.liad.salarycalc.R;
import com.liad.salarycalc.managers.FirebaseManager;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        findViewById(R.id.splash_activity_icon_image_view).animate().setDuration(5000).rotation(360);

        final FirebaseUser currentUser = FirebaseManager.getInstance().getFirebaseUser();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                changeActivity(SplashActivity.this, currentUser != null ? MainActivity.class : LoginActivity.class, true);
            }
        }, 1000);
    }
}
