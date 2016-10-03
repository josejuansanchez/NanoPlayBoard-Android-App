package org.josejuansanchez.nanoplayboard.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;
import org.josejuansanchez.nanoplayboard.services.UsbService;

import java.lang.ref.WeakReference;
import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import tr.xip.markview.MarkView;

// This code is based on the example available in the UsbSerial repository:
// https://github.com/felHR85/UsbSerial

public class PotentiometerActivity extends AppCompatActivity {

    public static final String TAG = PotentiometerActivity.class.getSimpleName();

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private UsbService mUsbService;
    private MyHandler mHandler;
    private MarkView mMarkViewPotentiometer;
    private Button mButtonStart;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            mUsbService = ((UsbService.UsbBinder) arg1).getService();
            mUsbService.setHandler(mHandler);
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
        mHandler = new MyHandler(this);
        mMarkViewPotentiometer = (MarkView) findViewById(R.id.mark_potentiometer);
        mButtonStart = (Button) findViewById(R.id.button_start);
        loadListeners();
        loadBluetoothListeners();
    }

    private void loadListeners() {
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send to Arduino a Json message with the id for this Activity
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
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
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

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<PotentiometerActivity> mActivity;

        public MyHandler(PotentiometerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    if (msg.obj == null) return;
                    NanoPlayBoardMessage message = (NanoPlayBoardMessage) msg.obj;

                    if (mActivity.get() != null)
                        mActivity.get().mMarkViewPotentiometer.setMark(message.getPotentiometer());
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
