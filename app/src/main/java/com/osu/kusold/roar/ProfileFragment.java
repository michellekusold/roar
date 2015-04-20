package com.osu.kusold.roar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ProfileFragment extends Fragment {
    private Firebase fRef, fRefUser, fRefProfile;
    private ImageView mProfilePic;
    private TextView mAge, mName, mGender;
    private String age, name, gender, profilePic;
    private String mUid;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Firebase root setup
        Firebase.setAndroidContext(getActivity());
        fRef = new Firebase(getString(R.string.firebase_ref));
        mUid = fRef.getAuth().getUid();
        fRefUser = fRef.child("users").child(mUid);
        fRefProfile = fRefUser.child("profile");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button editProfileButton = (Button) view.findViewById(R.id.editProfile);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToEditProfile();
            }
        });

        fRefProfile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(isAdded()) {
                    // do some stuff once
                    Map<String, Object> profileData = (Map<String, Object>) snapshot.getValue();
                    if(profileData.containsKey("name")) {
                        name = profileData.get("name").toString();
                    }
                    if(profileData.containsKey("gender")) {
                        gender = profileData.get("gender").toString();
                    }
                    if(profileData.containsKey("age")) {
                        age = profileData.get("age").toString();
                    }
                    if(profileData.containsKey("photo_thumbnail")) {
                        profilePic = profileData.get("photo_thumbnail").toString();
                    }

                    mProfilePic = (ImageView) view.findViewById(R.id.profileImg);
                    Drawable dPic = decodeBase64(profilePic);

                    mProfilePic.setImageDrawable(dPic);
                    mName = (TextView) view.findViewById(R.id.profileName);
                    mName.setText(name);
                    mGender = (TextView) view.findViewById(R.id.profileGender);
                    mGender.setText(gender);
                    mAge = (TextView) view.findViewById(R.id.profileAge);
                    mAge.setText(age);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        return view;
    }

    /* intent to switch back to editing the user's profile*/
    private void switchToEditProfile() {
        Intent intent = new Intent(getActivity(), CreateProfileActivity.class);
        startActivity(intent);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
