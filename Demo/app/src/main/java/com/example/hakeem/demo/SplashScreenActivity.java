package com.example.hakeem.demo;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.hakeem.demo.helper.SessionManager;

public class SplashScreenActivity extends AppCompatActivity {
private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        sessionManager = new SessionManager(this);
       // sessionManager.setProbmetUsernameStep(false);
        int SPLASH_TIME_OUT = 1000;
        new Handler().postDelayed(new Runnable() {

            /**
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */
            @Override
            public void run() {
                sessionManager.setProbmetUsernameStep(false);
                if(sessionManager.getProbmetUsernameStep()){
                    Intent i = new Intent(SplashScreenActivity.this, PromptUniqueUsernameActivity.class);
                    startActivity(i);
                }

                else {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(i);

                }
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
