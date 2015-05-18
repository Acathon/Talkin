package indie.pfe.talkin;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

/**
 * Created by Mustapha Essouri on 02/05/2015.
 */
public class HotspotController {
    public static boolean isApOn(Context context) {
        WifiManager mWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = mWifi.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifi);

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean configApState(Context context) {
        WifiManager mWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wConf = null;
        try {
            if (isApOn(context)) {
                mWifi.setWifiEnabled(false);
            }
            Method method = mWifi.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(mWifi, wConf, !isApOn(context));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
