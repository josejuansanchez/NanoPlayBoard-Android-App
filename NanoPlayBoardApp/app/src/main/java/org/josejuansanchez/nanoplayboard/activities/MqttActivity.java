package org.josejuansanchez.nanoplayboard.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;
import org.josejuansanchez.nanoplayboard.services.MqttService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class MqttActivity extends NanoPlayBoardActivity {

    public static final String TAG = MqttActivity.class.getSimpleName();
    private String uri = "tcp://test.mosquitto.org:1883";
    private String port = "1883";
    private String topicPublish = "nanoplayboard";
    private String topicSubscribe = "nanoplayboard";
    private String clientId = "";
    private String username = "";
    private String password = "";

    @BindView(R.id.edittext_broker_url) EditText mBrokerUrl;
    @BindView(R.id.edittext_port) EditText mPort;
    @BindView(R.id.edittext_username) EditText mUsername;
    @BindView(R.id.edittext_password) EditText mPassword;
    @BindView(R.id.edittext_topic_publish) EditText mTopicPublish;
    @BindView(R.id.togglebutton_publish) ToggleButton mToggleButtonPublish;

    MqttService mService;
    boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MqttService.LocalBinder binder = (MqttService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            // TODO: Review
            mService.connect(uri, clientId);
            mService.subscribe(topicSubscribe, 1);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);
        setTitle("MQTT");
        ButterKnife.bind(this);
    }

    @OnCheckedChanged(R.id.togglebutton_publish)
    void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_POTENTIOMETER_READ);
            sendJsonMessage(message);
            if (mBound) {
                mService.connect(uri, clientId);
                mService.subscribe(topicSubscribe, 1);
            }
        } else {
            // TODO: Include a new message to stop the reading process in the board
            if (mBound) mService.disconnect();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MqttService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onUsbSerialMessage(NanoPlayBoardMessage message) {
    }

    @Override
    public void onUsbSerialString(String data) {
    }

    @Override
    public void onBluetoothMessage(NanoPlayBoardMessage message) {
    }

    @Override
    public void onBluetoothString(String data) {
        if (mBound) {
            mService.publish(topicPublish, data);
        }
    }
}
