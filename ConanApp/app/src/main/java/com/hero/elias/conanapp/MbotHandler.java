package com.hero.elias.conanapp;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MbotHandler implements BluetoothHandler.BluetoothCallback {
    private static MbotHandler sSoleInstance;
    private ArrayList<MbotHandler.MbotCallback> mbotCallbacks;
    
    private byte[] START_SIGNIFIER = "/".getBytes();
    private byte[] END_SIGNIFIER = "\\".getBytes(); // is escaped
    
    private ByteArrayOutputStream byteArray;
    
    private MbotHandler() {
        this.mbotCallbacks = new ArrayList<>();
        this.byteArray = new ByteArrayOutputStream();
        BluetoothHandler.getInstance().addCallback(this);
    }
    
    public static MbotHandler getInstance() {
        if (MbotHandler.sSoleInstance == null) {
            MbotHandler.sSoleInstance = new MbotHandler();
        }
        return MbotHandler.sSoleInstance;
    }
    
    
    @Override
    public void bluetoothMessage(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++){
            if (Character.toString ((char) bytes[i]).equals("/")){
                try {
                    this.byteArray.flush();
                    this.byteArray.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if (Character.toString ((char) bytes[i]).equals("\\")){
                this.parseMessage();
            }else{
                this.byteArray.write(bytes[i]);
            }
        }
    }
    
    private void parseMessage(){
        byte[] bytes = this.byteArray.toByteArray();
        String s = new String(bytes, StandardCharsets.US_ASCII);
        String[] splitMessage = s.split(",");
        
        // FORMAT : /GYRO,MOTOR,LIDAR,LIGHTLEFT,LIGHTRIGHT\
        if (splitMessage.length == 5){
            int gyroData = Integer.parseInt(splitMessage[0]); // 0-360
            int pwmData = Integer.parseInt(splitMessage[1]); // 0-200000
            int lidarData = Integer.parseInt(splitMessage[2]); // 0-300
            boolean lightDataLeft = Boolean.parseBoolean(splitMessage[3]); // 0,1
            boolean lightDataRight = Boolean.parseBoolean(splitMessage[4]); // 0,1
        }
    
    }
    
    @Override
    public void onStateChange(BluetoothHandler.BluetoothInState state) {
    }
    
    public void removeCallback(final MbotHandler.MbotCallback callback) {
        this.mbotCallbacks.remove(callback);
    }
    
    public void addCallback(final MbotHandler.MbotCallback callback) {
        this.mbotCallbacks.add(callback);
    }
    
    interface MbotCallback {
        void onNewHeading(Vector2D headingVector);
        void onNewSpeed(float speed);
    }
}
