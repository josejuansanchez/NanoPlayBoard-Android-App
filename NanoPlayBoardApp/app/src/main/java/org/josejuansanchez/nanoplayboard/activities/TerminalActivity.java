package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.josejuansanchez.nanoplayboard.R;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class TerminalActivity extends AppCompatActivity {

    public static final String TAG = TerminalActivity.class.getSimpleName();
    private EditText mOutputMessage;
    private TextView mLogMessages;
    private Button mButtonSend;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        setTitle("Terminal");
        mOutputMessage = (EditText) findViewById(R.id.edittext_output_message);
        mLogMessages = (TextView) findViewById(R.id.textview_log_messages);
        mButtonSend = (Button) findViewById(R.id.button_send);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);
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
                    mLogMessages.append("> " + message + "\n");
                    updateScrollViewToBottom();
                }
            }
        });
    }

    private void loadBluetoothListeners() {
        BluetoothSPP.getInstance().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                mLogMessages.append("< " + message + "\n");
                updateScrollViewToBottom();
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

    // This is a temporary solution in order to solve a minor issue with the scrollView.
    // The scrollView does not scroll automatically when the textView is updated, so
    // we need to do the scrolling "manually" using this method.
    private void updateScrollViewToBottom() {
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
