package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.josejuansanchez.nanoplayboard.R;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class TerminalActivity extends AppCompatActivity {

    public static final String TAG = TerminalActivity.class.getSimpleName();
    private EditText mOutputMessage;
    private TextView mInputMessage;
    private Button mButtonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        setTitle("Terminal");
        mOutputMessage = (EditText) findViewById(R.id.edittext_output_message);
        mInputMessage = (TextView) findViewById(R.id.textview_input_message);
        mButtonSend = (Button) findViewById(R.id.button_send);
        loadListeners();
        loadBluetoothListeners();
    }

    private void loadListeners() {
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mOutputMessage.getText().toString();
                if (!message.trim().equals("")) {
                    sendMessage(message);
                }
            }
        });
    }

    private void loadBluetoothListeners() {
        BluetoothSPP.getInstance().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                mInputMessage.append(message + "\n");
            }
        });
    }

    private void sendMessage(String message) {
        // if Bluetooth service is correctly connected, send data
        if (BluetoothSPP.getInstance().isServiceAvailable()) {
            BluetoothSPP.getInstance().send(message.getBytes(), false);
            BluetoothSPP.getInstance().send("\n".getBytes(), false);
        }
    }

}
