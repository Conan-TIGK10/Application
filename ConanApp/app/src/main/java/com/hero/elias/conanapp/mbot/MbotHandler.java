package com.hero.elias.conanapp.mbot;

import com.hero.elias.conanapp.bluetooth.BluetoothHandler;

import java.util.ArrayList;

public class MbotHandler implements BluetoothHandler.BluetoothCallback {
    
    private static MbotHandler sSoleInstance;
    
    int collisionCount;
    
    Vector2D currentPosition;
    Vector2D currentAngle;
    
    double worldWidth;
    double worldHeight;
    
    ArrayList<MbotCallback> mbotCallback;
    
    private MbotHandler() {
        this.mbotCallback = new ArrayList<MbotCallback>();
    }
    
    public static MbotHandler getInstance() {
        if (sSoleInstance == null) {
            sSoleInstance = new MbotHandler();
        }
        return sSoleInstance;
    }
    
    public void IncrementCollision(){
        this.collisionCount++;
    }
    
    public void addCallback(MbotCallback callback) {
        this.mbotCallback.add(callback);
    }
    
    public void removeCallback(MbotCallback callback) {
        this.mbotCallback.remove(callback);
    }
    
    @Override
    public void bluetoothMessage(String message) {
    
    }
    
    interface MbotCallback {
        void onCollision(int collisionCount);
    }
}
