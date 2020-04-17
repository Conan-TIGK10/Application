package com.hero.elias.conanapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class CommandFragment extends Fragment implements BluetoothHandler.BluetoothCallback, View.OnClickListener {
    private Button sendButton;
    private TextView receiveTextView;
    
    public CommandFragment() {
        BluetoothHandler.getInstance().addCallback(this);
    }
    
    public static CommandFragment newInstance() {
        return new CommandFragment();
    }
    
    @Override
    public void bluetoothMessage(String message) {
        this.receiveTextView.setText(message);
    }
    
    @Override
    public void onClick(View v) {
        String message = "Hello Friend";
        BluetoothHandler.getInstance().write(message.getBytes());
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_command, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.sendButton = view.findViewById(R.id.send_button);
        this.sendButton.setOnClickListener(this);
        
        this.receiveTextView = view.findViewById(R.id.receive_text);
        
        super.onViewCreated(view, savedInstanceState);
    }
    
    @Override
    public void onDestroy() {
        BluetoothHandler.getInstance().removeCallback(this);
        super.onDestroy();
    }
}
