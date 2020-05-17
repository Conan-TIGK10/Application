package com.hero.elias.conanapp;

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
    
    private final String MAC_ADDRESS = "00:1B:10:65:FC:C5";
    private final UUID SERVICE_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private final UUID READ_UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
    private final UUID WRITE_UUID = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb");
    
    private MainActivity mainActivity;
    
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    
    private final ArrayList<BluetoothCallback> bluetoothCallback;
    
    private CharacteristicSubscription subscription;
    
    private BluetoothInState bluetoothState;
    
    private BluetoothHandler() {
        this.bluetoothCallback = new ArrayList<BluetoothCallback>();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothState = BluetoothInState.NOTFOUND;
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
    
    private void alertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.mainActivity);
        
        builder.setTitle("Bluetooth")
                .setMessage("In order to Communicate with the Robot Bluetooth must be Turned On")
                .setPositiveButton("Ok", (dialog, which) -> {
                    this.enableBluetooth();
                    this.discoverDevices();
                });
        
        final AlertDialog dialog = builder.create();
        
        dialog.show();
    }
    
    public void checkConnection() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.bluetoothAdapter == null) {
            this.updateState(BluetoothInState.BLUETOOTHDISABLED);
        } else if (!this.bluetoothAdapter.isEnabled()) {
            this.updateState(BluetoothInState.BLUETOOTHDISABLED);
        } else {
            this.updateState(BluetoothInState.DISCONNECTED);
        }
    }
    
    public void connect() {
        if (this.bluetoothAdapter != null && this.bluetoothState != BluetoothInState.BLUETOOTHDISABLED && this.bluetoothState != BluetoothInState.CONNECTED && this.bluetoothState != BluetoothInState.CONNECTING) {
            this.updateState(BluetoothInState.CONNECTING);
            this.bluetoothAdapter.cancelDiscovery();
            this.bluetoothDevice = Neatle.getDevice(this.MAC_ADDRESS);
            this.subscription = Neatle.createSubscription(this.mainActivity, this.bluetoothDevice, this.SERVICE_UUID, this.READ_UUID);
            this.subscription.setOnCharacteristicsChangedListener(this);
            this.subscription.start();
        }
    }
    
    private void discoverDevices() {
        //final Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 20);
        //this.mainActivity.startActivity(discoverableIntent);
        
        if (this.bluetoothAdapter != null && this.bluetoothState != BluetoothInState.CONNECTED) {
            this.bluetoothAdapter.startDiscovery();
            this.updateState(BluetoothInState.SEARCHING);
        }
    }
    
    private void enableBluetooth() {
        if (this.mainActivity != null) {
            final Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.mainActivity.startActivityForResult(enableBTIntent, 2);
        } else {
            Log.i("BT", "Main Activity Not Linked");
        }
    }
    
    public BluetoothInState getState() {
        return this.bluetoothState;
    }
    
    @Override
    public void onCharacteristicChanged(final CommandResult change) {
        if (change.wasSuccessful()) {
            if (this.bluetoothState != BluetoothInState.CONNECTED) {
                this.updateState(BluetoothInState.CONNECTED);
            }
            
            this.updateMessage(change.getValue());
        } else {
            this.updateState(BluetoothInState.DISCONNECTED);
        }
    }
    
    @Override
    public void onReceive(final Context context, final Intent intent) {
        switch (intent.getAction()) {
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                
                if (this.bluetoothDevice.getAddress().equals(device.getAddress())) {
                    if (this.bluetoothState != BluetoothInState.CONNECTED) {
                        this.connect();
                    }
                }
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                
                if (this.bluetoothDevice.getAddress().equals(device.getAddress())) {
                    this.updateState(BluetoothInState.DISCONNECTED);
                    this.start();
                }
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        this.updateState(BluetoothInState.BLUETOOTHDISABLED);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        this.start();
                        break;
                }
                break;
            
        }
    }
    
    private void printBluetoothDevices() {
        if (this.bluetoothAdapter != null) {
            return;
        }
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
        }
    }
    
    public void registerReceivers() {
        if (this.mainActivity != null) {
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_UUID));
            
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
            this.mainActivity.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        } else {
            Log.i("BT", "Main Activity Not Linked");
        }
    }
    
    public void removeCallback(final BluetoothCallback callback) {
        this.bluetoothCallback.remove(callback);
    }
    
    public void setMainActivity(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.registerReceivers();
    }
    
    public void start() {
        this.checkConnection();
        if (this.bluetoothState != BluetoothInState.BLUETOOTHDISABLED) {
            this.discoverDevices();
        }
        this.connect();
    }
    
    public void stop() {
        if (this.subscription != null) {
            this.subscription.stop();
        }
        this.updateState(BluetoothInState.DISCONNECTED);
    }
    
    private void unregisterReceivers() {
        if (this.mainActivity != null) {
            this.mainActivity.unregisterReceiver(this);
        } else {
            Log.i("BT", "Main Activity Not Linked");
        }
    }
    
    private void updateMessage(final byte[] bytes) {
        for (int i = 0; i < this.bluetoothCallback.size(); i++) {
            this.bluetoothCallback.get(i).bluetoothMessage(bytes);
        }
    }
    
    private void updateState(final BluetoothInState newState) {
        if (newState == BluetoothInState.BLUETOOTHDISABLED && this.bluetoothState != BluetoothInState.BLUETOOTHDISABLED) {
            this.alertDialog();
        }
        
        this.bluetoothState = newState;
        for (int i = 0; i < this.bluetoothCallback.size(); i++) {
            this.bluetoothCallback.get(i).onStateChange(this.bluetoothState);
        }
    }
    
    public void write(final byte[] bytes) {
        if (this.bluetoothState == BluetoothInState.CONNECTED) {
            final ByteArrayInputSource inputSource = new ByteArrayInputSource(bytes);
            final Operation writeOperation = Neatle.createOperationBuilder(this.mainActivity)
                    .write(this.SERVICE_UUID, this.WRITE_UUID, inputSource)
                    .onFinished(new SimpleOperationObserver() {
                        @Override
                        public void onOperationFinished(final Operation op, final OperationResults results) {
                            if (results.wasSuccessful()) {
                            } else {
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
