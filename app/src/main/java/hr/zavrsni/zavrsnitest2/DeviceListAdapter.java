package hr.zavrsni.zavrsnitest2;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hr.zavrsni.zavrsnitest2.utils.WfdUtils;

class DeviceListAdapter extends ArrayAdapter<WifiP2pDevice> {
    private List<WifiP2pDevice> devices;
    private DeviceListListener listener;

    interface DeviceListListener {
        void onItemClick(WifiP2pDevice device);
    }

    DeviceListAdapter(@NonNull Context context, @LayoutRes int resource,
                             List<WifiP2pDevice> devices, DeviceListListener listener) {
        super(context, resource);
        this.devices = devices;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.device_list_row, null);
        }

        final WifiP2pDevice device = devices.get(position);
        if(device == null) {
            return v;
        }

        if (listener != null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(device);
                }
            });
        }

        TextView name = (TextView) v.findViewById(R.id.row_device_name);
        TextView address = (TextView) v.findViewById(R.id.row_device_address);
        TextView status = (TextView) v.findViewById(R.id.row_device_status);

        String addressText = String.format(
                getContext().getResources().getString(R.string.device_info_device_address),
                device.deviceAddress);
        String nameText = String.format(
                getContext().getResources().getString(R.string.device_info_device_name),
                device.deviceName);
        String statusText = String.format(
                getContext().getResources().getString(R.string.device_info_device_status),
                WfdUtils.getDeviceStatus(device.status));

        if (name != null) name.setText(nameText);
        if (address != null) address.setText(addressText);
        if (status != null) status.setText(statusText);

        return v;
    }
}
