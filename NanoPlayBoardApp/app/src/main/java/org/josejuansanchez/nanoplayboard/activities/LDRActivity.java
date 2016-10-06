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

public class LDRActivity extends NanoPlayBoardActivity {

    public static final String TAG = LDRActivity.class.getSimpleName();
    @BindView(R.id.mark_ldr) MarkView mMarkViewLdr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ldr);
        setTitle("LDR");
        ButterKnife.bind(this);
    }

    @OnCheckedChanged(R.id.togglebutton_start_stop)
    void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            startReadingLdr();
        } else {
            stopReadingLdr();
        }
    }

    void startReadingLdr() {
        NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_LDR_READ);
        sendJsonMessage(message);
    }

    void stopReadingLdr() {
        NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_LDR_STOP);
        sendJsonMessage(message);
    }

    @Override
    public void onUsbSerialMessage(NanoPlayBoardMessage message) {
        if (message.getLdr() == null) return;
        mMarkViewLdr.setMark(message.getLdr().intValue());
    }

    @Override
    public void onBluetoothMessage(NanoPlayBoardMessage message) {
        if (message.getLdr() == null) return;
        mMarkViewLdr.setMark(message.getLdr().intValue());
    }
}
