package com.example.voicebaseapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.voicebaseapp.utils.CustomProgressDialog;
import com.example.voicebaseapp.utils.Transaction;
import com.example.voicebaseapp.utils.VoiceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.example.voicebaseapp.utils.AppDefine.EN_MALE_1;
import static com.example.voicebaseapp.utils.AppDefine.MY_PERMISSION_REQUEST_RECORD_AUDIO;
import static com.example.voicebaseapp.utils.AppDefine.TTS_CODE_ONE;
import static com.example.voicebaseapp.utils.AppDefine.MAIN_ACTIVITY_TAG;

public class MainActivity extends AppCompatActivity implements VoiceManager, TextToSpeech.OnInitListener{

    private TextView txvResult;
    private TextToSpeech tts;
    private Transaction transaction;
    private CustomProgressDialog dialog;
    private int lastListeningType = 0;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private VoiceManagerListener voiceManagerListener;
    private Intent speechIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txvResult = (TextView) findViewById(R.id.txvResult);

        //////////////////////////////////////////////////////////////////////////////
        startGetPermission();

        //TTS
        textToSpeech = new TextToSpeech(MainActivity.this, this, "com.google.android.tts");

        //dialog
        dialog = new CustomProgressDialog(MainActivity.this);
        //////////////////////////////////////////////////////////////////////////////
    }

    public interface VoiceManagerListener {

        void onResults(ArrayList<String> results, int type);

        void onSpeechCompleted(int requestCode);
    }

    public void startSpeech(View view){
        transaction.start(TTS_CODE_ONE);
    }

    private void initSpeechRecognition(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(recognitionListener);

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
    }

    public void startGetPermission(){
        if (isRecognitionGranted()){
            Toast.makeText(MainActivity.this,"you Already have the permission to access SPEECH RECOGNITION",
                    Toast.LENGTH_SHORT).show();
            //                    VoiceManager voiceManager = new VoiceManagerImpl(MainActivity.this);
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(recognitionListener);

            speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);

            transaction = new Transaction(MainActivity.this, this);

            Log.i(MAIN_ACTIVITY_TAG, "you Already have the permission to access SPEECH RECOGNITION");
        }else {
            Log.i(MAIN_ACTIVITY_TAG, "you do not have the permission to access SPEECH RECOGNITION");
            requestSpeechRecognitionPermission();
        }
    }

    private void requestSpeechRecognitionPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MY_PERMISSION_REQUEST_RECORD_AUDIO);
    }

    private boolean isRecognitionGranted() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this,

                Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//                    VoiceManager voiceManager = new VoiceManagerImpl(MainActivity.this);
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                    speechRecognizer.setRecognitionListener(recognitionListener);

                    speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);

                    transaction = new Transaction(MainActivity.this, this);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = textToSpeech.setLanguage(Locale.getDefault());
            textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);  //progress listener
            TTSSettings(1,1, EN_MALE_1);  //tts config

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(MainActivity.this, "TTS language is not supported", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "TTS initialization failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void speak(final String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void speak(String speech, int requestCode) {
        speakOut(requestCode, speech);
    }

    private void speakOut(int requestCode, String text) {
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, String.valueOf(requestCode));
    }

    @Override
    public void listen(int requestCode) {

        //custom dialog
        dialog.showDialog();

        lastListeningType = requestCode;

        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                speechRecognizer.startListening(speechIntent);
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public void setVoiceManagerListener(MainActivity.VoiceManagerListener listener) {
        this.voiceManagerListener = listener;
    }

    //listenr 
    RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {

            Log.i(MAIN_ACTIVITY_TAG, "VoiceManager onBeginofSpeech");

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            Log.i(MAIN_ACTIVITY_TAG, "VoiceManager onEndOfSpeech");

        }

        @Override
        public void onError(int errorCode) {
            //custom dialog
            dialog.hideDialog();

            String errorMessage = getErrorText(errorCode);

            Log.i(MAIN_ACTIVITY_TAG, "VoiceManager onError" + errorCode);

            if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH) || (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {

                listen(lastListeningType);
            }else{
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                stopListening();
                shutdown();
            }
        }

        @Override
        public void onResults(Bundle bundle) {
            //custom dialog
            dialog.hideDialog();

            Log.i(MAIN_ACTIVITY_TAG, "VoiceManager onResults");

            ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            voiceManagerListener.onResults(results, lastListeningType);
        }

        @Override
        public void onPartialResults(Bundle bundle) {

            Log.i(MAIN_ACTIVITY_TAG, "VoiceManager onpartial");
        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };

    private void stopListening() {

        speechRecognizer.stopListening();
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public  void TTSSettings(float pitch, float speechRate, String accent) {
        textToSpeech.setPitch(pitch);
        textToSpeech.setSpeechRate(speechRate);
        Set<String> a = new HashSet<>();
        a.add("male");

        //here you can give male if you want to select mail voice.
        Voice voiceobj = new Voice(accent, Locale.getDefault(), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, a);
        textToSpeech.setVoice(voiceobj);
    }

    UtteranceProgressListener utteranceProgressListener =new UtteranceProgressListener() {
        @Override
        public void onStart(String s) {
            final String keyword = s;
            Toast.makeText(MainActivity.this, keyword, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDone(final String s) {
            final int requestCode = Integer.parseInt(s);
            Log.e(MAIN_ACTIVITY_TAG, "ON DONE IS CALLED");

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    if (voiceManagerListener != null) {
                        voiceManagerListener.onSpeechCompleted(requestCode);
                    }
                }
            };
            mainHandler.post(myRunnable);

        }

        @Override
        public void onError(String s) {
            final String keyword = s;
            Toast.makeText(MainActivity.this, keyword, Toast.LENGTH_LONG).show();
        }
    };
}
