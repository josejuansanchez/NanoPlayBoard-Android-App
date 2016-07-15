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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.models.LedMatrix;
import org.josejuansanchez.nanoplayboard.services.UsbService;

import java.lang.ref.WeakReference;
import java.util.Set;

public class LedMatrixPatternActivity extends AppCompatActivity {

    public static final String TAG = LedMatrixActivity.class.getSimpleName();
    private UsbService mUsbService;
    private MyHandler mHandler;
    private EditText mText;
    private Button mButtonSend;
    private GridView mGridView;

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
        setContentView(R.layout.activity_led_matrix_pattern);
        mHandler = new MyHandler(this);
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setAdapter(new ImageAdapter(this));
        loadListeners();
    }

    private void loadListeners() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                ImageView imageView = (ImageView) v;

                if (imageView.getTag().toString().equals("off")) {
                    imageView.setImageResource(R.drawable.on);
                    imageView.setTag("on");
                } else {
                    imageView.setImageResource(R.drawable.off);
                    imageView.setTag("off");
                }

                // TODO: Send JSON message
                //sendJsonMessage
            }
        });
    }

    private void sendJsonMessage(String pattern) {
        // if UsbService was correctly binded, Send data
        if (mUsbService != null) {
            LedMatrix message = new LedMatrix(5, pattern);
            Gson gson = new Gson();
            mUsbService.write(gson.toJson(message).getBytes());
            mUsbService.write("\n".getBytes());
            Log.d(TAG, "JSON: " + gson.toJson(message));
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
        private final WeakReference<LedMatrixPatternActivity> mActivity;

        public MyHandler(LedMatrixPatternActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    if (msg.obj == null) return;
                    //NanoPlayBoardMessage message = (NanoPlayBoardMessage) msg.obj;
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

    private class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);
            imageView.setTag("off");
            return imageView;
        }

        // references to our images
        private Integer[] mThumbIds = {
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off, R.drawable.off,
                R.drawable.off
        };
    }
}
