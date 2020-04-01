package com.hero.elias.conanapp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements BluetoothCallback {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        
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
        Log.i("BT", message);
    }
}
