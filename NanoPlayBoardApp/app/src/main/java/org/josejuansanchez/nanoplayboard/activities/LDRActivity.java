package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @OnClick(R.id.button_start)
    void onClick() {
        NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_LDR_READ);
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
