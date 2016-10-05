package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tr.xip.markview.MarkView;

public class PotentiometerActivity extends NanoPlayBoardActivity {

    public static final String TAG = PotentiometerActivity.class.getSimpleName();
    @BindView(R.id.mark_potentiometer) MarkView mMarkViewPotentiometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_potentiometer);
        setTitle("Potentiometer");
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_start)
    void onClick() {
        NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_POTENTIOMETER_READ);
        sendJsonMessage(message);
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
