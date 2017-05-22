/*
Main Activity
 */
package com.dratek.dragonmanu.dragontooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import java.util.List;
import java.util.UUID;

public class MainActivity extends ActionBarActivity implements DeviceListFragment.OnFragmentInteractionListener {

    private DeviceListFragment mDeviceListFragment;
    private BluetoothAdapter BTAdapter;
    private BluetoothGatt mGatt;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;

    static boolean connected=false;
    public static int REQUEST_BLUETOOTH = 1;
    public static String message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check support of BLE communications
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Sorry,This device does not support BLE communication", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Request Bluetooth to turn on
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BTAdapter = bluetoothManager.getAdapter();

        //Request Location to turn on
        this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 11); //Any number

        // Phone does not support Bluetooth so let the user know and exit.
        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = BTAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        mDeviceListFragment = DeviceListFragment.newInstance(BTAdapter);
        fragmentManager.beginTransaction().replace(R.id.container, mDeviceListFragment).commit();
    }

    //When user clicks on a device item in the list
    @Override
    public void onFragmentInteraction(BluetoothDevice device) {
        Log.d("DEVICELIST", "connecting");
        connectToDevice(device);
    }

    public void connectToDevice(BluetoothDevice device) {
        message = null;
        if (!connected) {
            mGatt = device.connectGatt(this, false, gattCallback);
            connected = true;
            DeviceListFragment.isChecked = false; // scanning will stop after device is connected
        }else{
            mGatt.disconnect();
            connected = false;
            Log.d("DEVICELIST", "removing");
            DeviceListFragment.isChecked = true; // scanning will start after disconnect
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            //Log.i("onServicesDiscovered", services.toString());
            Log.i("onServicesDiscovered", services.get(3).getCharacteristics().get(0).getUuid().toString());
            BluetoothGattCharacteristic commandChar = services.get(3).getCharacteristics().get(0);

            //data to be sent
            String data = "hello dragon\n";

            byte[] byteArray = data.getBytes();
            commandChar.setValue(byteArray);
            gatt.writeCharacteristic(commandChar);

            //Receive updates if message is changed on BLE device
            gatt.setCharacteristicNotification(services.get(3).getCharacteristics().get(0),true);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            if (characteristic.getValue()!= null && characteristic.getValue().length >0) {
                Log.d("DEVICELIST", "reading"+ new String(characteristic.getValue())+ characteristic.getValue().length);
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i("onCharacteristicChange", characteristic.toString());
            if (characteristic.getValue()!= null && characteristic.getValue().length >0) {
                Log.d("DEVICELIST", "read from BLE : "+ new String(characteristic.getValue())); // currently outputs the message read on the logcat
                message = new String(characteristic.getValue());

                /*
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        MainActivity.this);
                alertDialog.setMessage(new String(characteristic.getValue()));
                alertDialog.setTitle("Message received");
                alertDialog.show();
                */
            }
        }
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status != BluetoothGatt.GATT_SUCCESS){
                Log.d("onCharacteristicWrite", "Failed write, retrying");
                gatt.writeCharacteristic(characteristic);
            }
            Log.d("onCharacteristicWrite", new String(characteristic.getValue()));
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        /*
        public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                                  boolean enabled) {
            if (BTAdapter == null || mGatt == null) {
                Log.d("warning", "BluetoothAdapter not initialized");
                return;
            }
            mGatt.setCharacteristicNotification(characteristic, enabled);

            // This is specific to Heart Rate Measurement.
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(descriptor);

        }
        */

    };


    //prompt the user on exiting application
    private void doExit() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainActivity.this);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mGatt.disconnect();
                mGatt.close();
                MainActivity.super.onBackPressed();
            }
        });

        alertDialog.setNegativeButton("No", null);

        alertDialog.setMessage("Do you really want to exit?");
        alertDialog.setTitle("DragonTooth");
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        doExit();
    }
}
