package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.widget.CompoundButton;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
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

    @OnCheckedChanged(R.id.togglebutton_start_stop)
    void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            startReadingPotentiometer();
        } else {
            stopReadingPotentiometer();
        }
    }

    void startReadingPotentiometer() {
        NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_POTENTIOMETER_READ);
        sendJsonMessage(message);
    }

    void stopReadingPotentiometer() {
        NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_POTENTIOMETER_STOP);
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
