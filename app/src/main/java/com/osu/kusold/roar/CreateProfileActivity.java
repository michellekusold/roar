package com.osu.kusold.roar;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.firebase.client.Firebase;


public class CreateProfileActivity extends ActionBarActivity {

    private Firebase fRef;
    private String authDataUid;
    private NumberPicker mAgePicker;
    private EditText mNameView;
    private String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        // Firebase root setup
        Firebase.setAndroidContext(this);
        fRef = new Firebase(getString(R.string.firebase_ref));
        mUid = fRef.getAuth().getUid();
        mNameView = (EditText) findViewById(R.id.create_profile_name);
        mAgePicker = (NumberPicker) findViewById(R.id.age_picker);
        mAgePicker.setMinValue(18);
        mAgePicker.setMaxValue(120);

        Button submitProfileButton = (Button) findViewById(R.id.create_profile_submit);
        submitProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Age is a number editable view
                if(!isErrorInProfileInfo()) {
                    submitProfile();
                }

            }
        });
    }

    private boolean isErrorInProfileInfo() {
        boolean result = false;
        if(mNameView.getText().toString().isEmpty()) {
            mNameView.setError(getString(R.string.error_field_required));
            result = true;
        }
        return result;
    }

    // Testing creating user information on Firebase, starting simply with age.
    private void submitProfile() {
        fRef.child("users").child(mUid).child("name").setValue(mNameView.getText().toString());
        fRef.child("users").child(mUid).child("age").setValue(mAgePicker.getValue());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
