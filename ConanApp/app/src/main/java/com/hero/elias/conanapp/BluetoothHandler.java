package com.hero.elias.conanapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import si.inova.neatle.Neatle;
import si.inova.neatle.operation.CharacteristicSubscription;
import si.inova.neatle.operation.CharacteristicsChangedListener;
import si.inova.neatle.operation.CommandResult;
import si.inova.neatle.operation.Operation;
import si.inova.neatle.operation.OperationResults;
import si.inova.neatle.operation.SimpleOperationObserver;
import si.inova.neatle.source.ByteArrayInputSource;

public class BluetoothHandler extends BroadcastReceiver implements CharacteristicsChangedListener {
    
    private static BluetoothHandler sSoleInstance;
    
    private String MAC_ADDRESS = "00:1B:10:65:FC:C5";
    private UUID SERVICE_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private UUID READ_UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
    private UUID WRITE_UUID = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb");
    
    private Activity mainActivity;
    
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    
    private final ArrayList<BluetoothCallback> bluetoothCallback;
    
    private CharacteristicSubscription subscription;
    
    private boolean deviceConnected;
    
    private BluetoothInState bluetoothState;
    
    private BluetoothHandler() {
        this.deviceConnected = false;
        this.bluetoothCallback = new ArrayList<BluetoothCallback>();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothState = BluetoothInState.BLUETOOTHDISABLED;
        this.updateState();
    }
    
    public static BluetoothHandler getInstance() {
        if (BluetoothHandler.sSoleInstance == null) {
            BluetoothHandler.sSoleInstance = new BluetoothHandler();
        }
        return BluetoothHandler.sSoleInstance;
    }
    
    public void addCallback(final BluetoothCallback callback) {
        this.bluetoothCallback.add(callback);
    }
    
    private void bluetoothEnableIntent() {
        if (this.mainActivity != null) {
            Log.i("BT", "Bluetooth Not Available");
    
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mainActivity);
    
            builder.setMessage("Bluetooth")
                    .setTitle("In order to Communicate with the robot Bluetooth must be turned on, Please turn on.");
            AlertDialog dialog = builder.create();
    
            dialog.show();
            
            final Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.mainActivity.startActivityForResult(enableBTIntent, 1);
        } else {
            Log.i("BT", "Main Activity Not Linked");
        }
    }
    
    private boolean checkConnection() {
        return this.subscription.isStarted();
    }
    
    public void connect() {
        this.init();
        if (this.bluetoothAdapter.isEnabled()) {
            this.bluetoothState = BluetoothInState.SEARCHING;
            this.updateState();
            this.bluetoothDevice = Neatle.getDevice(this.MAC_ADDRESS);
            this.subscription = Neatle.createSubscription(this.mainActivity, this.bluetoothDevice, this.SERVICE_UUID, this.READ_UUID);
            this.subscription.setOnCharacteristicsChangedListener(this);
            this.subscription.start();
        } else {
            this.bluetoothEnableIntent();
        }
    }
    
    public void setMainActivity(final MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }
    
    private void discoverDevices() {
        final Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        this.mainActivity.startActivity(discoverableIntent);
        this.bluetoothAdapter.startDiscovery();
    }
    
    public void dissconnect() {
        if (this.subscription.isStarted()) {
            this.bluetoothState = BluetoothInState.DISCONNECTED;
            this.updateState();
            this.deviceConnected = false;
            this.subscription.stop();
            this.unregisterReceivers();
        }
    }
    
    public BluetoothInState getState() {
        return this.bluetoothState;
    }
    
    private void init() {
        this.bluetoothState = BluetoothInState.BLUETOOTHDISABLED;
        this.updateState();
        while (this.bluetoothAdapter == null) {
            this.bluetoothEnableIntent();
            this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        this.registerReceivers();
    }
    
    @Override
    public void onCharacteristicChanged(final CommandResult change) {
        if (change.wasSuccessful()) {
            if (this.bluetoothState != BluetoothInState.CONNECTED) {
                this.bluetoothState = BluetoothInState.CONNECTED;
                this.updateState();
            }
            
            Log.i("BT", "RECEIVED FROM ROBOT: " + change.getValueAsString());
            this.updateMessage(change.getValue());
        } else {
            this.bluetoothState = BluetoothInState.DISCONNECTED;
            this.updateState();
        }
    }
    
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        switch (intent.getAction()) {
            case BluetoothDevice.ACTION_FOUND:
                //Log.i("BT", "Found New Device " + device.getName());
                break;
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                //Log.i("BT", "Bond State Changed " + device.getName());
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                Log.i("BT", "Device Connected " + device.getName());
                if (this.bluetoothDevice.getAddress().equals(device.getAddress())) {
                    this.bluetoothState = BluetoothInState.CONNECTED;
                    this.updateState();
                    if (!this.checkConnection()){
                        this.connect();
                    }
                }
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                Log.i("BT", "Device Dissconnected " + device.getName());
                if (this.bluetoothDevice.getAddress().equals(device.getAddress())) {
                    this.bluetoothState = BluetoothInState.DISCONNECTED;
                    this.updateState();
                    this.connect();
                }
                break;
        }
    }
    
    private void printBluetoothDevices() {
        if (this.bluetoothAdapter.isEnabled()) {
            final Set<BluetoothDevice> devices = this.bluetoothAdapter.getBondedDevices();
            if (devices != null) {
                for (final BluetoothDevice device : devices) {
                    Log.i("BT", "Device Name : " + device.getName());
                    Log.i("BT", "Device Mac Address : " + device.getAddress());
                    
                    device.fetchUuidsWithSdp();
                    final ParcelUuid[] uuids = device.getUuids();
                    if (uuids != null) {
                        for (final ParcelUuid uuid : uuids) {
                            Log.i("BT", "Device UUID Address : " + uuid.getUuid());
                        }
                    } else {
                        Log.i("BT", "No UUID's Found");
                    }
                }
            } else {
                Log.i("BT", "No Devices Found");
            }
        } else {
            this.bluetoothEnableIntent();
        }
    }
    
    private void registerReceivers() {
        if (this.mainActivity != null) {
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_UUID));
        } else {
            Log.i("BT", "Main Activity Not Linked");
        }
    }
    
    public void removeCallback(final BluetoothCallback callback) {
        this.bluetoothCallback.remove(callback);
    }
    
    private void unregisterReceivers() {
        if (this.mainActivity != null) {
            this.mainActivity.unregisterReceiver(this);
        } else {
            Log.i("BT", "Main Activity Not Linked");
        }
    }
    
    private void updateMessage(byte[] bytes) {
        for (int i = 0; i < this.bluetoothCallback.size(); i++) {
            this.bluetoothCallback.get(i).bluetoothMessage(bytes);
        }
    }
    
    private void updateState() {
        for (int i = 0; i < this.bluetoothCallback.size(); i++) {
            this.bluetoothCallback.get(i).onStateChange(this.bluetoothState);
        }
    }
    
    public void write(final byte[] bytes) {
        if (this.deviceConnected) {
            ByteArrayInputSource inputSource = new ByteArrayInputSource(bytes);
            final Operation writeOperation = Neatle.createOperationBuilder(this.mainActivity)
                    .write(this.SERVICE_UUID, this.WRITE_UUID, inputSource)
                    .onFinished(new SimpleOperationObserver() {
                        @Override
                        public void onOperationFinished(final Operation op, final OperationResults results) {
                            if (results.wasSuccessful()) {
                                System.out.println("Write was successful!");
                            } else {
                                System.out.println("Write failed! ");
                            }
                        }
                    })
                    .build(this.bluetoothDevice);
            writeOperation.execute();
        }
    }
    
    enum BluetoothInState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED,
        NOTFOUND,
        SEARCHING,
        BLUETOOTHDISABLED
    }
    
    interface BluetoothCallback {
        void bluetoothMessage(byte[] bytes);
        
        void onStateChange(BluetoothInState state);
    }
}
