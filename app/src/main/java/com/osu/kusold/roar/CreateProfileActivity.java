package com.osu.kusold.roar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.io.ByteArrayOutputStream;


public class CreateProfileActivity extends ActionBarActivity {

    private Firebase fRef, fRefUser, fRefProfile;
    private String authDataUid;
    private ImageButton mProfilePic;
    private NumberPicker mAgePicker;
    private EditText mNameView;
    private String mUid;
    private Spinner mGenderOptions;

    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        // Firebase root setup
        Firebase.setAndroidContext(this);
        fRef = new Firebase(getString(R.string.firebase_ref));
        mUid = fRef.getAuth().getUid();
        fRefUser = fRef.child("users").child(mUid);
        fRefProfile = fRefUser.child("profile");

        // set up input areas
        // picture option
        mProfilePic = (ImageButton) findViewById(R.id.addProfileImg);
        mProfilePic.setImageResource(R.drawable.add_prof_img);
        mProfilePic.setOnClickListener(profPicSelector);

        // name and age
        mNameView = (EditText) findViewById(R.id.create_profile_name);
        mAgePicker = (NumberPicker) findViewById(R.id.age_picker);
        mAgePicker.setMinValue(18);
        mAgePicker.setMaxValue(120);

        // gender options
        mGenderOptions = (Spinner) findViewById(R.id.genderOptions);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenderOptions.setAdapter(adapter);

        Button submitProfileButton = (Button) findViewById(R.id.create_profile_submit);
        submitProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Age is a number editable view
                if(!isErrorInProfileInfo()) {
                    submitProfile();
                    switchToEventFeed();
                }

            }
        });
    }

    /* Method to handle getting the profile picture by launching the Gallery */
    private View.OnClickListener profPicSelector =new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            // Create intent to Open Image applications like Gallery, Google Photos
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Start the Intent
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);

        }
    };

    /* Handles the image after the user has picked it */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                ImageButton imgBtn = (ImageButton) findViewById(R.id.addProfileImg);
                imgBtn.setImageURI(selectedImage);
                // Set the Image in ImageView after decoding the String
                //imgBtn.setImageBitmap(BitmapFactory
                //        .decodeFile(imgDecodableString));

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    /* Converts the image to a format storable in FireBase */
    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private boolean isErrorInProfileInfo() {
        boolean result = false;
        // if no name
        if(mNameView.getText().toString().isEmpty()) {
            mNameView.setError(getString(R.string.error_field_required));
            result = true;
            Toast.makeText(this, "You forgot to enter your name.",
                    Toast.LENGTH_LONG).show();
        }
        return result;
    }

    /* Checks to see if valid data was entered and if so, stores it to Firebase*/
    private void submitProfile() {
        Bitmap profBmp = drawableToBitmap(mProfilePic.getDrawable());
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        profBmp.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
        //profBmp.recycle();
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // store the user's profile information
        fRefProfile.child("photo").setValue(imageFile);
        fRefProfile.child("name").setValue(mNameView.getText().toString());
        fRefProfile.child("age").setValue(mAgePicker.getValue());
        fRefProfile.child("gender").setValue(mGenderOptions.getSelectedItem().toString());

        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.share_pref_file), MODE_PRIVATE).edit();
        editor.putBoolean(fRef.getAuth().getUid() + R.string.is_profile_info_complete, true);
        editor.apply();
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

    /* The next intent logically to occur after profile creation has completed */
    private void switchToEventFeed() {
        Intent intent = new Intent(this, EventFeedActivity.class);
        startActivity(intent);
    }
}
