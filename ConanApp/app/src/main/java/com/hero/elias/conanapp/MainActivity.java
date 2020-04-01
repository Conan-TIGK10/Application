package com.hero.elias.conanapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements BluetoothCallback, View.OnClickListener  {
    
    Button sendButton;
    TextView receiveTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
    
        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);
        
        this.receiveTextView = findViewById(R.id.receive_text);
        
        BluetoothHandler.getInstance().setMainActivity(this);
        BluetoothHandler.getInstance().addCallback(this);
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothHandler.getInstance().unregisterReceivers();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        BluetoothHandler.getInstance().stopThread();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        BluetoothHandler.getInstance().startThread();
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
}
