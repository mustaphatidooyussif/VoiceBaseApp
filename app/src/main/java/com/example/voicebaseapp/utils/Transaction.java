package com.example.voicebaseapp.utils;

import android.util.Log;
import android.widget.TextView;

import com.example.voicebaseapp.MainActivity;
import com.example.voicebaseapp.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.voicebaseapp.utils.AppDefine.TTS_CODE_FIVE;
import static com.example.voicebaseapp.utils.AppDefine.TTS_CODE_FOUR;
import static com.example.voicebaseapp.utils.AppDefine.TTS_CODE_ONE;
import static com.example.voicebaseapp.utils.AppDefine.TTS_CODE_THREE;
import static com.example.voicebaseapp.utils.AppDefine.TTS_CODE_TWO;
import static com.example.voicebaseapp.utils.AppDefine.TRANS_CLASS_TAG;

public class Transaction implements MainActivity.VoiceManagerListener{
    private final VoiceManager voiceManager;
    private TextView txvResult;
    WeakReference<MainActivity> activityReference;

    public Transaction(MainActivity activity, VoiceManager voiceManager){
        txvResult = (TextView) activity.findViewById(R.id.txvResult);
        this.activityReference = new WeakReference<>(activity);
        this.voiceManager = voiceManager;
        voiceManager.setVoiceManagerListener(this);
    }

    public boolean toTopUpCredit(String text){
        return (text.toUpperCase().contains("BUY") || text.toUpperCase().contains("TOP")
                || text.toUpperCase().contains("TOP-UP") || text.toUpperCase().contains("UP")
                || text.toUpperCase().contains("TOP UP") || text.toUpperCase().contains("PURCHASE")
                || text.toUpperCase().contains("AIRTIME")|| text.toUpperCase().contains("AIR TIME"));
    }

    public boolean contaninsDigits(String text){
        // Regular expression pattern to test input
        String regex1 = "(.)*(\\d)(.)*";
        Pattern pattern = Pattern.compile(regex1);
        Matcher matcher = pattern.matcher(text);
        boolean isMatched = matcher.matches();
        return isMatched;
    }

    public void start(int code){
        voiceManager.listen(code);
    }
    //Get a number from the string
    public String getDigits(String text){
        return text.replaceAll("[^0-9]", "");
    }

    @Override
    public void onResults(ArrayList<String> results, int type) {

        String result = results.get(0);

        switch (type){
            case TTS_CODE_ONE:
                if(toTopUpCredit(result)){
                    if(contaninsDigits(result)){

                        final String msg = "Are you sure you’d like to top up "+ getDigits(result)+ " cedis credit?";
                        voiceManager.speak(msg, TTS_CODE_ONE);
                    }else{
                        voiceManager.speak("How much credit do you want to top up?", TTS_CODE_TWO);
                    }

                }else{
                    voiceManager.speak("I can only sell credits");
                }
                break;
            case TTS_CODE_TWO:
                if(result.toLowerCase().contains("yes") || result.toLowerCase().contains("of course")
                        || result.toLowerCase().contains("sure") || result.toLowerCase().contains("confirm")){
                    voiceManager.speak("Sure, I’ll process your transaction");

                }else{
                    voiceManager.speak("How much credit do you want to top up?", TTS_CODE_THREE);
                }
                break;
            case TTS_CODE_THREE:
                if(contaninsDigits(result)){
                    final String msg = "Are you sure you’d like to top up "+ getDigits(result)+ " cedis credit?";
                    voiceManager.speak(msg, TTS_CODE_FOUR);

                }else{
                    voiceManager.speak("How much credit do you want to top up?", TTS_CODE_FIVE);
                }
                break;
        }

    }

    @Override
    public void onSpeechCompleted(int requestCode) {
        switch (requestCode){
            case TTS_CODE_ONE:
                Log.i(TRANS_CLASS_TAG, "String ONE");
                voiceManager.listen(TTS_CODE_TWO);
                break;
            case TTS_CODE_TWO:
                Log.i(TRANS_CLASS_TAG, "String TWO");
                voiceManager.listen(TTS_CODE_THREE);
                break;
            case TTS_CODE_THREE:
                Log.i(TRANS_CLASS_TAG, "String THREE");
                voiceManager.listen(TTS_CODE_THREE);
                break;
            case TTS_CODE_FOUR:
                Log.i(TRANS_CLASS_TAG, "String FOUR");
                voiceManager.listen(TTS_CODE_TWO);
                break;
            case TTS_CODE_FIVE:
                Log.i(TRANS_CLASS_TAG, "String FIVE");
                voiceManager.listen(TTS_CODE_THREE);
                break;
            default:
                break;
        }
    }
}
