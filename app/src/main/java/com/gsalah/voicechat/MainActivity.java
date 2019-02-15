package com.gsalah.voicechat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements RecognitionListener, View.OnTouchListener {
    static final int PERMISSION_REQUEST = 123;

    private Context context = this;
    EditText msgEditText;
    Button speakButton;
    TextToSpeech textToSpeech;
    SpeechRecognizer speechRecognizer;
    ArabicTTS arabicTTS;
    Intent speechRecognizerIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        msgEditText = findViewById(R.id.message_edtxt);
        speakButton = findViewById(R.id.speak_btn);
        speakButton.setOnTouchListener(this);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.US);
                Read(null);
            }
        });
        arabicTTS = new ArabicTTS();
        arabicTTS.prepare(this);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-JO");
    }

    void Read(View view) {
        arabicTTS.talk(msgEditText.getText().toString());
//        if (textToSpeech != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                textToSpeech.speak(msgEditText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
//            } else {
//                textToSpeech.speak(msgEditText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
//            }
//        }
    }

    void requestPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) !=
                                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS, Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST);
        }
    }

    void messageDialog(String message) {
        new AlertDialog.Builder(context)
                .setTitle("Alert")
                .setMessage(message)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_SMS, Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST);
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            boolean granted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (!granted) {
                messageDialog(getString(R.string.permession_msg));
            }
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(int error) {
        speakButton.setEnabled(true);
    }

    @Override
    public void onResults(Bundle results) {
        speakButton.setEnabled(true);
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        //displaying the first match
        if (matches != null) {
            msgEditText.setText(matches.get(0));
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                speakButton.setEnabled(false);
                speechRecognizer.stopListening();
                msgEditText.setHint("You will see input here");
                break;

            case MotionEvent.ACTION_DOWN:
                speechRecognizer.startListening(speechRecognizerIntent);
                msgEditText.setText("");
                msgEditText.setHint("Listening...");
                break;
        }
        return false;
    }
}
