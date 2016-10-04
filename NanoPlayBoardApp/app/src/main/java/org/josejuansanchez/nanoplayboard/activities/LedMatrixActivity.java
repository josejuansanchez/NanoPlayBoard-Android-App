package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

public class LedMatrixActivity extends NanoPlayBoardActivity {

    public static final String TAG = LedMatrixActivity.class.getSimpleName();
    private EditText mText;
    private Button mButtonSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_matrix);
        setTitle("Led Matrix");
        mText = (EditText) findViewById(R.id.edittext_ledmatrix);
        mButtonSend = (Button) findViewById(R.id.button_send_ledmatrix);
        setListeners();
    }

    private void setListeners() {
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mText.getText().toString();
                if (!text.trim().equals("")) {
                    NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_LEDMATRIX_PRINT_STRING);
                    message.setText(text);
                    sendJsonMessage(message);
                }
            }
        });
    }
}
