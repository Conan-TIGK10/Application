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

public class HomeFragment extends Fragment {
    
    BluetoothState bluetoothState;
    TextView bluetoothStateText;
    
    public HomeFragment() {
    
    }
    
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
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
        this.bluetoothState = (BluetoothState) view.findViewById(R.id.bluetooth_state);
        this.bluetoothState.setState(BluetoothState.State.CONNECTING);
        this.bluetoothStateText = (TextView) view.findViewById(R.id.bluetooth_state_text);
        this.bluetoothStateText.setText("Connected");
    
        super.onViewCreated(view, savedInstanceState);
    }
}
