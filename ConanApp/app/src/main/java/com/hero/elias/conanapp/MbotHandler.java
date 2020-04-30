package com.hero.elias.conanapp;

import android.os.SystemClock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MbotHandler implements BluetoothHandler.BluetoothCallback, Runnable {
    private static MbotHandler sSoleInstance;
    private Thread playbackThread;
    private boolean threadRunning;
    private ArrayList<MbotHandler.MbotCallback> mbotCallbacks;
    
    private ByteArrayOutputStream byteArray;
    
    private MbotHandler() {
        this.mbotCallbacks = new ArrayList<>();
        this.byteArray = new ByteArrayOutputStream();
        this.playbackThread = new Thread(this);
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
                byte[] b = this.byteArray.toByteArray();
                String s = new String(b, StandardCharsets.US_ASCII);
                this.parseMessage(s);
            }else{
                this.byteArray.write(bytes[i]);
            }
        }
    }
    
    private void parseMessage(String message){
        String[] splitMessage = message.split(",");
        
        // FORMAT : GYRO,MOTOR,LIDAR,LIGHTLEFT,LIGHTRIGHT
        if (splitMessage.length == 5){
            int gyroData = Integer.parseInt(splitMessage[0]); // 0-360
            int pwmData = Integer.parseInt(splitMessage[1]); // 0-200000
            int lidarData = Integer.parseInt(splitMessage[2]); // 0-300
            boolean lightDataLeft = Boolean.parseBoolean(splitMessage[3]); // 0,1
            boolean lightDataRight = Boolean.parseBoolean(splitMessage[4]); // 0,1
            
            this.onNewHeading(Vector2D.degreeToVector(gyroData));
            this.onNewSpeed((float) Tweening.linear(pwmData, 0, 200000, 0f, 2f));
            this.onLidarStrength((float) Tweening.linear(lidarData, 0, 300, 0f, 2f));
            
            if (lidarData < 100){
                this.onCollision();
            }
            if (lightDataLeft && lightDataRight){
                this.onGap();
            }
        }
    
    }
    
    private void onNewHeading(Vector2D v){
        for (int i = 0; i < this.mbotCallbacks.size(); i++){
            this.mbotCallbacks.get(i).onNewHeading(v);
        }
    }
    
    private void onNewSpeed(float speed){
        for (int i = 0; i < this.mbotCallbacks.size(); i++){
            this.mbotCallbacks.get(i).onNewSpeed(speed);
        }
    }
    
    private void onLidarStrength(float strength){
        for (int i = 0; i < this.mbotCallbacks.size(); i++){
            this.mbotCallbacks.get(i).onLidarStrength(strength);
        }
    }
    
    private void onCollision(){
        for (int i = 0; i < this.mbotCallbacks.size(); i++){
            this.mbotCallbacks.get(i).onCollision();
        }
    }
    
    private void onGap(){
        for (int i = 0; i < this.mbotCallbacks.size(); i++){
            this.mbotCallbacks.get(i).onGap();
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
    
    @Override
    public void run() {
        this.threadRunning = true;
        while (this.threadRunning){
            String s = String.valueOf(Tweening.getRandomNumber(0, 360)) + ","
                    + String.valueOf(Tweening.getRandomNumber(0, 200000)) + ","
                    + String.valueOf(Tweening.getRandomNumber(0, 300)) + ","
                    + String.valueOf(Tweening.getRandomNumber(0, 1)) + ","
                    + String.valueOf(Tweening.getRandomNumber(0, 1));
            
            this.parseMessage(s);
            SystemClock.sleep(1000);
        }
    }
    
    public void startThread(){
        if (!this.playbackThread.isAlive()){
            this.playbackThread = new Thread(this);
            this.playbackThread.setPriority(Thread.MIN_PRIORITY);
            this.playbackThread.start();
        }
    }
    
    public void stopThread(){
        if (this.playbackThread.isAlive()){
            this.threadRunning = false;
        }
    }
    
    interface MbotCallback {
        void onNewHeading(Vector2D headingVector);
        void onNewSpeed(float speed);
        void onLidarStrength(float strength);
        void onCollision();
        void onGap();
    
    }
}
