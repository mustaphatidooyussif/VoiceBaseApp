package com.example.voicebaseapp.utils;

import com.example.voicebaseapp.MainActivity;

public interface VoiceManager {

    void speak(String text);

    void speak( String speech,  int requestCode);

    void listen(int requestCode);

    void shutdown();

    void setVoiceManagerListener(MainActivity.VoiceManagerListener listener);
}
