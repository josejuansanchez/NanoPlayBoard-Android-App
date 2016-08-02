package org.josejuansanchez.nanoplayboard.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.adapters.RecyclerViewAdapter;
import org.josejuansanchez.nanoplayboard.models.Project;

import java.util.ArrayList;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class RecyclerViewActivity extends AppCompatActivity {

    public static final String TAG = RecyclerViewActivity.class.getSimpleName();
    private List<Project> mProjects;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        initializeProjects();
        initializeAdapter();
        loadBluetoothListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothSPP.getInstance().stopService();
    }

    private void initializeProjects() {
        mProjects = new ArrayList<>();
        mProjects.add(new Project("Potentiometer", "Display the values received from the potentiometer", R.drawable._potentiometer));
        mProjects.add(new Project("LDR", "Displays the values received from the potentiometer", R.drawable._ldr));
        mProjects.add(new Project("RGB LED", "Change the color of the RGB LED", R.drawable._ledrgb));
        mProjects.add(new Project("Buzzer", "Choose a frequency and make the buzzer sounds", R.drawable._buzzer));
        mProjects.add(new Project("Led Matrix", "Write a text and display it on the led matrix", R.drawable._ledmatrix));
        mProjects.add(new Project("Led Matrix Pattern", "Draw a pattern and display it on the led matrix", R.drawable._ledmatrix));
        mProjects.add(new Project("Led Matrix Voice", "Say something and display it on the led matrix", R.drawable._ledmatrix));
    }

    private void initializeAdapter() {
        mRecyclerView.setAdapter(new RecyclerViewAdapter(mProjects, new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Project item) {
                loadProject(mProjects.indexOf(item));
            }
        }));
    }

    private void loadProject(int position) {
        Intent intent = null;
        switch (position) {
            case 0:
                intent = new Intent(RecyclerViewActivity.this, PotentiometerActivity.class);
                break;
            case 1:
                intent = new Intent(RecyclerViewActivity.this, LDRActivity.class);
                break;
            case 2:
                intent = new Intent(RecyclerViewActivity.this, RGBActivity.class);
                break;
            case 3:
                intent = new Intent(RecyclerViewActivity.this, BuzzerActivity.class);
                break;
            case 4:
                intent = new Intent(RecyclerViewActivity.this, LedMatrixActivity.class);
                break;
            case 5:
                intent = new Intent(RecyclerViewActivity.this, LedMatrixPatternActivity.class);
                break;
            case 6:
                intent = new Intent(RecyclerViewActivity.this, LedMatrixVoiceActivity.class);
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private void loadBluetoothListeners() {
        BluetoothSPP.getInstance().setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                mMenu.clear();
                Snackbar.make(mRecyclerView, R.string.bluetooth_status_disconnected, Snackbar.LENGTH_LONG).show();
            }

            public void onDeviceConnectionFailed() {
                Snackbar.make(mRecyclerView, R.string.bluetooth_status_failed, Snackbar.LENGTH_LONG).show();
            }

            public void onDeviceConnected(String name, String address) {
                mMenu.clear();
                getMenuInflater().inflate(R.menu.menu_bluetooth_disconnection, mMenu);
                Snackbar.make(mRecyclerView, getString(R.string.bluetooth_status_connected) + name, Snackbar.LENGTH_LONG).show();
            }
        });
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
                Snackbar.make(mRecyclerView, R.string.bluetooth_status_not_enabled, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}