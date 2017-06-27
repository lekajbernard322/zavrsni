package hr.zavrsni.zavrsnitest2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.support.design.widget.Snackbar;
import android.util.Log;

import hr.zavrsni.zavrsnitest2.utils.WfdUtils;

/**
 * Prima Intent akcije:
 *      WIFI_P2P_STATE_CHANGED_ACTION
 *          -da li je Wi-Fi P2P uključen ili isključen
 *      WIFI_P2P_PEERS_CHANGED_ACTION
 *          -lista dostupnih uređaja se promjenila
 *      WIFI_P2P_CONNECTION_CHANGED_ACTION
 *          -stanje Wi-Fi P2P veze je promjenjeno
 *      WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
 *          -detalji ovog uređaja su promjenjeni
 *      WIFI_P2P_DISCOVERY_CHANGED_ACTION
 *          -da li je otkrivanje započeto ili zaustavljeno
 */
public class WFDReciever extends BroadcastReceiver {
    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;

    public WFDReciever(WifiP2pManager m, Channel c, MainActivity a) {
        manager = m;
        channel = c;
        activity = a;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.d(MainActivity.TAG, "WIFI_P2P_STATE_CHANGED_ACTION - " + WfdUtils.getWiFiStatus(state));

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setIsWifiEnabled(true);
            } else {
                activity.setIsWifiEnabled(false);
                activity.clear();
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(MainActivity.TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");

            if (manager == null)
                return;

            manager.requestPeers(channel, activity);

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(MainActivity.TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");

            if (manager == null) {
                return;
            }

            NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (netInfo.isConnected()) {
                Log.d(MainActivity.TAG, "Connected to device...");
                Log.d(MainActivity.TAG, "Requesting connection and group info...");
                manager.requestConnectionInfo(channel, activity);
                manager.requestGroupInfo(channel, activity);
            } else {
                Log.d(MainActivity.TAG, "Connection failed...");
                activity.clear();
            }

            if (netInfo.getState().equals("DISCONNECTED")) {
                Snackbar.make(
                        activity.findViewById(android.R.id.content),
                        "Disconnected from device...",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(MainActivity.TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");

            WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            activity.updateThisDevice(thisDevice);
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            Log.d(MainActivity.TAG, WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

            int discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 0);

            if (discoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                Log.d(MainActivity.TAG, "WIFI_P2P_DISCOVERY_STARTED");
                activity.setIsDiscoveryEnabled(true);
            } else {
                Log.d(MainActivity.TAG, "WIFI_P2P_DISCOVERY_STOPPED");
                activity.setIsDiscoveryEnabled(false);
            }
        }
    }
}
