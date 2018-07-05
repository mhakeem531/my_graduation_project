package com.example.hakeem.demo.NetworkUtilites;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.hakeem.demo.AudioPlayerActivity;
import com.example.hakeem.demo.GoogleVisionScanningActivity;
import com.example.hakeem.demo.R;
import com.example.hakeem.demo.utilities.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by hakeem on 1/25/18.
 */

public class ConnectToInvokeObjectInfo extends AsyncTask<String, Void, String> {

    private static final String LOG_TAG = ConnectToInvokeObjectInfo.class.getName();

    @SuppressLint("StaticFieldLeak")
    private Context context;


    public ConnectToInvokeObjectInfo(Context context1) {
        context = context1;
    }

    @Override
    protected String doInBackground(String... params) {
        String operationType = params[0];

        String connectionResult = "";

        if (operationType.equals(Variables.selectAudioFilePathOperation)) {

            String statueName = params[1];
            String language = params[2];
            String statueTable = params[3];
            String imageTable = params[4];
            connectionResult = audioFilePathConnection(statueName, language, statueTable, imageTable);
            Log.e(LOG_TAG, "doInBackground connectionResult " + connectionResult);

        }

        return connectionResult;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        Log.e(LOG_TAG, "onPostExecute " + s);

        GoogleVisionScanningActivity.fa.finish();

        if(s == null){

            Toast.makeText(context, R.string.error_while_query, Toast.LENGTH_LONG).show();
        }

        else if(s.equals("no")){


            Toast.makeText(context, R.string.unknown_qr, Toast.LENGTH_LONG).show();


        }
        else{

            //TODO come here and parse JSON object to obtain(image path, description, audio path) --> completed
            try {

//                String encodedString = URLEncoder.encode(s, "ISO-8859-1");
//                s = URLDecoder.decode(encodedString, "UTF-8");
                JSONObject statueInfo = new JSONObject(s);

                JSONArray statueArray = statueInfo.getJSONArray("statue");
                JSONObject currentStatue = statueArray.getJSONObject(0);

                Variables.completeAudioFilePath = Variables.serverUrl + currentStatue.getString("audioFilePathe");
                Variables.statueDescription = currentStatue.getString("description");
                Variables.completeImageFilePath = Variables.serverUrl + currentStatue.getString("imagePath");

                Log.e("completeAudioFilePath", Variables.completeAudioFilePath);
                Log.e("statueDescription", Variables.statueDescription);
                Log.e("completeImageFilePath", Variables.completeImageFilePath);



            } catch (JSONException e) {
                e.printStackTrace();
            }


//            Variables.audioFilePath = s;
//            Variables.completeAudioFilePath = Variables.serverUrl + Variables.audioFilePath;
            Intent intent = new Intent(context, AudioPlayerActivity.class);
            context.startActivity(intent);
        }
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }


    private String audioFilePathConnection(String statueName, String language, String statueTable, String imagesTable) {

        String connectionResult = null;

        try {
            String statueNameKey = "statueName=";
            String languageKey = "&language=";
            String statueTableKey = "&statues_table=";
            String imagesTableKey = "&images_table=";

            //String selectUrl = Variables.serverUrl + "guidak_files/select_audio_file_path_with_POST.php";
            String selectUrl = Variables.URL_INVOKE_STATUE_INFO;

            Log.e(LOG_TAG, "ppp " +statueName + "---" + language);

            String connectionParameters = statueNameKey
                    + URLEncoder.encode(statueName, "UTF-8")
                    + languageKey + URLEncoder.encode(language, "UTF-8")
                    + statueTableKey + URLEncoder.encode(statueTable, "UTF-8")
                    + imagesTableKey + URLEncoder.encode(imagesTable, "UTF-8");

            Log.e(LOG_TAG, "ppp " +connectionParameters);

            byte[] parametersByt = connectionParameters.getBytes("UTF-8");

            URL SelectAudioFilePathUrl = new URL(selectUrl);


            HttpURLConnection filePathConnection = (HttpURLConnection) SelectAudioFilePathUrl.openConnection();

            filePathConnection.setRequestMethod("POST");
            filePathConnection.setDoInput(true);
            filePathConnection.setDoOutput(true);
            Log.e(LOG_TAG, "1");

            filePathConnection.getOutputStream().write(parametersByt);
            filePathConnection.getOutputStream().flush();
            filePathConnection.getOutputStream().close();

            Log.e(LOG_TAG, "2");

            InputStreamReader resultStreamReader = new InputStreamReader(filePathConnection.getInputStream());
            Log.e(LOG_TAG, "3");

            BufferedReader resultReader = new BufferedReader(resultStreamReader);
            Log.e(LOG_TAG, "4");

            String line;
            final StringBuilder textBulider = new StringBuilder();
            while ((line = resultReader.readLine()) != null){
                textBulider.append(line);

            }
          //  connectionResult = resultReader.readLine();

            connectionResult = textBulider.toString();



            Log.e(LOG_TAG, "res -" + connectionParameters);
            resultReader.close();
            resultStreamReader.close();
            filePathConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.e(LOG_TAG, "file path  fom method is " + connectionResult);
        return connectionResult;

    }

}


