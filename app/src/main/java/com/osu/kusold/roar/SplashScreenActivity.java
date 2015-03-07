package com.osu.kusold.roar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.firebase.client.Firebase;


public class SplashScreenActivity extends Activity {

    private static int SPLASH_TIME_OUT = 2000;
    private Firebase fRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Firebase setup (only need to set context once)
        Firebase.setAndroidContext(this);
        fRef = new Firebase(getString(R.string.firebase_ref));

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over

                Intent i;
                SharedPreferences settings = getSharedPreferences(getString(R.string.share_pref_file),MODE_PRIVATE);
                boolean isNewUser = settings.getBoolean(getString(R.string.is_new_user), true);

                /*
                 * Decision logic for beginning activity follows:
                 *      no account created -> signup
                 *      account created, not logged in -> login
                 *      account created, logged in, profile not complete -> createprofile
                 *      account created, logged in, profile complete -> eventfeed
                 */
                if(isNewUser) {
                    // User does not have an account
                    i = new Intent(SplashScreenActivity.this, SignUpActivity.class);
                }
                else {
                    // If logged in
                    if(fRef.getAuth() != null) {
                        if(settings.getBoolean(getString(R.string.is_profile_info_complete), false)) {
                            // user has account, is logged in, and profile is complete
                            i = new Intent(SplashScreenActivity.this, EventFeedActivity.class);
                        } else {
                            // User has account, is logged in, but profile is not complete
                            i = new Intent(SplashScreenActivity.this, CreateProfileActivity.class);
                        }

                    } else {
                        // User has account, but not logged in
                        i = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    }
                }

                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);

    }

}
