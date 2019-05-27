package com.example.voicebaseapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import static com.example.voicebaseapp.utils.AppDefine.MY_PERMISSION_REQUEST_RECORD_AUDIO;


public class permissionModule {
    private Activity activity;

    public permissionModule(Activity activity) {
        this.activity = activity;
    }

    public void startGetPermission(){
        if (isRecognitionGranted()){
            Toast.makeText(activity,"you Already have the permission to access SPEECH RECOGNITION",
                    Toast.LENGTH_SHORT).show();
        }else {
            requestSpeechRecognitionPermission();
        }
    }

    private void requestSpeechRecognitionPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MY_PERMISSION_REQUEST_RECORD_AUDIO);
    }

    private boolean isRecognitionGranted() {
        int result = ContextCompat.checkSelfPermission(activity,

                Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
