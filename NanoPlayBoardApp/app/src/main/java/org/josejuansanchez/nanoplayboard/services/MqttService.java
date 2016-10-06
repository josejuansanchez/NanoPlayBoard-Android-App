package org.josejuansanchez.nanoplayboard.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.josejuansanchez.nanoplayboard.events.MqttStringEvent;

public class MqttService extends Service {
    public static final String TAG = MqttService.class.getSimpleName();
    private static final int CONNECT_TIMEOUT = 2000;
    private final IBinder mBinder = new LocalBinder();
    private MqttAsyncClient mMqttClient = null;

    public class LocalBinder extends Binder {
        public MqttService getService() {
            return MqttService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        if (isConnected()) disconnect();
    }

    public void connect(final String uri, final String clientId, final String username, final String password) {
        try {
            mMqttClient = new MqttAsyncClient(uri, clientId, null);
            mMqttClient.setCallback(new MyMqttCallback());
        } catch (MqttException e) {
            e.printStackTrace();
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        try {
            final IMqttToken connectToken = mMqttClient.connect(options);
            connectToken.waitForCompletion(CONNECT_TIMEOUT);
        } catch (MqttException e) {
            Log.d(TAG, "Connection attempt failed with reason code: " + e.getReasonCode() + ":" + e.getCause());
            e.printStackTrace();
        }
    }

    public boolean connect(final String uri, final String clientId) {
        try {
            mMqttClient = new MqttAsyncClient(uri, clientId, null);
            mMqttClient.setCallback(new MyMqttCallback());
        } catch (MqttException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        try {
            final IMqttToken connectToken = mMqttClient.connect();
            connectToken.waitForCompletion(CONNECT_TIMEOUT);
        } catch (MqttException e) {
            Log.d(TAG, "Connection attempt failed with reason code: " + e.getReasonCode() + ":" + e.getCause());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void publish(final String topic, final String payload) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(payload.getBytes());
            mMqttClient.publish(topic, mqttMessage);
        }
        catch (MqttException e) {
            Log.d(TAG, "Publish failed with reason code: " + e.getReasonCode());
            e.printStackTrace();
        }
    }

    public void subscribe(final String topic, int qos) {
        try {
            mMqttClient.subscribe(topic, qos);
        }
        catch (MqttException e) {
            Log.d(TAG, "Subscribe failed with reason code: " + e.getReasonCode());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            mMqttClient.disconnect();
        } catch (MqttException e) {
            Log.d(TAG, "Disconnect failed with reason code: " + e.getReasonCode());
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        if (mMqttClient == null) return false;
        return mMqttClient.isConnected();
    }

    public class MyMqttCallback implements MqttCallback {
        public void connectionLost(Throwable cause) {
            Log.d(TAG, "MQTT Server connection lost: " + cause.toString());
            cause.printStackTrace();
        }

        public void messageArrived(String topic, MqttMessage message) {
            Log.d(TAG, "Message arrived: " + topic + ":" + message.toString());
            EventBus.getDefault().post(new MqttStringEvent(topic, message.toString()));
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(TAG, "Delivery complete");
        }
    }
}
