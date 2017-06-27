package hr.zavrsni.zavrsnitest2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import hr.zavrsni.zavrsnitest2.utils.WfdUtils;

public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        WifiP2pManager.ChannelListener,
        WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener,
        WifiP2pManager.GroupInfoListener,
        Clearable {
    public static final String TAG = "WFDirect";

    private boolean isWifiEnabled = false;
    private boolean isDiscoveryEnabled = false;

    private WifiP2pInfo wifiP2pInfo;
    private WifiP2pGroup wifiP2pGroup;
    private WifiP2pDeviceList wifiP2pDeviceList;
    private WifiP2pDevice thisDevice;

    private WifiP2pManager manager;
    private Channel channel;
    private IntentFilter intentFilter;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction().add(
                R.id.main_fragment,
                DeviceListFragment.newInstance(null)).commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), this);
        receiver = new WFDReciever(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_discover) {
            if (isDiscoveryEnabled) {
                stopDiscovery();
            } else {
                initiateDiscovery();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isDiscoveryEnabled)
            menu.findItem(R.id.action_discover).getIcon().setTint(getResources().
                    getColor(R.color.colorAccent));
        else
            menu.findItem(R.id.action_discover).getIcon().setTint(getResources().
                    getColor(android.R.color.white));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment f;

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        if (id == R.id.nav_discover) {
            getSupportActionBar().setTitle(getResources().getString(R.string.navigation_discover));
            f = DeviceListFragment.newInstance(wifiP2pDeviceList);
        } else if (id == R.id.nav_group_info) {
            getSupportActionBar().setTitle(getResources().getString(R.string.navigation_group_info));
            f = GroupInfoFragment.newInstance(wifiP2pGroup, wifiP2pInfo);
        } else if (id == R.id.nav_device_info) {
            getSupportActionBar().setTitle(getResources().getString(R.string.navigation_device_info));
            f = DeviceInfoFragment.newInstance(thisDevice);
        } else if (id == R.id.nav_settings) {
            getSupportActionBar().setTitle(getResources().getString(R.string.navigation_settings));
            f = SettingsFragment.newInstance();
        } else if (id == R.id.nav_about) {
            getSupportActionBar().setTitle(getResources().getString(R.string.navigation_about));
            f = AboutFragment.newInstance();
        } else {
            f = DeviceListFragment.newInstance(null);
        }

        transaction.replace(R.id.main_fragment, f).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void initiateDiscovery() {
        if (isDiscoveryEnabled) {
            Log.d(TAG, "[*] Discovery already enabled...");
            return;
        }

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "[*] Peer discovery initiated...");
                setIsDiscoveryEnabled(true);
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "[!] Peer discovery failed. Reason: "
                        + WfdUtils.getOnFailureReason(i));
            }
        });
    }

    public void stopDiscovery() {
        manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "[*] Peer discovery stopped...");
                setIsDiscoveryEnabled(false);
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "[!] Failed to stop peer discovery. Reason: "
                        + WfdUtils.getOnFailureReason(i));
            }
        });
    }

    public void connectToDevice(final WifiP2pDevice device) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "[*] Connection to device (" + device.deviceName + ") initiated...");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "[!] Connection to device (" + device.deviceName
                        + ") failed. Reason: " + WfdUtils.getOnFailureReason(i));
            }
        });

        Snackbar.make(
            findViewById(R.id.main_fragment),
            "Connecting...",
            Snackbar.LENGTH_LONG)
            .setAction(
                getResources().getString(R.string.action_stop_connect),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancelConnect();
                    }
                }
            )
            .show();
    }

    public void cancelConnect() {
        manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "[*] Connection to device canceled...");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "[!] Failed to cancel connection attempt. Reason: "
                        + WfdUtils.getOnFailureReason(i));
            }
        });
    }

    public void initiateGroup() {
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "[*] Group creation initiated...");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "[!] Group creation failed. Reason: "
                        + WfdUtils.getOnFailureReason(i));
            }
        });
    }

    public void removeGroup() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "[*] Group removal initiated...");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "[!] Failed to remove group. Reason: "
                        + WfdUtils.getOnFailureReason(i));
            }
        });
    }

    public void setIsWifiEnabled(boolean b) {
        isWifiEnabled = b;
    }

    public boolean isWifiEnabled() {
        return isWifiEnabled;
    }

    public void setIsDiscoveryEnabled(boolean b) {
        isDiscoveryEnabled = b;
        invalidateOptionsMenu();

        if (getFragmentManager().findFragmentById(R.id.main_fragment) instanceof DeviceListFragment) {
            DeviceListFragment f = (DeviceListFragment)
                getFragmentManager().findFragmentById(R.id.main_fragment);

            f.updateEmptyViewText(isDiscoveryEnabled);

            if (!isDiscoveryEnabled)
                f.clear();
        }
    }

    public boolean isDiscoveryEnabled() {
        return isDiscoveryEnabled;
    }

    @Override
    public void onChannelDisconnected() {
        Log.d(TAG, "[!] onChannelDisconnected");
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        this.wifiP2pDeviceList = wifiP2pDeviceList;

        Log.d(TAG, "[*] Found " + wifiP2pDeviceList.getDeviceList().size() + " device/s...");

        if (getFragmentManager().findFragmentById(R.id.main_fragment) instanceof WifiP2pManager.PeerListListener) {
            WifiP2pManager.PeerListListener f = (WifiP2pManager.PeerListListener)
                getFragmentManager().findFragmentById(R.id.main_fragment);

            f.onPeersAvailable(wifiP2pDeviceList);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        Log.d(TAG, "[*] Received connection info... ");
        this.wifiP2pInfo = wifiP2pInfo;

        if (getFragmentManager().findFragmentById(R.id.main_fragment) instanceof WifiP2pManager.ConnectionInfoListener) {
            WifiP2pManager.ConnectionInfoListener f = (WifiP2pManager.ConnectionInfoListener)
                getFragmentManager().findFragmentById(R.id.main_fragment);

            f.onConnectionInfoAvailable(wifiP2pInfo);
        }
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
        Log.d(TAG, "[*] Received group info...");
        this.wifiP2pGroup = wifiP2pGroup;

        if (getFragmentManager().findFragmentById(R.id.main_fragment) instanceof WifiP2pManager.GroupInfoListener) {
            WifiP2pManager.GroupInfoListener f = (WifiP2pManager.GroupInfoListener)
                getFragmentManager().findFragmentById(R.id.main_fragment);

            f.onGroupInfoAvailable(wifiP2pGroup);
        }
    }

    public void updateThisDevice(WifiP2pDevice thisDevice) {
        Log.d(TAG, "[*] Received this device info...");
        this.thisDevice = thisDevice;

        if (getFragmentManager().findFragmentById(R.id.main_fragment) instanceof DeviceInfoFragment) {
            DeviceInfoFragment f = (DeviceInfoFragment)
                getFragmentManager().findFragmentById(R.id.main_fragment);

            f.thisDevice = thisDevice;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void clear() {
        Log.d(TAG, "[*] Clearing data...");

        this.wifiP2pDeviceList = null;
        this.wifiP2pGroup = null;
        this.wifiP2pInfo = null;

        if (getFragmentManager().findFragmentById(R.id.main_fragment) instanceof Clearable) {
            ((Clearable)getFragmentManager().findFragmentById(R.id.main_fragment)).clear();
        }
    }
}
