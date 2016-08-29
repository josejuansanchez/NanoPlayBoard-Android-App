package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.josejuansanchez.nanoplayboard.R;

public class MqttActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);
        setTitle("MQTT");
    }
}
