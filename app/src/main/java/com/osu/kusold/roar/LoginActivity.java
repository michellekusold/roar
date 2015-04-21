package com.osu.kusold.roar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    // key const for sending email to sign up activity
    public final static String EMAIL_ADDRESS_MESSAGE = "com.osu.roar.EMAIL_ADDRESS_MESSAGE";
    public final static String LOGIN_ERROR_MESSAGE = "com.osu.roar.LOGIN_ERROR_MESSAGE";
    public final static String AUTHDATA_MESSAGE = "com.osu.roar.AUTHDATA_MESSAGE";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private Firebase fRef;
    private Boolean wasInvalidEmailOrPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.v("Create", "The LoginActivity onCreate was called.");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Firebase.setAndroidContext(this);
        fRef = new Firebase(getString(R.string.firebase_ref));
        fRef.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    // user is logged in
                } else {
                    // user is not logged in
                }
            }
        });

        // Check intent for email preset/errors from previous attempts
        Bundle extras = getIntent().getExtras();
        String emailPreset = "";
        if(extras != null) {
            emailPreset = extras.getString(LoginActivity.EMAIL_ADDRESS_MESSAGE, "");
            wasInvalidEmailOrPassword = extras.getBoolean(LoginActivity.LOGIN_ERROR_MESSAGE, false);
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();
        mEmailView.setText(emailPreset);

        mPasswordView = (EditText) findViewById(R.id.password_confirm);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        if(wasInvalidEmailOrPassword) {
            mPasswordView.setError(getString(R.string.error_invalid_email_or_password));
            mPasswordView.requestFocus();
        }

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInternetAvailable()) {
                    Log.v("OnClick", "checking for connection");
                    attemptLogin();
                }
                else {
                    Toast.makeText(getApplicationContext(), "No internet connection present :(",
                    Toast.LENGTH_LONG).show();
                }
            }
        });

        Button mEmailSignUpButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToSignUp();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void switchToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        String emailAddress = mEmailView.getText().toString();
        intent.putExtra(SignUpActivity.EMAIL_ADDRESS_MESSAGE, emailAddress);
        startActivity(intent);
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            // User logged in already, start activity for create profile?
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password, this);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 7;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onResume();
        Log.v("Start", "The LoginActivity onStart was called.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("Pause", "The LoginActivity onPause was called.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("Resume", "The LoginActivity onResume was called.");
    }

    @Override
    protected void onStop() {
        super.onResume();
        Log.v("Stop", "The LoginActivity onStop was called.");
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private Context mContext;

        UserLoginTask(String email, String password, Context context) {
            mEmail = email;
            mPassword = password;
            mContext = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            fRef.authWithPassword(mEmail, mPassword, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    Log.v("Login", "User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                }
                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Log.v("Login", "Error on login.");
                    Log.v("Login", "Email: " + mEmail + ", Password: " + mPassword);
                }
            });
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            showProgress(false);
            Intent intent;
            if(fRef.getAuth() != null) {
                SharedPreferences settings = getSharedPreferences(getString(R.string.share_pref_file),MODE_PRIVATE);
                // if the user logs in from loginActivity, they are not a new user
                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.share_pref_file),MODE_PRIVATE).edit();
                editor.putBoolean(getString(R.string.is_new_user), false);
                editor.commit();
                // check to see if they have filled out a profile yet
                if(!settings.getBoolean(fRef.getAuth().getUid() + getString(R.string.is_profile_info_complete), true)) {
                    intent = new Intent(mContext, CreateProfileActivity.class);
                    settings.edit().putBoolean(fRef.getAuth().getUid() + getString(R.string.is_profile_info_complete), true).apply();
                } else {
                    intent = new Intent(mContext, EventFeedActivity.class);
                }

            } else {
                intent = new Intent(mContext, LoginActivity.class);
                intent.putExtra(LoginActivity.EMAIL_ADDRESS_MESSAGE, mEmail);
                intent.putExtra(LoginActivity.LOGIN_ERROR_MESSAGE, true);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}



