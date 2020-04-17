package com.hero.elias.conanapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothHandler implements Runnable {
    
    private static BluetoothHandler sSoleInstance;
    
    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    Log.i("BT", "Found New Device");
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    Log.i("BT", "Bond State Changed");
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.i("BT", "Device Connected");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.i("BT", "Device Dissconnected");
                    break;
                case BluetoothDevice.ACTION_UUID:
                    Log.i("BT", "Found UUID From : " + device.getName());
                    if (device.getAddress() == BluetoothHandler.getInstance().getMacAddress()){
                        UUID uuid = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
                        Log.i("BT", "UUID: " + uuid.toString());
                    }
                    break;
            }
        }
    };
    
    private Activity mainActivity;
    private Thread bluetoothThread;
    
    private String MAC_ADDRESS = "98:D3:34:90:6F:A1"; // INSERT MBOT MAC ADDRESS
    private UUID UUID_ADDRESS = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");  // INSERT CORRECT MBOT UUID ADDRESS
    
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    
    private  ArrayList<BluetoothCallback> bluetoothCallback;
    
    private  InputStream inputStream;
    private  OutputStream outputStream;
    private  BufferedReader bufferedReader;
    
    private  boolean threadRunning;
    private  boolean deviceConnected;
    private  boolean callbackConnected;
    
    private BluetoothHandler() {
        this.callbackConnected = false;
        this.deviceConnected = false;
        this.threadRunning = false;
        this.bluetoothThread = new Thread(this, "Bluetooth Thread");
        this.bluetoothCallback = new ArrayList<BluetoothCallback>();
    }
    
    public static BluetoothHandler getInstance() {
        if (sSoleInstance == null) {
            sSoleInstance = new BluetoothHandler();
        }
        return sSoleInstance;
    }
    
    public void setMainActivity(Activity mainActivity) {
        this.mainActivity = mainActivity;
        this.registerReceivers();
    }
    
    public String getMacAddress(){
        return this.MAC_ADDRESS;
    }
    
    private void discoverDevices() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        this.mainActivity.startActivity(discoverableIntent);
        this.bluetoothAdapter.startDiscovery();
    }
    
    private void init() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        while (this.bluetoothAdapter == null) {
            this.bluetoothEnableIntent();
            this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        
        this.discoverDevices();
        this.printBluetoothDevices();
    }
    
    @Override
    public void run() {
        this.init();
        //this.bluetoothAdapter.cancelDiscovery();
        this.connectToMbot();
        
        this.threadRunning = true;
        while (this.threadRunning) {
            if (this.deviceConnected) {
                this.checkConnection();
                try {
                    String text = this.bufferedReader.readLine(); // Reads a line of text, text ends at \n or \r
                    if (this.callbackConnected) {
                        this.executeCallback(text);
                    }
                } catch (IOException e) {
                    Log.e("BT", "Error:" + e.getMessage());
                }
            } else {
                this.connectToMbot();
            }
        }
        this.closeConnection();
    }
    
    private void executeCallback(String text) {
        for (int i = 0; i < this.bluetoothCallback.size(); i++) {
            this.bluetoothCallback.get(i).bluetoothMessage(text);
        }
    }
    
    private void checkConnection() {
        if (!this.bluetoothSocket.isConnected()) {
            this.closeConnection();
        }
    }
    
    public void stopThread() {
        this.threadRunning = false;
    }
    
    public void startThread() {
        if (!this.bluetoothThread.isAlive()) {
            this.bluetoothThread = new Thread(this, "Bluetooth Thread");
            this.bluetoothThread.setPriority(Thread.MIN_PRIORITY);
            this.bluetoothThread.start();
        }
    }
    
    public void write(byte[] bytes) {
        if (this.deviceConnected && this.threadRunning) {
            try {
                this.outputStream.write(bytes);
            } catch (IOException e) {
                try {
                    this.outputStream.flush();
                } catch (IOException ex) {
                    Log.e("BT", "Error:" + e.getMessage());
                }
                Log.e("BT", "Error:" + e.getMessage());
            }
        }
    }
    
    public void addCallback(BluetoothCallback callback) {
        this.bluetoothCallback.add(callback);
        this.callbackConnected = true;
    }
    
    public void removeCallback(BluetoothCallback callback) {
        this.bluetoothCallback.remove(callback);
        
        if (this.bluetoothCallback.size() == 0) {
            this.callbackConnected = false;
        }
    }
    
    private void closeConnection() {
        if (this.deviceConnected) {
            try {
                this.deviceConnected = false;
                this.bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("BT", "Error:" + e.getMessage());
            }
        }
    }
    
    private void connectToMbot() {
        if (this.bluetoothAdapter.isEnabled()) {
            this.bluetoothDevice = this.bluetoothAdapter.getRemoteDevice(this.MAC_ADDRESS);
            if (this.bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                try {
                    //this.bluetoothSocket = this.bluetoothDevice.createRfcommSocketToServiceRecord(UUID_ADDRESS);
                    this.bluetoothSocket = this.bluetoothDevice.createInsecureRfcommSocketToServiceRecord(this.UUID_ADDRESS);
                    this.bluetoothSocket.connect();
                    
                    this.inputStream = this.bluetoothSocket.getInputStream();
                    this.outputStream = this.bluetoothSocket.getOutputStream();
                    this.outputStream.flush();
                    this.bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
                    this.deviceConnected = true;
                    
                } catch (IOException e) {
                    Log.e("BT", "Error:" + e.getMessage());
                    this.closeConnection();
                }
            } else {
                this.bluetoothDevice.createBond();
            }
        } else {
            this.bluetoothEnableIntent();
        }
    }
    
    private void printBluetoothDevices() {
        if (this.bluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> devices = this.bluetoothAdapter.getBondedDevices();
            if (devices != null){
                for (BluetoothDevice device : devices) {
                    Log.i("BT", "Device Name : " + device.getName());
                    Log.i("BT", "Device Mac Address : " + device.getAddress());
                    
                    device.fetchUuidsWithSdp();
                    ParcelUuid[] uuids = device.getUuids();
                    if (uuids != null){
                        for (ParcelUuid uuid :uuids) {
                            Log.i("BT", "Device UUID Address : " + uuid.getUuid());
                        }
                    }else{
                        Log.i("BT", "No UUID's Found");
                    }
                }
            }else{
                Log.i("BT", "No Devices Found");
            }
        } else {
            this.bluetoothEnableIntent();
        }
    }
    
    private void bluetoothEnableIntent() {
        if (this.mainActivity != null) {
            Log.i("BT", "Bluetooth Not Available");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.mainActivity.startActivityForResult(enableBTIntent, 1);
        } else {
            Log.i("BT", "Main Activity Not Linked");
        }
    }
    
    private void registerReceivers() {
        if (this.mainActivity != null) {
            this.mainActivity.registerReceiver(this.bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            this.mainActivity.registerReceiver(this.bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            this.mainActivity.registerReceiver(this.bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
            this.mainActivity.registerReceiver(this.bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
            this.mainActivity.registerReceiver(this.bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_UUID));
        } else {
            Log.i("BT", "Main Activity Not Linked");
        }
    }
    
    public void unregisterReceivers() {
        if (this.mainActivity != null) {
            this.mainActivity.unregisterReceiver(this.bluetoothReceiver);
        } else {
            Log.i("BT", "Main Activity Not Linked");
        }
    }
    
    interface BluetoothCallback {
        void bluetoothMessage(String message);
    }
}
