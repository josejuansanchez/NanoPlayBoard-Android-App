package org.josejuansanchez.nanoplayboard.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.events.MqttStringEvent;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;
import org.josejuansanchez.nanoplayboard.services.MqttService;
import org.josejuansanchez.nanoplayboard.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import tr.xip.markview.MarkView;

public class TestMqttSubscribe extends NanoPlayBoardActivity {

    public static final String TAG = TestMqttSubscribe.class.getSimpleName();
    private String uri;
    private String url;
    private String port;
    private String topicSubscribe;
    private String clientId;
    private String username;
    private String password;
    private final String MQTT_DEFAULT_BROKER_URL = "tcp://test.mosquitto.org";
    private final String MQTT_DEFAULT_PORT = "1883";
    private final String MQTT_DEFAULT_TOPIC_SUBSCRIBE = "nanoplayboard";

    @BindView(R.id.mark_potentiometer) MarkView mMarkViewPotentiometer;

    Utils mUtils;
    MqttService mService;
    boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MqttService.LocalBinder binder = (MqttService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            // TODO: Temporary solution
            uri = MQTT_DEFAULT_BROKER_URL + ":" + MQTT_DEFAULT_PORT;
            clientId = mUtils.getIpAddress();
            if (mService.connect(uri, clientId)) {
                mService.subscribe(MQTT_DEFAULT_TOPIC_SUBSCRIBE, 1);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mqtt_subscribe);
        ButterKnife.bind(this);
        mUtils = new Utils(this);
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
    protected void onMqttMessage(MqttStringEvent event) {
        Log.d(TAG, event.topic + " : " + event.topic);

        Gson gson = new Gson();
        final NanoPlayBoardMessage message = gson.fromJson(event.data, NanoPlayBoardMessage.class);
        mMarkViewPotentiometer.setMark(message.getPotentiometer().intValue());

        //if (message.getPotentiometer() == null) return;
        //mMarkViewPotentiometer.setMark(message.getPotentiometer().intValue());
    }
}
