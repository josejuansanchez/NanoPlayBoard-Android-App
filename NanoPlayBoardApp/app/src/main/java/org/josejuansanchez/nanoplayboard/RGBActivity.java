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
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.christophesmet.android.views.colorpicker.ColorPickerView;
import com.google.gson.Gson;

import org.josejuansanchez.nanoplayboard.model.LedRGB;
import org.josejuansanchez.nanoplayboard.model.NanoPlayBoardMessage;

import java.lang.ref.WeakReference;
import java.util.Set;

// This code is based on the example available in the UsbSerial repository:
// https://github.com/felHR85/UsbSerial

public class RGBActivity extends AppCompatActivity {

    public static final String TAG = RGBActivity.class.getSimpleName();

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
    private ColorPickerView mColorPickerView;
    private View mColorSectedView;
    private TextView mColorSelectedRed;
    private TextView mColorSelectedGreen;
    private TextView mColorSelectedBlue;

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
        setContentView(R.layout.activity_rgb);

        mHandler = new MyHandler(this);
        mColorPickerView = (ColorPickerView) findViewById(R.id.colorpicker);
        mColorSectedView = (View) findViewById(R.id.colorselected_view);
        mColorSelectedRed = (TextView) findViewById(R.id.colorselected_red);
        mColorSelectedGreen = (TextView) findViewById(R.id.colorselected_green);
        mColorSelectedBlue = (TextView) findViewById(R.id.colorselected_blue);
        mColorPickerView.setDrawDebug(false);

        loadListeners();
    }

    public void loadListeners() {
        mColorPickerView.setColorListener(new ColorPickerView.ColorListener() {
            @Override
            public void onColorSelected(int color) {
                updateSelectedColor(color);

                // TODO: Send data
                // A flow control is needed in order to avoid
                // a serial buffer overflow in the Arduino side
            }

            @Override
            public void onStopTrackingTouch(int color) {
                updateSelectedColor(color);

                // if UsbService was correctly binded, Send data
                if (mUsbService != null) {
                    LedRGB message = new LedRGB(color);
                    Gson gson = new Gson();
                    mUsbService.write(gson.toJson(message).getBytes());
                    Log.d(TAG, "JSON: " + gson.toJson(message));
                }
            }
        });
    }

    private void updateSelectedColor(int color) {
        mColorSectedView.setBackgroundColor(color);
        mColorSelectedRed.setText(Html.fromHtml("R: <b>" + Color.red(color) + "</b>"));
        mColorSelectedGreen.setText(Html.fromHtml("G: <b>" + Color.green(color) + "</b>"));
        mColorSelectedBlue.setText(Html.fromHtml("B: <b>" + Color.blue(color) + "</b>"));
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
        private final WeakReference<RGBActivity> mActivity;

        public MyHandler(RGBActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:

                    if (msg.obj == null) return;

                    NanoPlayBoardMessage message = (NanoPlayBoardMessage) msg.obj;

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
