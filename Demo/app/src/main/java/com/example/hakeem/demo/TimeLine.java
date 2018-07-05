package com.example.hakeem.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.hakeem.demo.NetworkUtilites.MyVolley;
import com.example.hakeem.demo.helper.Feedback;
import com.example.hakeem.demo.helper.FeedbackAdapter;
import com.example.hakeem.demo.utilities.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TimeLine extends AppCompatActivity {

    private ImageButton backButton;
    private final String TAG = TimeLine.class.getSimpleName();

    private RecyclerView timeline;

    private ArrayList<Feedback> feedbackArrayList;

    private FeedbackAdapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;

    private TextView noConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);
        feedbackArrayList = new ArrayList<>();
        mAdapter = new FeedbackAdapter(this, feedbackArrayList);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        timeline = findViewById(R.id.time_line);
        backButton = findViewById(R.id.back_button);
        noConnection = findViewById(R.id.no_connection_text_view);

        timeline.setLayoutManager(mLayoutManager);
        timeline.setAdapter(mAdapter);


        noConnection.setVisibility(View.GONE);

        // making http call and fetching menu json
        drawTimeLine();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimeLine.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void drawTimeLine() {

        ///////String feedbackUrl = "http://192.168.1.4/guidak_files/feedback_scripts/invoke_feedback.php";
        String feedbackUrl = Variables.INVOKE_FEEDBACK_URL;

        StringRequest request = new StringRequest(feedbackUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    noConnection.setVisibility(View.GONE);

                    feedbackArrayList.clear();

                    Log.e("responxse", response);

                    JSONObject feedbackJsonObject = new JSONObject(response);

                    JSONArray feedbackArray = feedbackJsonObject.getJSONArray("feedback");

                    /***
                     *                                  json object
                     *
                     *                             $temp['id'] = $row['id'];
                     *                             $temp['user_displayed_name'] = $row['displayedName'];
                     *                             $temp['profile_photo'] = $row['profilePhoto'];
                     *                             $temp['time'] = $row['postedAt'];
                     *                             $temp['text'] = $row['feedbackText'];
                     *                             $temp['photo_attached_url'] = $row['photoPath'];
                    * */
                    long id;
                    String name;
                    String text;
                    String date;
                    String photoUrl;
                    String profilePhoto;

                    for (int i = 0; i < feedbackArray.length(); i++) {
                        JSONObject oneFeedback = feedbackArray.getJSONObject(i);

                        id = oneFeedback.getLong("id");
                        name = oneFeedback.getString("user_displayed_name");
                        profilePhoto = oneFeedback.getString("profile_photo");
                        Log.e("profilePhotoUrl", profilePhoto);
                        //2018-04-15 23:43:20
                        date = oneFeedback.getString("time");
                        text = oneFeedback.getString("text");
                        photoUrl = Variables.serverUrl + "guidak_files/feedback_scripts/"+ oneFeedback.getString("photo_attached_url");



                        byte[] encodeByte = Base64.decode(profilePhoto, Base64.DEFAULT);

                        feedbackArrayList.add(new Feedback(id, name, date, text, photoUrl, encodeByte));

                    }
                    mAdapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                noConnection.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        int MY_SOCKET_TIMEOUT_MS = 7000;

        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyVolley.getInstance(this).addToRequestQueue(request);

    }

}
