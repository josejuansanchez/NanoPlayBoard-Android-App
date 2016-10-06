package org.josejuansanchez.nanoplayboard.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

/**
 * Created by josejuansanchez on 05/10/16.
 */

public class Utils {
    private Context mContext;

    public Utils(Context mContext) {
        this.mContext = mContext;
    }

    public String getIpAddress() {
        WifiManager wm = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }
}
