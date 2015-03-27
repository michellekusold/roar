package com.osu.kusold.roar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/* Class to view the user's Profile */
public class ViewProfileActivity extends ActionBarActivity {

    private Firebase fRef, fRefUser, fRefProfile;
    private ImageView mProfilePic;
    private TextView mAge, mName, mGender;
    private String age, name, gender, profilePic;
    private String mUid;

    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        // Firebase root setup
        Firebase.setAndroidContext(this);
        fRef = new Firebase(getString(R.string.firebase_ref));
        mUid = fRef.getAuth().getUid();
        fRefUser = fRef.child("users").child(mUid);
        fRefProfile = fRefUser.child("profile");


        fRefProfile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                Map<String, Object> profileData = (Map<String, Object>) snapshot.getValue();
                name = profileData.get("name").toString();
                gender = profileData.get("gender").toString();
                age = profileData.get("age").toString();
                profilePic = profileData.get("photo").toString();

                Drawable dPic = decodeBase64(profilePic);

                mProfilePic = (ImageView) findViewById(R.id.profileImg);
                mProfilePic.setImageDrawable(dPic);
                mName = (TextView) findViewById(R.id.profileName);
                mName.setText(name);
                mGender = (TextView) findViewById(R.id.profileGender);
                mGender.setText(gender);
                mAge = (TextView) findViewById(R.id.profileAge);
                mAge.setText(age);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        Button editProfileButton = (Button) findViewById(R.id.editProfile);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    switchToEditProfile();
            }
        });
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

    public Drawable decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        Bitmap bImg = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        Drawable d = new BitmapDrawable(getResources(),bImg);
        return d;
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

    /* intent to switch back to editing the user's profile*/
    private void switchToEditProfile() {
        Intent intent = new Intent(this, CreateProfileActivity.class);
        startActivity(intent);
    }
}