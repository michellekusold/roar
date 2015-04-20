package com.osu.kusold.roar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.firebase.client.Firebase;

/* Start-Up Screen that is seen on launch
 *
 */
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

            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent i;
                SharedPreferences settings = getSharedPreferences(getString(R.string.share_pref_file), MODE_PRIVATE);
                // returns true if "isNewUser" does not exist which means it is a new user
                boolean isNewUser = !settings.contains(getString(R.string.is_new_user));

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
                            i = new Intent(SplashScreenActivity.this, EventFeedActivity.class);
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
