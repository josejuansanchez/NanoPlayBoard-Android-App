package org.josejuansanchez.nanoplayboard.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;
import org.josejuansanchez.nanoplayboard.services.MqttService;
import org.josejuansanchez.nanoplayboard.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class MqttActivity extends NanoPlayBoardActivity {

    public static final String TAG = MqttActivity.class.getSimpleName();
    private String uri;
    private String url;
    private String port;
    private String topicPublish;
    private String topicSubscribe;
    private String clientId;
    private String username;
    private String password;
    private final String MQTT_DEFAULT_BROKER_URL = "tcp://test.mosquitto.org";
    private final String MQTT_DEFAULT_PORT = "1883";
    private final String MQTT_DEFAULT_TOPIC_PUBLISH = "nanoplayboard";

    @BindView(R.id.edittext_broker_url) EditText mBrokerUrl;
    @BindView(R.id.edittext_port) EditText mPort;
    @BindView(R.id.edittext_username) EditText mUsername;
    @BindView(R.id.edittext_password) EditText mPassword;
    @BindView(R.id.edittext_topic_publish) EditText mTopicPublish;
    @BindView(R.id.togglebutton_publish) ToggleButton mToggleButtonPublish;

    Utils mUtils;
    MqttService mService;
    boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MqttService.LocalBinder binder = (MqttService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
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
        readFromSharedPreferences();
        setMqttSettings();
        mUtils = new Utils(this);
    }

    @OnCheckedChanged(R.id.togglebutton_publish)
    void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (validateMqttSettings() == false) return;
            getMqttSettings();

            NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_POTENTIOMETER_READ);
            sendJsonMessage(message);

            if (mBound == false) return;
            if (mService.connect(uri, clientId)) {
                Snackbar.make(mBrokerUrl, R.string.mqtt_successful_connection, Snackbar.LENGTH_LONG).show();
                writeToSharedPreferences();
            } else {
                Snackbar.make(mBrokerUrl, R.string.mqtt_error_connect, Snackbar.LENGTH_LONG).show();
                mToggleButtonPublish.setChecked(false);
            }
        } else {
            NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_POTENTIOMETER_STOP);
            sendJsonMessage(message);

            if (mBound && mService.isConnected()) mService.disconnect();
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
        if (mBound && mService.isConnected()) {
            mService.publish(topicPublish, data);
        }
    }

    private void setMqttSettings() {
        mBrokerUrl.setText(url);
        mPort.setText(port);
        mTopicPublish.setText(topicPublish);
    }

    private void getMqttSettings() {
        url = mBrokerUrl.getText().toString().trim();

        if (!mPort.getText().toString().trim().isEmpty()) {
            port = mPort.getText().toString().trim();
        } else {
            port = MQTT_DEFAULT_PORT;
        }

        uri = url + ":" + port;
        topicPublish = mTopicPublish.getText().toString().trim();
        clientId = mUtils.getIpAddress();
    }

    private boolean validateMqttSettings() {
        if (mBrokerUrl.getText().toString().trim().isEmpty()) {
            Snackbar.make(mBrokerUrl, R.string.mqtt_error_broker_url, Snackbar.LENGTH_LONG).show();
            return false;
        }

        if (mTopicPublish.getText().toString().trim().isEmpty()) {
            Snackbar.make(mBrokerUrl, R.string.mqtt_error_topic_publish, Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void writeToSharedPreferences() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_mqtt_broker_url), url);
        editor.putString(getString(R.string.saved_mqtt_port), port);
        editor.putString(getString(R.string.saved_mqtt_topic_publish), topicPublish);
        editor.commit();
    }

    private void readFromSharedPreferences() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        url = sharedPref.getString(getString(R.string.saved_mqtt_broker_url), MQTT_DEFAULT_BROKER_URL);
        port = sharedPref.getString(getString(R.string.saved_mqtt_port), MQTT_DEFAULT_PORT);
        topicPublish = sharedPref.getString(getString(R.string.saved_mqtt_topic_publish), MQTT_DEFAULT_TOPIC_PUBLISH);
    }
}
