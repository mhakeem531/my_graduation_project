package com.example.hakeem.demo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.hakeem.demo.NetworkUtilites.ConnectToInvokeObjectInfo;
import com.example.hakeem.demo.utilities.Variables;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanningActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private String statueName;
    private String fileLanguage;

    //to finish this activity from AsyncTask class
    public static Activity fa;
    public static Context context;


    public static final String LOG_TAG = ScanningActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        Log.e(LOG_TAG, "onCreate");
        fa = this;


        String key = getResources().getString(R.string.pref_langs_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Variables.audioFileLanguage = prefs.getString(key, null);

        this.fileLanguage = prefs.getString(key, null);

        Log.e(LOG_TAG, Variables.audioFileLanguage);

        QrScanner();

    }

    public void QrScanner() {

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view

        setContentView(mScannerView);

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.

        mScannerView.startCamera();         // Start camera

    }

    @Override
    public void handleResult(Result rawResult) {

        Log.e(LOG_TAG, "handleResult");

        Variables.statueName = rawResult.getText();

        this.statueName = rawResult.getText();

        Log.e(LOG_TAG, rawResult.getText()); // Prints scan results --->> i.e Djoser_king
        Log.e(LOG_TAG, rawResult.getBarcodeFormat().toString());// --> QR_CODE

        FetchStatueAudioFilePath();
    }

    @Override
    public void onPause() {
        super.onPause();

        mScannerView.stopCamera();   // Stop camera on pause
    }

    public void FetchStatueAudioFilePath(){

        if(this.fileLanguage != null){

            ConnectToInvokeObjectInfo invokeAudioFilePath = new ConnectToInvokeObjectInfo(this);
            invokeAudioFilePath.execute(Variables.selectAudioFilePathOperation, this.statueName, this.fileLanguage);

        }else{
            Toast.makeText(this, R.string.select_lang_first, Toast.LENGTH_LONG).show();
        }

    }
}