package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

import tr.xip.markview.MarkView;

public class PotentiometerActivity extends NanoPlayBoardActivity {

    public static final String TAG = PotentiometerActivity.class.getSimpleName();
    private MarkView mMarkViewPotentiometer;
    private Button mButtonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_potentiometer);
        setTitle("Potentiometer");
        mMarkViewPotentiometer = (MarkView) findViewById(R.id.mark_potentiometer);
        mButtonStart = (Button) findViewById(R.id.button_start);
        setListeners();
    }

    private void setListeners() {
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_POTENTIOMETER_READ);
                sendJsonMessage(message);
            }
        });
    }

    @Override
    public void onUsbSerialMessage(NanoPlayBoardMessage message) {
        if (message.getPotentiometer() == null) return;
        mMarkViewPotentiometer.setMark(message.getPotentiometer().intValue());
    }

    @Override
    public void onBluetoothMessage(NanoPlayBoardMessage message) {
        if (message.getPotentiometer() == null) return;
        mMarkViewPotentiometer.setMark(message.getPotentiometer().intValue());
    }
}
