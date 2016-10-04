package org.josejuansanchez.nanoplayboard.events;

/**
 * Created by josejuansanchez on 04/10/16.
 */

public class UsbSerialStringEvent {
    public final String data;

    public UsbSerialStringEvent(String data) {
        this.data = data;
    }
}