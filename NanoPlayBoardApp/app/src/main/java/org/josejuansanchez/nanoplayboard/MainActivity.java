package org.josejuansanchez.nanoplayboard;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.christophesmet.android.views.colorpicker.ColorPickerView;
import com.google.gson.Gson;

import org.josejuansanchez.nanoplayboard.model.LedRGB;
import org.josejuansanchez.nanoplayboard.model.NanoPlayBoardMessage;

import java.lang.ref.WeakReference;
import java.util.Set;

import tr.xip.markview.MarkView;

// This code is based on the example availabe in the UsbSerial repository:
// https://github.com/felHR85/UsbSerial

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

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
    private MarkView mMarkViewPotentiometer;
    private MarkView mMarkViewLdr;
    private ColorPickerView mColorPickerView;

    private TextView mLog;
    private EditText editText;
    private MyHandler mHandler;

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
        setContentView(R.layout.activity_main);

        mHandler = new MyHandler(this);

        mMarkViewPotentiometer = (MarkView) findViewById(R.id.mark_potentiometer);
        mMarkViewLdr = (MarkView) findViewById(R.id.mark_ldr);
        mColorPickerView = (ColorPickerView) findViewById(R.id.colorpicker);

        mColorPickerView.setDrawDebug(false);

        mColorPickerView.setColorListener(new ColorPickerView.ColorListener() {
            @Override
            public void onColorSelected(int color) {

                LedRGB message = new LedRGB();
                message.setR(Color.red(color));
                message.setG(Color.green(color));
                message.setB(Color.blue(color));

                // if UsbService was correctly binded, Send data
                if (mUsbService != null) {
                    Gson gson = new Gson();
                    Log.d(TAG, "JSON: " + gson.toJson(message));
                    mUsbService.write(gson.toJson(message).getBytes());
                }
            }
        });

        // TEST
        //mLog = (TextView) findViewById(R.id.textview_log);

        /*
        editText = (EditText) findViewById(R.id.edittext_command);
        Button sendButton = (Button) findViewById(R.id.button_send);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().equals("")) {
                    String data = editText.getText().toString();
                    if (mUsbService != null) { // if UsbService was correctly binded, Send data
                        mUsbService.write(data.getBytes());
                    }
                }
            }
        });
        */
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
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:

                    if (msg.obj == null) return;

                    NanoPlayBoardMessage message = (NanoPlayBoardMessage) msg.obj;

                    mActivity.get().mMarkViewPotentiometer.setMark(message.getPotentiometer());
                    mActivity.get().mMarkViewLdr.setMark(message.getLdr());

                    // TEST
                    //mActivity.get().mLog.append(message.getError());

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
