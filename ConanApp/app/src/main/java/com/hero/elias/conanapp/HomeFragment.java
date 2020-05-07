package com.hero.elias.conanapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import eo.view.bluetoothstate.BluetoothState;

public class HomeFragment extends Fragment implements BluetoothHandler.BluetoothCallback, WifiHandler.WifiCallback {
    
    private BluetoothState bluetoothState;
    private TextView bluetoothStateText;
    
    public HomeFragment() {
        BluetoothHandler.getInstance().addCallback(this);
        WifiHandler.getInstance().addCallback(this);
    }
    
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        this.bluetoothState = view.findViewById(R.id.bluetooth_state);
        
        this.bluetoothStateText = view.findViewById(R.id.bluetooth_state_text);
        this.setState(BluetoothHandler.getInstance().getState());
    
        super.onViewCreated(view, savedInstanceState);
    }
    
    @Override
    public void onDestroy() {
        BluetoothHandler.getInstance().removeCallback(this);
        super.onDestroy();
    }
    
    @Override
    public void bluetoothMessage(final byte[] bytes) {
    }
    
    @Override
    public void onStateChange(final BluetoothHandler.BluetoothInState state) {
        this.setState(state);
    }
    
    private void setState(final BluetoothHandler.BluetoothInState state){
        if (this.bluetoothState == null){return;}
        switch (state){
            case NOTFOUND:
                this.bluetoothState.setState(BluetoothState.State.SEARCHING);
                this.bluetoothStateText.setText("Robot Not Found");
                break;
            case BLUETOOTHDISABLED:
                this.bluetoothState.setState(BluetoothState.State.OFF);
                this.bluetoothStateText.setText("Bluetooth Disabled");
                break;
            case CONNECTED:
                this.bluetoothState.setState(BluetoothState.State.CONNECTED);
                this.bluetoothStateText.setText("Connected !");
                break;
            case SEARCHING:
                this.bluetoothState.setState(BluetoothState.State.SEARCHING);
                this.bluetoothStateText.setText("Searching...");
                break;
            case CONNECTING:
                this.bluetoothState.setState(BluetoothState.State.CONNECTING);
                this.bluetoothStateText.setText("Connecting...");
                break;
            case DISCONNECTED:
                this.bluetoothState.setState(BluetoothState.State.SEARCH);
                this.bluetoothStateText.setText("Disconnected");
                break;
        }
    }
    
    @Override
    public void onStateChange(WifiHandler.WifiInState state) {
        Log.i("WIFI", "HEEY");
    }
}
