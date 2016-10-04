package org.josejuansanchez.nanoplayboard.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;
import org.josejuansanchez.nanoplayboard.events.UsbSerialActionEvent;
import org.josejuansanchez.nanoplayboard.events.UsbSerialMessageEvent;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

// This code is based on the example availabe in the UsbSerial repository:
// https://github.com/felHR85/UsbSerial

public class UsbService extends Service {

    public static final String TAG = UsbService.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    public static final String ACTION_USB_READY = "org.josejuansanchez.nanoplayboard.USB_READY";
    public static final String ACTION_USB_NOT_SUPPORTED = "org.josejuansanchez.nanoplayboard.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "org.josejuansanchez.nanoplayboard.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "org.josejuansanchez.nanoplayboard.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "org.josejuansanchez.nanoplayboard.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED = "org.josejuansanchez.nanoplayboard.USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "org.josejuansanchez.nanoplayboard.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "org.josejuansanchez.nanoplayboard.ACTION_USB_DEVICE_NOT_WORKING";

    public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    public static final int CTS_CHANGE = 1;
    public static final int DSR_CHANGE = 2;

    public static boolean SERVICE_CONNECTED = false;
    private static final int BAUD_RATE = 115200;

    private IBinder binder = new UsbBinder();
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;

    private boolean serialPortConnected;

    /*
     *  Data received from serial port will be received here. Just populate onReceivedData with your code
     *  In this particular example. byte stream is converted to String and send to UI thread to
     *  be treated there.
     */
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                String data = new String(arg0, "UTF-8");
                Log.d(TAG, "Response: " + data);

                // Convert json to a NanoPlayBoardMessage object
                Gson gson = new Gson();
                final NanoPlayBoardMessage message = gson.fromJson(data, NanoPlayBoardMessage.class);
                EventBus.getDefault().post(new UsbSerialMessageEvent(MESSAGE_FROM_SERIAL_PORT, message));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JsonSyntaxException jse) {
                jse.printStackTrace();
            }
        }
    };

    /*
     * State changes in the CTS line will be received here
     */
    private UsbSerialInterface.UsbCTSCallback ctsCallback = new UsbSerialInterface.UsbCTSCallback() {
        @Override
        public void onCTSChanged(boolean state) {
            EventBus.getDefault().post(new UsbSerialMessageEvent(CTS_CHANGE));
        }
    };

    /*
     * State changes in the DSR line will be received here
     */
    private UsbSerialInterface.UsbDSRCallback dsrCallback = new UsbSerialInterface.UsbDSRCallback() {
        @Override
        public void onDSRChanged(boolean state) {
            EventBus.getDefault().post(new UsbSerialMessageEvent(DSR_CHANGE));
        }
    };

    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) // User accepted our USB connection. Try to open the device as a serial port
                {
                    EventBus.getDefault().post(new UsbSerialActionEvent(ACTION_USB_PERMISSION_GRANTED));
                    connection = usbManager.openDevice(device);
                    new ConnectionThread().run();
                } else
                {
                    EventBus.getDefault().post(new UsbSerialActionEvent(ACTION_USB_PERMISSION_NOT_GRANTED));
                }
            } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                if (!serialPortConnected)
                    findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                // Usb device was disconnected
                EventBus.getDefault().post(new UsbSerialActionEvent(ACTION_USB_DISCONNECTED));
                serialPortConnected = false;
                if (serialPortConnected) {
                    serialPort.close();
                }
            }
        }
    };

    /*
     * onCreate will be executed when service is started. It configures an IntentFilter to listen for
     * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
     */
    @Override
    public void onCreate() {
        serialPortConnected = false;
        UsbService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        findSerialPortDevice();
    }

    /* MUST READ about services
     * http://developer.android.com/guide/components/services.html
     * http://developer.android.com/guide/components/bound-services.html
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UsbService.SERVICE_CONNECTED = false;
    }

    /*
     * This function will be called from Activities to write data through Serial Port
     */
    public void write(byte[] data) {
        if (serialPort != null)
            serialPort.write(data);
    }

    private void findSerialPortDevice() {
        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();

                if (deviceVID != 0x1d6b && (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003)) {
                    // There is a device connected to our Android device. Try to open it as a Serial Port.
                    requestUserPermission();
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
            if (!keep) {
                // There is no USB devices connected (but usb host were listed)
                EventBus.getDefault().post(new UsbSerialActionEvent(ACTION_NO_USB));
            }
        } else {
            // There is no USB devices connected
            EventBus.getDefault().post(new UsbSerialActionEvent(ACTION_NO_USB));
        }
    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(device, mPendingIntent);
    }

    public class UsbBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }
    }

    /*
     * A simple thread to open a serial port.
     * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serialPort != null) {
                if (serialPort.open()) {
                    serialPortConnected = true;
                    serialPort.setBaudRate(BAUD_RATE);
                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                    /**
                     * Current flow control Options:
                     * UsbSerialInterface.FLOW_CONTROL_OFF
                     * UsbSerialInterface.FLOW_CONTROL_RTS_CTS only for CP2102 and FT232
                     * UsbSerialInterface.FLOW_CONTROL_DSR_DTR only for CP2102 and FT232
                     */
                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    serialPort.read(mCallback);
                    serialPort.getCTS(ctsCallback);
                    serialPort.getDSR(dsrCallback);
                    // Everything went as expected
                    EventBus.getDefault().post(new UsbSerialActionEvent(ACTION_USB_READY));
                } else {
                    // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
                    if (serialPort instanceof CDCSerialDevice) {
                        EventBus.getDefault().post(new UsbSerialActionEvent(ACTION_CDC_DRIVER_NOT_WORKING));
                    } else {
                        EventBus.getDefault().post(new UsbSerialActionEvent(ACTION_USB_DEVICE_NOT_WORKING));
                    }
                }
            } else {
                // No driver for given device, even generic CDC driver could not be loaded
                EventBus.getDefault().post(new UsbSerialActionEvent(ACTION_USB_NOT_SUPPORTED));
            }
        }
    }
}