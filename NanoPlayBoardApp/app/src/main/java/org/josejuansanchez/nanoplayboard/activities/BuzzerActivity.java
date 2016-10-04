package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.text.Html;
import android.widget.SeekBar;
import android.widget.TextView;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

public class BuzzerActivity extends NanoPlayBoardActivity {

    public static final String TAG = BuzzerActivity.class.getSimpleName();
    private SeekBar mSeekbar;
    private TextView mFrequencySelected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buzzer);
        setTitle("Buzzer");
        mFrequencySelected = (TextView) findViewById(R.id.frequency_selected);
        mSeekbar = (SeekBar) findViewById(R.id.seekbar_notes);
        setListeners();
    }

    private void setListeners() {
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_BUZZER_PLAY_TONE);
                message.setFrequency(progress);
                message.setDuration(125);
                sendJsonMessage(message);
                updateTextViewFrequencySelected(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateTextViewFrequencySelected(int frequency) {
        mFrequencySelected.setText(Html.fromHtml("Frequency: <b>" + Integer.toString(frequency) + "</b>"));
    }
}
