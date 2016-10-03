package org.josejuansanchez.nanoplayboard.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.events.UsbSerialActionEvent;
import org.josejuansanchez.nanoplayboard.events.UsbSerialMessageEvent;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;
import org.josejuansanchez.nanoplayboard.services.UsbService;

import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import tr.xip.markview.MarkView;

// This code is based on the example available in the UsbSerial repository:
// https://github.com/felHR85/UsbSerial

public class PotentiometerActivity extends AppCompatActivity {

    public static final String TAG = PotentiometerActivity.class.getSimpleName();
    private UsbService mUsbService;
    private MarkView mMarkViewPotentiometer;
    private Button mButtonStart;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            mUsbService = ((UsbService.UsbBinder) arg1).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mUsbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_potentiometer);
        setTitle("Potentiometer");
        mMarkViewPotentiometer = (MarkView) findViewById(R.id.mark_potentiometer);
        mButtonStart = (Button) findViewById(R.id.button_start);
        loadListeners();
        loadBluetoothListeners();
    }

    private void loadListeners() {
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send to NanoPlayBoard a Json message
                sendJsonMessage(ProtocolConstants.ID_POTENTIOMETER_READ);
            }
        });
    }

    private void loadBluetoothListeners() {
        BluetoothSPP.getInstance().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.d(TAG, "Message: " + message);

                try {
                    Gson gson = new Gson();
                    NanoPlayBoardMessage npbMessage = gson.fromJson(message, NanoPlayBoardMessage.class);
                    mMarkViewPotentiometer.setMark(npbMessage.getPotentiometer());
                } catch(JsonSyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    private void sendJsonMessage(int id) {
        // Create the Json message
        NanoPlayBoardMessage message = new NanoPlayBoardMessage(id);
        Gson gson = new Gson();
        Log.d(TAG, "JSON: " + gson.toJson(message));

        // if UsbService was correctly binded, send data
        if (mUsbService != null) {
            mUsbService.write(gson.toJson(message).getBytes());
            mUsbService.write("\n".getBytes());
        }

        // if Bluetooth service is correctly connected, send data
        if (BluetoothSPP.getInstance().isServiceAvailable()) {
            BluetoothSPP.getInstance().send(gson.toJson(message).getBytes(), false);
            BluetoothSPP.getInstance().send("\n".getBytes(), false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        startService(UsbService.class, usbConnection, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UsbSerialMessageEvent event) {
        switch (event.what) {
            case UsbService.MESSAGE_FROM_SERIAL_PORT:
                if (event.obj == null) return;
                NanoPlayBoardMessage message = (NanoPlayBoardMessage) event.obj;
                mMarkViewPotentiometer.setMark(message.getPotentiometer());
                break;
            case UsbService.CTS_CHANGE:
                Toast.makeText(this, "CTS_CHANGE",Toast.LENGTH_LONG).show();
                break;
            case UsbService.DSR_CHANGE:
                Toast.makeText(this, "DSR_CHANGE",Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UsbSerialActionEvent event) {
        switch (event.action) {
            case UsbService.ACTION_USB_PERMISSION_GRANTED:
                Toast.makeText(this, "USB Ready", Toast.LENGTH_SHORT).show();
                break;
            case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED:
                Toast.makeText(this, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                break;
            case UsbService.ACTION_NO_USB:
                Toast.makeText(this, "No USB connected", Toast.LENGTH_SHORT).show();
                break;
            case UsbService.ACTION_USB_DISCONNECTED:
                Toast.makeText(this, "USB disconnected", Toast.LENGTH_SHORT).show();
                break;
            case UsbService.ACTION_USB_NOT_SUPPORTED:
                Toast.makeText(this, "USB device not supported", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
