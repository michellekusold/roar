package com.osu.kusold.roar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.Map;


public class CreateProfileActivity extends ActionBarActivity {

    private Firebase fRef, fRefUser, fRefProfile;
    private ImageButton mProfilePic;
    private NumberPicker mAgePicker;
    private EditText mNameView;
    private String mUid;
    private Spinner mGenderOptions;
    private String age, name, gender, profilePic;
    ArrayAdapter<CharSequence> adapter;

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
        adapter = ArrayAdapter.createFromResource(this, R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenderOptions.setAdapter(adapter);

        // populate information if it already exists (View Profile Functionality)
        fRefProfile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // check to make sure that prev profile data only gets populated if it exists
                SharedPreferences settings = getSharedPreferences(getString(R.string.share_pref_file), MODE_PRIVATE);
                boolean isProfileComplete = settings.getBoolean(getString(R.string.is_profile_info_complete), false);
                if(isProfileComplete) {
                    // do some stuff once
                    Map<String, Object> profileData = (Map<String, Object>) snapshot.getValue();
                    // profileData will not be null if the user is editing the profile
                    if (profileData != null) {
                        name = profileData.get("name").toString();
                        gender = profileData.get("gender").toString();
                        age = profileData.get("age").toString();
                        profilePic = profileData.get("photo").toString();

                        // set image
                        Drawable dPic = decodeBase64(profilePic);
                        mProfilePic.setImageDrawable(dPic);
                        // set name
                        mNameView.setText(name);
                        // set age
                        mAgePicker.setValue(Integer.parseInt(age));
                        // set gender
                        int spinnerPosition = adapter.getPosition(gender);
                        mGenderOptions.setSelection(spinnerPosition);
                    }
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });


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
                Drawable sizedImg = scaleImage(selectedImage);
                imgBtn.setImageDrawable(sizedImg);

            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    /* Take in a Uri resource, scale it, then return a drawable */
    public Drawable scaleImage(Uri image) throws FileNotFoundException{
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(this.getContentResolver().openInputStream(image), null, options);

        // find dimensions we want to scale to
        int imgViewHeight = mProfilePic.getHeight();
        int imgViewWidth = mProfilePic.getWidth();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imgViewHeight, imgViewWidth);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bImg = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(image), null, options);

        Drawable d = new BitmapDrawable(getResources(),bImg);
        return d;
    }

    /* Caculates a sample size value that is a power of two based on a target width and height */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public Drawable decodeBase64(String input)
    {
        // preventing memory issues:
        // Decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        byte[] decodedByte = Base64.decode(input, 0);
        BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length, options);

        // find dimensions we want to scale to
        int imgViewHeight = mProfilePic.getHeight();
        int imgViewWidth = mProfilePic.getWidth();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imgViewHeight, imgViewWidth);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bImg = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length, options);

        Drawable d = new BitmapDrawable(getResources(),bImg);
        return d;
    }


    /* Converts the full sized image to a format storable in FireBase */
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
        // Store full resolution and thumbnail size for profile picture
        Bitmap profBmp = drawableToBitmap(mProfilePic.getDrawable());
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        profBmp.compress(Bitmap.CompressFormat.PNG, 100, byteBuffer);
        byte[] byteArray = byteBuffer.toByteArray();
        String fullImageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d("CreateProfileActivity", "Profile picture full resolution is size (bytes): " + byteArray.length);

        byteBuffer.reset();
        profBmp.compress(Bitmap.CompressFormat.JPEG, 10, byteBuffer);
        byteArray = byteBuffer.toByteArray();
        String thumbnailImageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d("CreateProfileActivity", "Profile picture thumbnail is size (bytes): " + byteArray.length);

        // store the user's profile information
        //fRefProfile.child("photo").setValue(fullImageFile);
        fRefProfile.child("photo_thumbnail").setValue(thumbnailImageFile);
        fRefProfile.child("name").setValue(mNameView.getText().toString());
        fRefProfile.child("age").setValue(mAgePicker.getValue());
        fRefProfile.child("gender").setValue(mGenderOptions.getSelectedItem().toString());

        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(getString(R.string.share_pref_file), MODE_PRIVATE).edit();
        editor.putBoolean(getString(R.string.is_profile_info_complete), true);
        editor.apply();
    }

    /* The next intent logically to occur after profile creation has completed */
    private void switchToEventFeed() {
        Intent intent = new Intent(this, EventFeedActivity.class);
        startActivity(intent);
    }
}
