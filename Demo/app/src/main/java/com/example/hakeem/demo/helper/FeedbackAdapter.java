package com.example.hakeem.demo.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hakeem.demo.R;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;


public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {
    private Context context;
    private ArrayList<Feedback> feedbackList;

    public FeedbackAdapter(Context context, ArrayList<Feedback> feedbackList) {

        this.context = context;
        this.feedbackList = feedbackList;

    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        View itemView = LayoutInflater.from(context).inflate(R.layout.time_line_list_item, parent, false);
//
//        return new FeedbackViewHolder(itemView);

        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.time_line_list_item, parent, false);
        return new FeedbackViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {

        Log.e("from xxxxxx adapter, ", "hhhhh");

        final Feedback item = feedbackList.get(position);
        holder.displayedName.setText(item.getName());

        holder.dateTime.setText(item.getDate());

        if(!(item.getFeedbackText().equals("null"))){
            holder.feedBackAttachedText.setText(item.getFeedbackText());
        }else{
            holder.feedBackAttachedText.setVisibility(View.GONE);
        }

        Log.e("getAttachedPhotoUrl()", item.getAttachedPhotoUrl());

        if(!(item.getAttachedPhotoUrl() == null)){
            Glide.with(context)
                    .load(item.getAttachedPhotoUrl())
                    .into(holder.feedbackAttachedPhoto);
        }else {
            Log.e("hleooe", "sdkfsf");
            holder.feedbackAttachedPhoto.setVisibility(View.GONE);
        }

        if((item.getProfileImage()) != null){
            ByteArrayInputStream imageStream = new ByteArrayInputStream(item.getProfileImage());
            Bitmap theImage = BitmapFactory.decodeStream(imageStream);
            holder.profilePhoto.setImageBitmap(theImage);
        }


        holder.itemView.setTag(item.getFeedbackId());

    }

    public void setProfilePhoto(byte[] profileImage, ImageView profileImageView){

        ByteArrayInputStream imageStream = new ByteArrayInputStream(profileImage);
        Bitmap theImage = BitmapFactory.decodeStream(imageStream);
        profileImageView.setImageBitmap(theImage);
    }


    @Override
    public int getItemCount() {
        return this.feedbackList.size();
    }

    // Inner class for creating ViewHolders
    class FeedbackViewHolder extends RecyclerView.ViewHolder {

        // Class variables for the task description and priority TextViews
        ImageView profilePhoto;
        TextView displayedName;
        TextView dateTime;
        ImageView feedbackAttachedPhoto;
        TextView feedBackAttachedText;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public FeedbackViewHolder(View itemView) {
            super(itemView);

            profilePhoto = itemView.findViewById(R.id.profile_photo);
            displayedName = itemView.findViewById(R.id.displayed_name);
            dateTime = itemView.findViewById(R.id.date_time);
            feedbackAttachedPhoto = itemView.findViewById(R.id.feedback_photo);
            feedBackAttachedText = itemView.findViewById(R.id.feedback_text);
        }
    }
}
