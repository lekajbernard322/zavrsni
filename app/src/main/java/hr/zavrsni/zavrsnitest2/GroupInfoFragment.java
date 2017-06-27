package hr.zavrsni.zavrsnitest2;

import android.app.Fragment;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Prikazuje informacije o trenutnoj P2P grupi.
 */
public class GroupInfoFragment extends Fragment implements
        Clearable,
        WifiP2pManager.ConnectionInfoListener,
        WifiP2pManager.GroupInfoListener{
    private View contentView = null;
    private WifiP2pGroup wifiP2pGroup = null;
    private WifiP2pInfo wifiP2pInfo = null;

    public static GroupInfoFragment newInstance(WifiP2pGroup wifiP2pGroup, WifiP2pInfo wifiP2pInfo) {
        
        Bundle args = new Bundle();
        args.putParcelable(WifiP2pManager.EXTRA_WIFI_P2P_GROUP, wifiP2pGroup);
        args.putParcelable(WifiP2pManager.EXTRA_WIFI_P2P_INFO, wifiP2pInfo);
        
        GroupInfoFragment fragment = new GroupInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        wifiP2pGroup = bundle.getParcelable(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
        wifiP2pInfo = bundle.getParcelable(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.group_info, null);
        updateThisGroup();
        return contentView;
    }

    public void updateThisGroup() {
        if (wifiP2pGroup == null || wifiP2pInfo == null) {
            Log.d(MainActivity.TAG, "[!] Group or connection info is null.");
            clear();
            return;
        }
        Log.d(MainActivity.TAG, "Updating this group views...");

        Button b = (Button) contentView.findViewById(R.id.group_info_button_group);
        b.setText(R.string.group_info_remove_group);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).removeGroup();
            }
        });

        Button startChatButton = (Button) contentView.findViewById(R.id.group_info_button_chat);
        startChatButton.setEnabled(true);
        startChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(
                        R.id.main_fragment,
                        ChatFragment.newInstance(
                                wifiP2pInfo.isGroupOwner,
                                wifiP2pInfo.groupOwnerAddress.getHostAddress(),
                                wifiP2pGroup.getOwner().deviceAddress))
                        .commit();
            }
        });

        String deviceOwnerString = String.format(
                getResources().getString(R.string.group_info_group_owner_device),
                    (wifiP2pGroup.getOwner().deviceName
                            + " (" + wifiP2pGroup.getOwner().deviceAddress + ")"));
        String groupOwnerAddressString = String.format(
                getResources().getString(R.string.group_info_group_owner_address),
                wifiP2pInfo.groupOwnerAddress);
        String isGroupOwnerString = String.format(
                getResources().getString(R.string.group_info_is_group_owner),
                wifiP2pInfo.isGroupOwner);
        String isGroupFormedString = String.format(
                getResources().getString(R.string.group_info_is_group_formed),
                wifiP2pInfo.groupFormed);
        String interfaceString = String.format(
                getResources().getString(R.string.group_info_interface),
                wifiP2pGroup.getInterface());
        String networkNameString = String.format(
                getResources().getString(R.string.group_info_network_name),
                wifiP2pGroup.getNetworkName());
        String passphraseString = String.format(
                getResources().getString(R.string.group_info_passphrase),
                wifiP2pGroup.getPassphrase());

        TextView deviceOwnerTextView = (TextView)
                contentView.findViewById(R.id.group_info_group_owner_device);
        TextView groupOwnerAddressTextView = (TextView)
                contentView.findViewById(R.id.group_info_group_owner_address);
        TextView isGroupOwnerTextView = (TextView)
                contentView.findViewById(R.id.group_info_is_group_owner);
        TextView isGroupFormedTextView = (TextView)
                contentView.findViewById(R.id.group_info_is_group_formed);
        TextView interfaceTextView = (TextView)
                contentView.findViewById(R.id.group_info_interface);
        TextView networkNameTextView = (TextView)
                contentView.findViewById(R.id.group_info_network_name);
        TextView passphraseTextView = (TextView)
                contentView.findViewById(R.id.group_info_passphrase);

        ListView clientList = (ListView) contentView.findViewById(R.id.group_info_client_list);
        clientList.setAdapter(new DeviceListAdapter(
                getActivity(),
                R.layout.device_list_row,
                new ArrayList<>(wifiP2pGroup.getClientList()),
                null));

        deviceOwnerTextView.setText(deviceOwnerString);
        groupOwnerAddressTextView.setText(groupOwnerAddressString);
        isGroupOwnerTextView.setText(isGroupOwnerString);
        isGroupFormedTextView.setText(isGroupFormedString);
        interfaceTextView.setText(interfaceString);
        networkNameTextView.setText(networkNameString);
        passphraseTextView.setText(passphraseString);
    }

    @Override
    public void clear() {
        Log.d(MainActivity.TAG, "Clearing this group views...");

        Button b = (Button) contentView.findViewById(R.id.group_info_button_group);
        b.setText(R.string.group_info_create_group);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).initiateGroup();
            }
        });

        Button startChatButton = (Button) contentView.findViewById(R.id.group_info_button_chat);
        startChatButton.setEnabled(false);

        String deviceOwnerString = String.format(
                getResources().getString(R.string.group_info_group_owner_device), " - ");
        String groupOwnerAddressString = String.format(
                getResources().getString(R.string.group_info_group_owner_address), " - ");
        String isGroupOwnerString = String.format(
                getResources().getString(R.string.group_info_is_group_owner), false);
        String isGroupFormedString = String.format(
                getResources().getString(R.string.group_info_is_group_formed), false);
        String interfaceString = String.format(
                getResources().getString(R.string.group_info_interface), " - ");
        String networkNameString = String.format(
                getResources().getString(R.string.group_info_network_name), " - ");
        String passphraseString = String.format(
                getResources().getString(R.string.group_info_passphrase), " - ");

        TextView deviceOwnerTextView = (TextView)
                contentView.findViewById(R.id.group_info_group_owner_device);
        TextView groupOwnerAddressTextView = (TextView)
                contentView.findViewById(R.id.group_info_group_owner_address);
        TextView isGroupOwnerTextView = (TextView)
                contentView.findViewById(R.id.group_info_is_group_owner);
        TextView isGroupFormedTextView = (TextView)
                contentView.findViewById(R.id.group_info_is_group_formed);
        TextView interfaceTextView = (TextView)
                contentView.findViewById(R.id.group_info_interface);
        TextView networkNameTextView = (TextView)
                contentView.findViewById(R.id.group_info_network_name);
        TextView passphraseTextView = (TextView)
                contentView.findViewById(R.id.group_info_passphrase);

        deviceOwnerTextView.setText(deviceOwnerString);
        groupOwnerAddressTextView.setText(groupOwnerAddressString);
        isGroupOwnerTextView.setText(isGroupOwnerString);
        isGroupFormedTextView.setText(isGroupFormedString);
        interfaceTextView.setText(interfaceString);
        networkNameTextView.setText(networkNameString);
        passphraseTextView.setText(passphraseString);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        this.wifiP2pInfo = wifiP2pInfo;
        updateThisGroup();
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
        this.wifiP2pGroup = wifiP2pGroup;
        updateThisGroup();
    }
}
