package com.research.activityinvoker.services;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TTSService extends Service implements TextToSpeech.OnInitListener {

    public static final String MESSAGE = "message";
    private TextToSpeech tts;
    private String message;
    private boolean isInit;
    private Handler handler;

    // Create text to speech object here
    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(getApplicationContext(), this);
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacksAndMessages(null);

        message = intent.getStringExtra(TTSService.MESSAGE);
        // Only call speak function if engine is initialized
        if (isInit) {
            speak();
        }

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                stopSelf();
            }
        }, 15*1000);
        // when device runs out of memory, service is killed and not restarted without being called again
        return TTSService.START_NOT_STICKY;
    }

    //Handles shutdown of tts engine
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    // Initializes text to speech object and sets locale and voice options
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Change these to alter pitch and speech rate
            tts.setSpeechRate(1.0f);
            tts.setPitch(1.0f);

            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                speak();
                isInit = true;
            }
        }
    }

    // Calls speak method of TextToSpeech, if no input is entered, a generic response is passed back
    // Each speech input is queued in texttospeech, however when a new input is queued, it empties the existing queue.
    // To disable this, change QUEUE_FLUSH to QUEUE.ADD
    private void speak() {

        if (message == null || "".equals(message)) {
            tts.speak("I didn't catch that, could you please repeat that?", TextToSpeech.QUEUE_FLUSH, null, null);
        } else
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);

    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
