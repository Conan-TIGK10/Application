package com.hero.elias.conanapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hero.elias.conanapp.bluetooth.BluetoothHandler;
import com.hero.elias.conanapp.mbot.PlaybackSystem;
import com.hero.elias.conanapp.views.DrawView;
import com.hero.elias.conanapp.wifi.WifiHandler;


public class MainActivity extends AppCompatActivity implements BluetoothHandler.BluetoothCallback, View.OnClickListener {
    
    Button sendButton;
    TextView receiveTextView;
    DrawView drawView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
    
        this.drawView = (DrawView) findViewById(R.id.view_draw);
        this.drawView.setBackgroundColor(Color.WHITE);
    
        this.sendButton = (Button) findViewById(R.id.send_button);
        this.sendButton.setOnClickListener(this);
        
        this.receiveTextView = findViewById(R.id.receive_text);
        
        BluetoothHandler.getInstance().setMainActivity(this);
        BluetoothHandler.getInstance().addCallback(this);
        
        WifiHandler.GetPosition(new WifiHandler.PositionGetListener() {
            @Override
            public void onFinished(double x, double y) {
                Log.i("MSG", String.valueOf(x));
                Log.i("MSG", String.valueOf(y));
            }
        });
        
        //WifiHandler.PostPosition(3.1, 4.2);
        
        PlaybackSystem playbackSystem = new PlaybackSystem();
        playbackSystem.startThread();
        
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
        //BluetoothHandler.getInstance().startThread();
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

