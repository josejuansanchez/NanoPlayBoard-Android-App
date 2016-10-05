package org.josejuansanchez.nanoplayboard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class LedMatrixVoiceActivity extends NanoPlayBoardActivity {

    public static final String TAG = LedMatrixVoiceActivity.class.getSimpleName();
    private static final int SPEECH_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_matrix_voice);
        setTitle("Led Matrix");
        ButterKnife.bind(this);
    }

    @OnClick({R.id.button_speak, R.id.image_speak})
    void onClick() {
        displaySpeechRecognizer();
    }

    // Source: https://developer.android.com/training/wearables/apps/voice.html
    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.d(TAG, "spokenText: "  +spokenText);
            NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_LEDMATRIX_PRINT_STRING);
            message.setText(spokenText);
            sendJsonMessage(message);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
