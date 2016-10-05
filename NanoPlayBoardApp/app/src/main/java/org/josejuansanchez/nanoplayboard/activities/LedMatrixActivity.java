package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.widget.EditText;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LedMatrixActivity extends NanoPlayBoardActivity {

    public static final String TAG = LedMatrixActivity.class.getSimpleName();
    @BindView(R.id.edittext_ledmatrix) EditText mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_matrix);
        setTitle("Led Matrix");
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_send_ledmatrix)
    void onClick() {
        String text = mText.getText().toString();
        if (!text.trim().equals("")) {
            NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_LEDMATRIX_PRINT_STRING);
            message.setText(text);
            sendJsonMessage(message);
        }
    }
}
