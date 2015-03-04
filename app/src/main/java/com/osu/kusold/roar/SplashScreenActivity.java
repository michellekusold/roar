package com.osu.kusold.roar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;


public class SplashScreenActivity extends Activity {

    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over

                Intent i;
                SharedPreferences settings = getPreferences(MODE_PRIVATE);
                boolean isNewUser = settings.getBoolean("isNewUser", true);

                if(isNewUser) {
                    i = new Intent(SplashScreenActivity.this, SignUpActivity.class);
                }
                else {
                    // if not logged in, sign in screen
                    i = new Intent(SplashScreenActivity.this, LoginActivity.class);
                }

                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);

    }

}
