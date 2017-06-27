package hr.zavrsni.zavrsnitest2.utils;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

public class WfdUtils {

    public static String getOnFailureReason(int reason) {

        switch (reason) {
            case WifiP2pManager.ERROR:
                return "Error";
            case WifiP2pManager.P2P_UNSUPPORTED:
                return "P2P unsupported";
            case WifiP2pManager.BUSY:
                return "Busy";
            case WifiP2pManager.NO_SERVICE_REQUESTS:
                return "No service requests";
            default:
                return "Undefined";
        }

    }

    public static String getDeviceStatus(int status) {

        switch (status) {
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Undefined";
        }

    }

    public static String getWiFiStatus(int status) {

        switch (status) {
            case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                return "Wifi enabled";
            case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                return "Wifi disabled";
            default:
                return "Undefined";
        }

    }

}
