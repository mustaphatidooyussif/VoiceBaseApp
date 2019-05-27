package com.example.voicebaseapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.voicebaseapp.MainActivity;
import com.example.voicebaseapp.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class VoiceManagerImpl implements VoiceManager, TextToSpeech.OnInitListener {
    private Activity activity;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private VoiceManagerListener voiceManagerListener;
    private int lastListeningType = 0;
    private Intent speechIntent;
    private final String EN_MALE_1 = "en-us-x-sfg#male_1-local";
    private TextView  txvResult;
    private CustomProgressDialog dialog;
    private int permissionCheck;
    private final int MY_PERMISSION_REQUEST_RECORD_AUDIO = 1;

    public interface VoiceManagerListener {

        void onResults(ArrayList<String> results, int type);

        void onSpeechCompleted(int requestCode);
    }

    public VoiceManagerImpl(final Activity activity) {
        txvResult = (TextView) activity.findViewById(R.id.txvResult);
        this.activity = activity;

        initSpeechRecognition();

        //TTS
        textToSpeech = new TextToSpeech(activity, this, "com.google.android.tts");

        //dialog
        dialog = new CustomProgressDialog(activity);
    }

    private void initSpeechRecognition(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        speechRecognizer.setRecognitionListener(recognitionListener);

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
    }



    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = textToSpeech.setLanguage(Locale.getDefault());
            textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);  //progress listener
            TTSSettings(1,1, EN_MALE_1);  //tts config

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(activity, "TTS language is not supported", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(activity, "TTS initialization failed", Toast.LENGTH_LONG).show();
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

    }


    private void speak(final int requestCode, final String speech) {

        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }

        textToSpeech = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    TTSSettings(1,1, EN_MALE_1);
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
                    textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(requestCode));
                }
            }
        });
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

    private void stopListening() {

        speechRecognizer.stopListening();
    }


    RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
        }

        @Override
        public void onBeginningOfSpeech() {

            Log.i("VOICE", "VoiceManager onBeginofSpeech");

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            Log.i("VOICE", "VoiceManager onEndOfSpeech");

        }

        @Override
        public void onError(int errorCode) {
            //custom dialog
            dialog.hideDialog();

            String errorMessage = getErrorText(errorCode);

            Log.i("VOICE", "VoiceManager onError" + errorCode);

            if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH) || (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {

                listen(lastListeningType);
            }else{
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
                stopListening();
                shutdown();
            }
        }

        @Override
        public void onResults(Bundle bundle) {
            //custom dialog
            dialog.hideDialog();

            Log.i("VOICE", "VoiceManager onResults");

            ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            voiceManagerListener.onResults(results, lastListeningType);
        }

        @Override
        public void onPartialResults(Bundle bundle) {

            Log.i("VOICE", "VoiceManager onpartial");
        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };


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

    UtteranceProgressListener utteranceProgressListener =new UtteranceProgressListener() {
        @Override
        public void onStart(String s) {
            final String keyword = s;
            Toast.makeText(activity, keyword, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDone(final String s) {
            final int requestCode = Integer.parseInt(s);
            Log.e("TAG", "ON DONE IS CALLED");

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
            Toast.makeText(activity, keyword, Toast.LENGTH_LONG).show();
        }
    };
}
