package hr.zavrsni.zavrsnitest2;

import android.app.Fragment;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Prikazuje informacije o trenutno uređaju.
 */
import hr.zavrsni.zavrsnitest2.utils.WfdUtils;

public class DeviceInfoFragment extends Fragment {
    View contentView = null;
    WifiP2pDevice thisDevice = null;

    public static DeviceInfoFragment newInstance(WifiP2pDevice thisDevice) {
        Bundle args = new Bundle();
        args.putParcelable(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, thisDevice);

        DeviceInfoFragment fragment = new DeviceInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisDevice = getArguments().getParcelable(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.device_info, null);
        updateThisDevice();
        return contentView;
    }

    public void updateThisDevice() {
        if (thisDevice == null) {
            Log.d(MainActivity.TAG, "[!] This device is null.");
            return;
        }
        Log.d(MainActivity.TAG, "Updating this device views...");

        String deviceAddressString = String.format(
                getResources().getString(R.string.device_info_device_address),
                thisDevice.deviceAddress);
        String deviceNameString = String.format(
                getResources().getString(R.string.device_info_device_name),
                thisDevice.deviceName);
        String deviceStatusString = String.format(
                getResources().getString(R.string.device_info_device_status),
                "" + WfdUtils.getDeviceStatus(thisDevice.status));
        String primaryTypeString = String.format(
                getResources().getString(R.string.device_info_primary_type),
                thisDevice.primaryDeviceType);
        String secondaryTypeString = String.format(
                getResources().getString(R.string.device_info_secondary_type),
                thisDevice.secondaryDeviceType);
        String isGOString = String.format(
                getResources().getString(R.string.device_info_is_group_owner),
                thisDevice.isGroupOwner());
        String isServiceDiscoveryString = String.format(
                getResources().getString(R.string.device_info_is_service_discovery_capable),
                thisDevice.isServiceDiscoveryCapable());
        String wpsDisplayString = String.format(
                getResources().getString(R.string.device_info_wps_display_support),
                thisDevice.wpsDisplaySupported());
        String wpsKeypadString = String.format(
                getResources().getString(R.string.device_info_wps_keypad_support),
                thisDevice.wpsKeypadSupported());
        String wpsPbcString = String.format(
                getResources().getString(R.string.device_info_wps_pbc_support),
                thisDevice.wpsPbcSupported());

        TextView deviceAddressTextView = (TextView)
                contentView.findViewById(R.id.device_info_device_address);
        TextView deviceNameTextView = (TextView)
                contentView.findViewById(R.id.device_info_device_name);
        TextView deviceStatusTextView = (TextView)
                contentView.findViewById(R.id.device_info_device_status);
        TextView primaryTypeTextView = (TextView)
                contentView.findViewById(R.id.device_info_primary_type);
        TextView secondaryTypeTextView = (TextView)
                contentView.findViewById(R.id.device_info_secondary_type);
        TextView isGOTextView = (TextView)
                contentView.findViewById(R.id.device_info_is_group_owner);
        TextView isServiceDiscoveryTextView = (TextView)
                contentView.findViewById(R.id.device_info_is_service_discovery_capable);
        TextView wpsDisplayTextView = (TextView)
                contentView.findViewById(R.id.device_info_wps_display_support);
        TextView wpsKeypadTextView = (TextView)
                contentView.findViewById(R.id.device_info_wps_keypad_support);
        TextView wpsPbcTextView = (TextView)
                contentView.findViewById(R.id.device_info_wps_pbc_support);

        deviceAddressTextView.setText(deviceAddressString);
        deviceNameTextView.setText(deviceNameString);
        deviceStatusTextView.setText(deviceStatusString);
        primaryTypeTextView.setText(primaryTypeString);
        secondaryTypeTextView.setText(secondaryTypeString);
        isGOTextView.setText(isGOString);
        isServiceDiscoveryTextView.setText(isServiceDiscoveryString);
        wpsDisplayTextView.setText(wpsDisplayString);
        wpsKeypadTextView.setText(wpsKeypadString);
        wpsPbcTextView.setText(wpsPbcString);
    }
}
