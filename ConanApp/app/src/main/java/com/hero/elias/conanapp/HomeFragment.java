package com.hero.elias.conanapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import eo.view.bluetoothstate.BluetoothState;

public class HomeFragment extends Fragment implements BluetoothHandler.BluetoothCallback {
    
    private BluetoothState bluetoothState;
    private TextView bluetoothStateText;
    
    public HomeFragment() {
    }
    
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        BluetoothHandler.getInstance().addCallback(this);
    
        this.bluetoothState = view.findViewById(R.id.bluetooth_state);
        
        this.bluetoothStateText = view.findViewById(R.id.bluetooth_state_text);
        this.bluetoothStateText.setText("Connecting");
    
        this.bluetoothState.setState(BluetoothState.State.CONNECTING);
    
        super.onViewCreated(view, savedInstanceState);
    }
    
    @Override
    public void onDestroy() {
        BluetoothHandler.getInstance().removeCallback(this);
        super.onDestroy();
    }
    
    @Override
    public void bluetoothMessage(byte[] bytes) {
    }
    
    @Override
    public void onStateChange(BluetoothHandler.BluetoothInState state) {
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
}
