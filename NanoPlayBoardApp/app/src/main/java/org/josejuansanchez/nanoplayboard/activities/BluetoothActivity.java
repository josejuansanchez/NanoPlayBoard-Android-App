package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.josejuansanchez.nanoplayboard.R;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class BluetoothActivity extends AppCompatActivity {

    BluetoothSPP mBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        mBT = new BluetoothSPP(this);
    }

    public void onStart() {
        super.onStart();
        if(!mBT.isBluetoothEnabled()) {
            // Do somthing if bluetooth is disable
            // TODO
        } else {
            // Do something if bluetooth is already enable
            mBT.startService(BluetoothState.DEVICE_OTHER);
        }
    }
}
