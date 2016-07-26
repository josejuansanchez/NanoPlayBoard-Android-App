package org.josejuansanchez.nanoplayboard.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.josejuansanchez.nanoplayboard.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    private Menu mMenu;
    private ListView mListview;
    private String[] mValues = new String[] {
            "Potentiometer",
            "LDR",
            "RGB LED",
            "Buzzer",
            "Led Matrix",
            "Led Matrix Pattern",
            "Led Matrix Voice"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListview = (ListView) findViewById(R.id.listview);

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < mValues.length; ++i) {
            list.add(mValues[i]);
        }
        final BasicArrayAdapter adapter = new BasicArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        mListview.setAdapter(adapter);

        loadUIListeners();
        loadBluetoothListeners();
    }

    private void loadUIListeners() {
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                Intent intent = null;
                switch (position) {
                    case 0:
                        intent = new Intent(MainActivity.this, PotentiometerActivity.class);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, LDRActivity.class);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, RGBActivity.class);
                        break;
                    case 3:
                        intent = new Intent(MainActivity.this, BuzzerActivity.class);
                        break;
                    case 4:
                        intent = new Intent(MainActivity.this, LedMatrixActivity.class);
                        break;
                    case 5:
                        intent = new Intent(MainActivity.this, LedMatrixPatternActivity.class);
                        break;
                    case 6:
                        intent = new Intent(MainActivity.this, LedMatrixVoiceActivity.class);
                        break;
                }
                startActivity(intent);
            }

        });
    }

    private void loadBluetoothListeners() {
        BluetoothSPP.getInstance().setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                mMenu.clear();
                getMenuInflater().inflate(R.menu.menu_bluetooth_connection, mMenu);
                Toast.makeText(MainActivity.this,
                        "Bluetooth device disconnected",
                        Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(MainActivity.this,
                        "Connection has failed",
                        Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnected(String name, String address) {
                mMenu.clear();
                getMenuInflater().inflate(R.menu.menu_bluetooth_disconnection, mMenu);
                Toast.makeText(MainActivity.this,
                        "Connected to " + name,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothSPP.getInstance().stopService();
    }

    private class BasicArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public BasicArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_bluetooth_connection, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.menu_bluetooth_connect:
                if (!BluetoothSPP.getInstance().isBluetoothEnabled()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
                } else {
                    if(!BluetoothSPP.getInstance().isServiceAvailable()) {
                        BluetoothSPP.getInstance().setupService();
                    }
                    BluetoothSPP.getInstance().setDeviceTarget(BluetoothState.DEVICE_OTHER);
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
                break;

            case R.id.menu_bluetooth_disconnect:
                if(BluetoothSPP.getInstance().getServiceState() == BluetoothState.STATE_CONNECTED) {
                    BluetoothSPP.getInstance().disconnect();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                BluetoothSPP.getInstance().connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                BluetoothSPP.getInstance().setupService();
                BluetoothSPP.getInstance().setDeviceTarget(BluetoothState.DEVICE_OTHER);
                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

}
