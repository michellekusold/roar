package com.osu.kusold.roar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by johnsgresham on 4/2/15.
 */
public class EventPostAdapter extends ArrayAdapter<EventPost> {

    Context context;
    List<EventPost> posts;
    ImageView mPostImageView;

    public EventPostAdapter(Context context, List<EventPost> posts) {
        super(context, R.layout.event_post_layout, posts);
        this.context = context;
        this.posts = posts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.event_post_layout, parent, false);
        mPostImageView = (ImageView) rowView.findViewById(R.id.event_post_image);
        TextView mPostNameView = (TextView) rowView.findViewById(R.id.event_post_name);
        TextView mPostDescriptionView = (TextView) rowView.findViewById(R.id.event_post_description);

        EventPost post = posts.get(position);

        // set the brief event description listed in the eventfeed
        mPostNameView.setText(post.eventName);
        mPostDescriptionView.setText(post.description);

        Drawable thumbnail = decodeBase64(post.image);
        mPostImageView.setImageDrawable(thumbnail);

        return rowView;
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
        int imgViewHeight = 70;
        int imgViewWidth = 70;

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imgViewHeight, imgViewWidth);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bImg = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length, options);

        Drawable d = new BitmapDrawable(getContext().getResources(),bImg);
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
}
