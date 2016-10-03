package org.josejuansanchez.nanoplayboard.events;

/**
 * Created by josejuansanchez on 03/10/16.
 */

public class UsbSerialMessageEvent {
    public int what;
    public Object obj;

    public UsbSerialMessageEvent(int what) {
        this.what = what;
    }

    public UsbSerialMessageEvent(int what, Object obj) {
        this.what = what;
        this.obj = obj;
    }
}
