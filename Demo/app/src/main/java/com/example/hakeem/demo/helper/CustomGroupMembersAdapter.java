package com.example.hakeem.demo.helper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.hakeem.demo.NetworkUtilites.MyVolley;
import com.example.hakeem.demo.R;
import com.example.hakeem.demo.utilities.Variables;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hakeem on 4/12/18.
 */

public class CustomGroupMembersAdapter extends RecyclerView.Adapter<CustomGroupMembersAdapter.GroupMemberViewHolder> {
    // Class variables for the Cursor that holds task data and the Context
    private Cursor mCursor;
    private Context mContext;
    private int count;



    /**
     * Constructor using the context and the db cursor
     * @param context the calling context/activity
     * @param cursor the db cursor with waitlist data to display
     */
    public CustomGroupMembersAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
//        this.count = mCursor.getCount();
    }


    @Override
    public GroupMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.group_member_list_item, parent, false);
        return new GroupMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomGroupMembersAdapter.GroupMemberViewHolder holder, int position) {

        Log.e("from xxxxxx adapter, ", "hhhhh");

        // Move the mCursor to the position of the item to be displayed
        if (!mCursor.moveToPosition(position)) {
            Log.e("from xxxxxx adapter, ", "hhhhh");
            return;
        }

        // Update the view holder with the information needed to display
        String username = mCursor.getString(mCursor.getColumnIndex(GroupMembersContract.GroupMemberEntry.COLUMN_USERNAME));

        long id = mCursor.getLong(mCursor.getColumnIndex(GroupMembersContract.GroupMemberEntry._ID));

        // Display the guest name
        holder.username.setText(username);
        Log.e("from xxxxxx adapter, ", username);
        holder.userAvatar.setImageResource(R.drawable.profile_photo);
        getMemberProfile(username, holder.userAvatar);

        holder.itemView.setTag(id);


    }




    @Override
    public int getItemCount() {
        return mCursor.getCount();
        //return 2;

    }


    /**
     * Swaps the Cursor currently held in the adapter with a new one
     * and triggers a UI refresh
     *
     * @param newCursor the new cursor that will replace the existing one
     */
    public void swapCursor(Cursor newCursor) {
        // Always close the previous mCursor first
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }

    }

    /**
     * invoke profile image  of member added by admin to be displayed in list item
     * @param username : username of added member
     * @param profile : image view in list item of list view
     */
    private void getMemberProfile(final String username, final ImageView profile) {

        //  final String uploadImage = getStringImage(image);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Variables.URL_INVOKE_ADDED_MEMBER_PROFILE_IMAGE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.e("user-mebxxer-", response);
                if(!response.equals("\nfff")){
                    byte[] encodeByte = Base64.decode(response, Base64.DEFAULT);
                    Log.e("user-meber-phtot", encodeByte.toString());
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(encodeByte);
                    Bitmap theImage = BitmapFactory.decodeStream(imageStream);
                    Log.e("INFO0", imageStream.toString());
                    profile.setImageBitmap(theImage);
                }else {
                    Log.e("user-mebxer-", response);
                    profile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.profile_photo));
                }



            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);

                return params;
            }

        };

        MyVolley.getInstance(mContext).addToRequestQueue(strReq);
    }



    // Inner class for creating ViewHolders
    class GroupMemberViewHolder extends RecyclerView.ViewHolder {

        // Class variables for the task description and priority TextViews
        TextView username;
        ImageView userAvatar;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public GroupMemberViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.group_member_username);
            userAvatar = itemView.findViewById(R.id.group_member_avatar);
        }
    }
}
