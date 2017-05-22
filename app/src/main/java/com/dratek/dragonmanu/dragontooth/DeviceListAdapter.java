/*
Class which implements showing the list of devices on UI
*/

package com.dratek.dragonmanu.dragontooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<DeviceItem> {

    private Context context;
    private BluetoothAdapter bTAdapter;

    public DeviceListAdapter(Context context, List items, BluetoothAdapter bTAdapter) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.bTAdapter = bTAdapter;
        this.context = context;
    }

    private class ViewHolder{
        TextView titleText;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View line = null;
        DeviceItem item = (DeviceItem)getItem(position);
        //final String name = item.getDeviceName();
        TextView macAddress = null;
        TextView rssi = null;
        TextView message = null;
        View viewToUse = null;

        // This block exists to inflate the settings list item conditionally based on whether
        // we want to support a grid or list view.
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        viewToUse = mInflater.inflate(R.layout.device_list_item, null);
        holder = new ViewHolder();
        holder.titleText = (TextView)viewToUse.findViewById(R.id.titleTextView);
        viewToUse.setTag(holder);

        //linking objects to UI elements
        macAddress = (TextView)viewToUse.findViewById(R.id.macAddress);
        rssi = (TextView)viewToUse.findViewById(R.id.rssi_value);
        message = (TextView)viewToUse.findViewById(R.id.message);
        line = (View)viewToUse.findViewById(R.id.line);

        //Setting up the values to be displayed
        holder.titleText.setText(item.getDeviceName());
        macAddress.setText(item.getAddress());
        rssi.setText(String.valueOf(item.getrssi()));
        message.setText(MainActivity.message);

        //If no devices found, we make UI invisible
        if(item.getDeviceName()!=null){
            if ( item.getDeviceName().toString() == "No Devices") {
                macAddress.setVisibility(View.INVISIBLE);
                rssi.setVisibility(View.INVISIBLE);
                line.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                        ((int) RelativeLayout.LayoutParams.WRAP_CONTENT, (int) RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                holder.titleText.setLayoutParams(params);
            }
        }
        return viewToUse;
    }
}
