package com.example.hakeem.demo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.hakeem.demo.NetworkUtilites.MyVolley;
import com.example.hakeem.demo.helper.AppUserInfoDbHelper;
import com.example.hakeem.demo.utilities.ImageProcess;
import com.example.hakeem.demo.utilities.Variables;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PostFeedback extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private String mTempPhotoPath;


    String id = "1";
    String username = "hakeem";

    ImageButton attachPhotoWithFeedback;

    Button submitFeedbackButton;


    ImageView ShowSelectedImage;


    EditText feedbackEditText;

    Bitmap capturedPhotoBitmap = null;

    String feedbackText;

    private String feedbackPhotoFileName;
    private int GALLERY = 1, CAMERA = 2;

    private AppUserInfoDbHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feedback);

        Log.e("post-feed", id);
        attachPhotoWithFeedback = findViewById(R.id.attached_photo_button);

        submitFeedbackButton = findViewById(R.id.submit_feedback_button);

        ShowSelectedImage = findViewById(R.id.attached_photo);

        feedbackEditText = findViewById(R.id.feedback_input_text);

        db = new AppUserInfoDbHelper(getApplicationContext());

        attachPhotoWithFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                howToTakePhoto();


            }
        });

        id = db.getUserID();
        Log.e("post-feed", id);


        submitFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                feedbackText = feedbackEditText.getText().toString().trim();


                if (feedbackText.isEmpty() && capturedPhotoBitmap != null) {

                    //TODO insert feedback as photo only ---> completed

                    feedbackPhotoFileName = ImageProcess.getPhotoUploadedName(id);

                    ImageProcess.uploadImage(PostFeedback.this, capturedPhotoBitmap, feedbackPhotoFileName, id);

                } else if (!feedbackText.isEmpty() && capturedPhotoBitmap == null) {


                    //TODO insert feedback as text only ---> completed
                    submitFeedbackAsTextOnly(feedbackText, id);

                } else if ((!feedbackText.isEmpty()) && capturedPhotoBitmap != null) {
                    //TODO insert feedback as text and photo ---> completed
                    feedbackPhotoFileName = ImageProcess.getPhotoUploadedName(id);

                    ImageProcess.uploadCompleteFeedback(PostFeedback.this, feedbackText, capturedPhotoBitmap, feedbackPhotoFileName, id);
                } else {
                    Toast.makeText(getApplicationContext(), "please set required parameters", Toast.LENGTH_LONG).show();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(PostFeedback.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        5);
            }
        }
    }

    public void submitFeedbackAsTextOnly(final String text, final String userId) {

       //////// String feedbackUrl = "http://192.168.1.4/guidak_files/feedback_scripts/upload_feedback_text_only.php";

        String feedbackUrl = Variables.UPLOAD_FEEDBACK_TEXT_ONLY;

        StringRequest request = new StringRequest(Request.Method.POST, feedbackUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Toast.makeText(getApplicationContext(), "***" + response, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() {


                // Creating Map String Params.
                Map<String, String> params = new HashMap<String, String>();

                // Adding All values to Params.
                params.put("user_id", userId);
                params.put("feedback_text", text);

                return params;
            }
        };

        MyVolley.getInstance(this).addToRequestQueue(request);
    }

    private void howToTakePhoto() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Photo Gallery",
                "Camera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                takePhotoByCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoByCamera() {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            launchCamera();
        }

    }

    /**
     * Creates a temporary image file and captures a picture to store in it.
     */
    private void launchCamera() {

        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = ImageProcess.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = Uri.fromFile(photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, CAMERA);


            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    mTempPhotoPath = ImageProcess.getRealPathFromURI(this, contentURI);
                    capturedPhotoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    ShowSelectedImage.setImageBitmap(ImageProcess.resamplePic(getApplicationContext(), mTempPhotoPath));

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(PostFeedback.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            if (resultCode == RESULT_OK) {

                capturedPhotoBitmap = ImageProcess.resamplePic(getApplicationContext(), mTempPhotoPath);
                //ShowSelectedImage.setImageBitmap(capturedPhotoBitmap);

                capturedPhotoBitmap = ImageProcess.rotateBy90Degree(capturedPhotoBitmap);

                ShowSelectedImage.setImageBitmap(capturedPhotoBitmap);

            } else {
                // Otherwise, delete the temporary image file
                ImageProcess.deleteImageFile(this, mTempPhotoPath);
            }


        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

}
