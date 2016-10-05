package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.josejuansanchez.nanoplayboard.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TerminalActivity extends NanoPlayBoardActivity {

    public static final String TAG = TerminalActivity.class.getSimpleName();
    @BindView(R.id.edittext_output_message) EditText mOutputMessage;
    @BindView(R.id.textview_log_messages) TextView mLogMessages;
    @BindView(R.id.scrollview) ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        setTitle("Terminal");
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_send)
    void onClick() {
        String data = mOutputMessage.getText().toString();
        if (!data.trim().equals("")) {
            sendString(data);
            mLogMessages.append("> " + data + "\n");
            updateScrollViewToBottom();
        }
    }

    @Override
    protected void onBluetoothString(String data) {
        mLogMessages.append("< " + data + "\n");
        updateScrollViewToBottom();
    }

    @Override
    protected void onUsbSerialString(String data) {
        mLogMessages.append("< " + data + "\n");
        updateScrollViewToBottom();
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
