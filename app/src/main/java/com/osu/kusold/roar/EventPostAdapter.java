package com.osu.kusold.roar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    public EventPostAdapter(Context context, List<EventPost> posts) {
        super(context, R.layout.event_post_layout, posts);
        this.context = context;
        this.posts = posts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.event_post_layout, parent, false);
        ImageView mPostImageView = (ImageView) rowView.findViewById(R.id.event_post_image);
        TextView mPostNameView = (TextView) rowView.findViewById(R.id.event_post_name);
        TextView mPostDescriptionView = (TextView) rowView.findViewById(R.id.event_post_description);

        EventPost post = posts.get(position);

        mPostNameView.setText(post.eventName);
        mPostDescriptionView.setText(post.description);
        byte[] bitmapData = Base64.decode(post.image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
        mPostImageView.setImageBitmap(bitmap);

        return rowView;
    }
}
