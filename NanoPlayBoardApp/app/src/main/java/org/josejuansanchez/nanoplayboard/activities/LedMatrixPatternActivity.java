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
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
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
    private TextView mPatternSelectedDec;
    private TextView mPatternSelectedHex;
    private Button mButtonClear;
    private GridView mGridView;
    private final int MATRIX_ROWS = 7;
    private final int MATRIX_COLS = 5;

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
        mPatternSelectedDec = (TextView) findViewById(R.id.pattern_selected_decimal);
        mPatternSelectedHex = (TextView) findViewById(R.id.pattern_selected_hexadecimal);
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setAdapter(new ImageAdapter(this));
        loadListeners();
    }

    private void loadListeners() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                // Update the thumbnail of the item (is on or off?)
                int thumbId = ((ImageAdapter)mGridView.getAdapter()).getItemThumbId(position);
                if (thumbId == R.drawable.led_off) {
                    ((ImageAdapter)mGridView.getAdapter()).setItemThumbId(position, R.drawable.led_on);
                } else {
                    ((ImageAdapter)mGridView.getAdapter()).setItemThumbId(position, R.drawable.led_off);
                }

                // Calculate the values of the matrix and send the JSON message
                boolean matrix[][] = createMatrixFromAdapter();
                int columns[] = convertColumnsMatrixToInt(matrix);
                sendJsonMessage(columns);
                updateTextViewPattern(columns);
            }
        });
    }

    // Create a matrix of booleans from the adapter used for the GridView
    private boolean [][] createMatrixFromAdapter() {
        ImageAdapter adapter = ((ImageAdapter) mGridView.getAdapter());
        boolean matrix[][] = new boolean[MATRIX_ROWS][MATRIX_COLS];
        for(int i = 0; i < MATRIX_ROWS; i++) {
            for(int j = 0; j < MATRIX_COLS; j++ ) {
                if (adapter.getItemThumbId((i * MATRIX_COLS) + j) == R.drawable.led_on) {
                    matrix[i][j] = true;
                } else {
                    matrix[i][j] = false;
                }
            }
        }
        return matrix;
    }

    // Convert each column of the matrix into a int value
    private int [] convertColumnsMatrixToInt(boolean matrix[][]) {
        int columnInt[] = new int[MATRIX_COLS];
        for(int j = 0; j < MATRIX_COLS; j++) {
            boolean columnBool[] = copyColumnMatrix(matrix, j);
            columnInt[j] = convertArrayOfBooleansToInt(columnBool);
        }
        return columnInt;
    }

    // Note that is necessary to add manually the last element of the array
    // because the led matrix has 7 rows but we need an array of 8 bits
    private boolean [] copyColumnMatrix(boolean matrix[][], int colIndex) {
        boolean column[] = new boolean[MATRIX_ROWS + 1];
        for(int i = 0; i < MATRIX_ROWS; i++) {
            column[i] = matrix[i][colIndex];
        }
        column[MATRIX_ROWS] = false;
        return column;
    }

    private int convertArrayOfBooleansToInt(boolean a[]) {
        int n = 0;
        int length = a.length;
        for (int i = 0; i < length; i++) {
            n = (n << 1) + (a[i] ? 1 : 0);
        }
        return n;
    }

    private void sendJsonMessage(int pattern[]) {
        // if UsbService was correctly binded, Send data
        if (mUsbService != null) {
            LedMatrix message = new LedMatrix(5, pattern);
            Gson gson = new Gson();
            mUsbService.write(gson.toJson(message).getBytes());
            mUsbService.write("\n".getBytes());
            Log.d(TAG, "JSON: " + gson.toJson(message));
        }
    }

    private void updateTextViewPattern(int[] pattern) {
        String DecText = patternToString(pattern, 10);
        String HexText = patternToString(pattern, 16);
        mPatternSelectedDec.setText(Html.fromHtml("Dec: <b>" + DecText + "</b>"));
        mPatternSelectedHex.setText(Html.fromHtml("Hex: <b>" + HexText + "</b>"));
    }

    private String patternToString(int pattern[], int base) {
        String text = "";
        int length = pattern.length;
        for(int i = 0; i < length; i++) {
            text += Integer.toString(pattern[i], base);
            if (i != length - 1) {
                text += ", ";
            }
        }
        return text;
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

        public void setItemThumbId(int position, int id) {
            mThumbIds[position] = id;
            notifyDataSetChanged();
        }

        public int getItemThumbId(int position) {
            return mThumbIds[position];
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
            return imageView;
        }

        private Integer[] mThumbIds = {
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off
        };
    }
}