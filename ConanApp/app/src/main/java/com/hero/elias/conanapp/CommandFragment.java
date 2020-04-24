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

import java.io.UnsupportedEncodingException;

public class CommandFragment extends Fragment implements BluetoothHandler.BluetoothCallback {
    private Button upButton;
    private Button leftButton;
    private Button rightButton;
    private Button downButton;
    private Button stopButton;
    
    private TextView receiveTextView;
    private int messagesReceived;
    
    public CommandFragment() {
        BluetoothHandler.getInstance().addCallback(this);
    }
    
    public static CommandFragment newInstance() {
        return new CommandFragment();
    }
    
    @Override
    public void bluetoothMessage(byte[] bytes) {
        this.messagesReceived++;
        this.receiveTextView.setText(this.messagesReceived + ": " + bytes.toString());
    }
    
    @Override
    public void onStateChange(BluetoothHandler.BluetoothInState state) {
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
        this.upButton = view.findViewById(R.id.up_button);
        this.upButton.setOnClickListener(v -> {
            String message = "/1,1\\";
            try {
                BluetoothHandler.getInstance().write(message.getBytes("US-ASCII"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    
        this.rightButton = view.findViewById(R.id.right_button);
        this.rightButton.setOnClickListener(v -> {
            String message = "/1,3\\";
            try {
                BluetoothHandler.getInstance().write(message.getBytes("US-ASCII"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    
        this.downButton = view.findViewById(R.id.down_button);
        this.downButton.setOnClickListener(v -> {
            String message = "/1,4\\";
            try {
                BluetoothHandler.getInstance().write(message.getBytes("US-ASCII"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    
        this.leftButton = view.findViewById(R.id.left_button);
        this.leftButton.setOnClickListener(v -> {
            String message = "/1,2\\";
            try {
                BluetoothHandler.getInstance().write(message.getBytes("US-ASCII"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    
        this.stopButton = view.findViewById(R.id.stop_button);
        this.stopButton.setOnClickListener(v -> {
            String message = "/1,0\\";
            try {
                BluetoothHandler.getInstance().write(message.getBytes("US-ASCII"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    
        this.receiveTextView = view.findViewById(R.id.receive_text);
        this.messagesReceived = 0;
        
        super.onViewCreated(view, savedInstanceState);
    }
    
    @Override
    public void onDestroy() {
        BluetoothHandler.getInstance().removeCallback(this);
        super.onDestroy();
    }
}
