package com.example.hakeem.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.hakeem.demo.NetworkUtilites.ConnectToInvokeObjectInfo;
import com.example.hakeem.demo.utilities.Variables;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;

import info.androidhive.barcode.BarcodeReader;



public class GoogleVisionScanningActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {

    public static final String TAG = GoogleVisionScanningActivity.class.getSimpleName();
//    SurfaceView cameraView;
//    BarcodeDetector barcode;
//    CameraSource cameraSource;
//    SurfaceHolder holder;

    //  private String statueName;
    private String fileLanguage;

    @SuppressLint("StaticFieldLeak")
    public static Activity fa;
    BarcodeReader barcodeReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_vision_scanner2);

        fa = this;

        String key = getResources().getString(R.string.pref_langs_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Variables.audioFileLanguage = prefs.getString(key, null);

      //  this.fileLanguage = prefs.getString(key, null);

        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_scanner);


    }

    @Override
    public void onScanned(Barcode barcode) {
        // playing barcode reader beep sound
        barcodeReader.playBeep();

        Log.e("result is ", barcode.displayValue);
        Variables.statueName = barcode.displayValue;

//        Toast.makeText(getApplicationContext(), "result is " + barcode.displayValue, Toast.LENGTH_LONG).show();
        /**
         * Qr code example:
         *                 "period_num&statue_code"                          i.e 0&king_senusert_I_statue_ozirain_situation
         *                 ---------------------
         *  period_num : useful for detect tables names depending on periods of ancient egypt history
         *
         *           0---->  Predynastic period                --->    عصر ماقبل الاسرات

         *           1---->  Early Dynastic Period             --->   العصر العتيق

         *           2---->  Old Kingdom                       --->    الدوله القديمه

         *           3---->  First Intermediate Period         --->    عصرالاضمحلال الأول

         *           4---->  Middle Kingdom                    --->    الدولة الوسطى

         *           5---->  Second Intermediate Period        --->      عصرالاضمحلال الثاني

         *           6---->  New Kingdom                       --->     الدولة الحديثة

         *           7---->  Third Intermediate Period         --->     عصر الإنتقال الثالث

         *           8---->  Late Period                       --->       العصر المتأخر

         *           9-----> Hellenistic period                --->         الفترة البطلمية
         *
         *
         *  statue_code : used for querying whole statue info
         *
         * */
        //this.statueName = barcode.displayValue;

        switch (Variables.statueName.charAt(0)) {
            case '0':
                Variables.audioFilePathTableName = "xyz_predynastic_period_audioM";
                Variables.imageFilePathTable = "xyz_predynastic_period_imageM";
                break;

            case '1':
                Variables.audioFilePathTableName = "xyz_early_dynastic_period_audioM";
                Variables.imageFilePathTable = "xyz_early_dynastic_period_imageM";
                break;

            case '2':
                Variables.audioFilePathTableName = "xyz_old_kingdom_audioM";
                Variables.imageFilePathTable = "xyz_old_kingdom_imageM";
                break;

            case '3':
                Variables.audioFilePathTableName = "xyz_First_intermediate_period_audioM";
                Variables.imageFilePathTable = "xyz_first_intermediate_period_imageM";
                break;
            case '4':
                Variables.audioFilePathTableName = "xyz_middle_kingdom_audioM";
                Variables.imageFilePathTable = "xyz_middle_kingdom_imageM";
                break;

            case '5':
                Variables.audioFilePathTableName = "xyz_second_intermediate_period_audioM";
                Variables.imageFilePathTable = "xyz_second_intermediate_period_imageM";
                break;

            case '6':
                Variables.audioFilePathTableName = "xyz_new_kingdom_audioM";
                Variables.imageFilePathTable = "xyz_new_kingdom_imageM";
                break;

            case '7':
                Variables.audioFilePathTableName = "xyz_third_intermediate_period_audioM";
                Variables.imageFilePathTable = "xyz_third_intermediate_period_imageM";
                break;

            case '8':
                Variables.audioFilePathTableName = "xyz_late_period_audioM";
                Variables.imageFilePathTable = "xyz_late_period_imageM";
                break;

            case '9':
                Variables.audioFilePathTableName = "xyz_hellenistic_period_audioM";
                Variables.imageFilePathTable = "xyz_hellenistic_period_imageM";
                break;

            default:
                break;


        }

        Variables.statueName = Variables.statueName.substring(2, Variables.statueName.length());

        //TODO come and comment the coming 4 lines
        Variables.audioFilePathTableName = "xyz_audio_files_pathesM";
        Variables.imageFilePathTable = "xyz_staute_image_pathM";
        Variables.statueName = barcode.displayValue;

       FetchStatueAudioFilePath();
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {

        Toast.makeText(getApplicationContext(), R.string.error_while_scanning + errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraPermissionDenied() {


    }


    public void FetchStatueAudioFilePath() {

        ConnectToInvokeObjectInfo invokeAudioFilePath = new ConnectToInvokeObjectInfo(this);
        invokeAudioFilePath.execute(Variables.selectAudioFilePathOperation,
                Variables.statueName,
                Variables.audioFileLanguage,
                Variables.audioFilePathTableName,
                Variables.imageFilePathTable);

    }

}
