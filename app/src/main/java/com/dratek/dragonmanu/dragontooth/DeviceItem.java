package com.dratek.dragonmanu.dragontooth;


import android.bluetooth.BluetoothDevice;

public class DeviceItem {

    private String deviceName;
    private String address;
    private int rssi;
    private boolean connected;
    private BluetoothDevice device;

    public String getDeviceName() {
        return deviceName;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public boolean getConnected() {
        return connected;
    }

    public int getrssi() {return rssi; }

    public void setrssi(int set) {rssi = set; }

    public String getAddress() {
        return address;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceItem(String name, BluetoothDevice device, String address, int rssi, String connected){
        this.deviceName = name;
        this.device = device;
        this.address = address;
        this.rssi = rssi;
        if (connected == "true") {
            this.connected = true;
        }
        else {
            this.connected = false;
        }
    }
}
