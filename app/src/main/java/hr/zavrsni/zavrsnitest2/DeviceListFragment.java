package hr.zavrsni.zavrsnitest2;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Prikazuje listu otkrivenih uređaja.
 */
public class DeviceListFragment extends ListFragment implements
        DeviceListAdapter.DeviceListListener,
        WifiP2pManager.PeerListListener,
        Clearable {
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private WifiP2pDeviceList wifiP2pDeviceList = null;

    public static DeviceListFragment newInstance(WifiP2pDeviceList wifiP2pDeviceList) {
        Bundle args = new Bundle();
        args.putParcelable(WifiP2pManager.EXTRA_P2P_DEVICE_LIST, wifiP2pDeviceList);
        
        DeviceListFragment fragment = new DeviceListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiP2pDeviceList = getArguments().getParcelable(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.device_list, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new DeviceListAdapter(getActivity(), R.layout.device_list_row, peers, this));
        updateEmptyViewText(((MainActivity)getActivity()).isDiscoveryEnabled());
        onPeersAvailable(wifiP2pDeviceList);
    }

    public void updateEmptyViewText(boolean isDiscovering) {
        ((TextView)this.getListView().getEmptyView()).setText(
                isDiscovering ?
                        R.string.device_list_empty_view_no_devices :
                        R.string.device_list_empty_view_no_discovery
        );
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        peers.clear();

        if (wifiP2pDeviceList != null)
            peers.addAll(wifiP2pDeviceList.getDeviceList());

        ((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onItemClick(WifiP2pDevice device) {
        createDialog(device).show();
    }

    // ne radi
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);

        Log.d(MainActivity.TAG, "[!] Device: " + device.deviceName + ", address: " + device.deviceAddress +
                ", types: " + device.primaryDeviceType + "(" + device.secondaryDeviceType + ")" +
                ", status: " + device.status);
    }

    public void clearPeers() {
        peers.clear();
        ((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public AlertDialog createDialog(final WifiP2pDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        switch (device.status) {
            case WifiP2pDevice.CONNECTED:
                builder.setMessage(R.string.device_list_fragment_dialog_text_disconnect)
                    .setPositiveButton(R.string.device_list_fragment_dialog_positive_disconnect,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity)getActivity()).removeGroup();
                            }
                        });
                break;
            case WifiP2pDevice.AVAILABLE:
                builder.setMessage(R.string.device_list_fragment_dialog_text_connect)
                    .setPositiveButton(R.string.device_list_fragment_dialog_positive_connect,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity)getActivity()).connectToDevice(device);
                            }
                        });
                break;
            case WifiP2pDevice.INVITED:
                builder.setMessage(R.string.device_list_fragment_dialog_text_cancel_connect)
                    .setPositiveButton(R.string.device_list_fragment_dialog_positive_cancel_connect,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity)getActivity()).cancelConnect();
                            }
                        });
                break;
        }

        builder.setNegativeButton(R.string.device_list_fragment_dialog_negative,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void clear() {
        clearPeers();
    }

}
