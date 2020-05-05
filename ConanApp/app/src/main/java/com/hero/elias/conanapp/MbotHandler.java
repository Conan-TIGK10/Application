package com.hero.elias.conanapp;

import android.os.SystemClock;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MbotHandler implements BluetoothHandler.BluetoothCallback, Runnable {
    private static MbotHandler sSoleInstance;
    
    private ArrayList<MbotHandler.MbotCallback> mbotCallbacks;
    
    private Thread playbackThread;
    private boolean threadRunning;
    
    private ByteArrayOutputStream byteArray;
    
    private long lastMillis;
    private long millisCounter;
    
    private Vector2D mbotPosition;
    private Vector2D mbotHeading;
    private int lastDistance;
    
    private MbotHandler() {
        this.mbotCallbacks = new ArrayList<>();
        this.byteArray = new ByteArrayOutputStream();
        this.playbackThread = new Thread(this);
        this.mbotPosition = new Vector2D(0f, 0f);
        this.mbotHeading = new Vector2D(0f, 0f);
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
            }else if (Character.toString ((char) bytes[i]).equals("&")){
                byte[] b = this.byteArray.toByteArray();
                String s = new String(b, StandardCharsets.US_ASCII);
                this.parseMessage(s);
            }else{
                this.byteArray.write(bytes[i]);
            }
        }
    }
    
    public Vector2D getHeading() {
        return this.mbotHeading;
    }
    
    public long getMillis() {
        return this.millisCounter;
    }
    
    public Vector2D getPosition() {
        return this.mbotPosition;
    }
    
    private void parseMessage(String message){
        String[] splitMessage = message.split(",");
    
        // FORMAT : GYRO,DISTANCE,LIDAR,LIGHT,MILLIS
        if (splitMessage.length == 5){
            int gyroData = Integer.parseInt(splitMessage[0]); // 0-360 heading in degrees
            int distData = Integer.parseInt(splitMessage[1]); // 0-n distance in cm
            int lidarData = Integer.parseInt(splitMessage[2]); // 0-n strength of lidar sensor
            int lightData = Integer.parseInt(splitMessage[3]); // 0 = none, 1 = left, 2 = right, 3 = both
            long millis = Integer.parseInt(splitMessage[4]); // 0-uint16 millis overflow
    
            long millisDifference = 0;
            if (millis <= this.lastMillis){
                this.millisCounter += millis;
            }else{
                millisDifference = (millis - this.lastMillis);
                this.millisCounter += millisDifference;
            }
            this.lastMillis = millis;
            
            if (distData >= 0){
                if (this.lastDistance < 0){
                    this.lastDistance = 0;
                }
                if (distData >= this.lastDistance){
                    distData -= this.lastDistance;
                    this.lastDistance = distData;
                }else{
                    this.lastDistance = distData;
                }
            }else{
                if (this.lastDistance > 0){
                    this.lastDistance = 0;
                }
                if (distData <= this.lastDistance){
                    distData -= this.lastDistance;
                    this.lastDistance = distData;
                }else{
                    this.lastDistance = distData;
                }
            }
            
            float distance = (float)distData / 2f;
            
            this.mbotHeading = Vector2D.degreeToVector(gyroData);
            this.mbotPosition.add(new Vector2D(this.mbotHeading.x * distance, this.mbotHeading.y * distance));
            
            boolean gap = lightData > 0;
    
            this.onNewData(this.mbotPosition, this.mbotHeading, this.millisCounter, lidarData, gap);
        }
    }
    
    private void onNewData(Vector2D position, Vector2D heading, long millis, int lidar,  boolean gap) {
        for (int i = 0; i < this.mbotCallbacks.size(); i++){
            this.mbotCallbacks.get(i).onNewData(position, heading, millis, lidar,  gap);
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
        int randomMillis = 0;
        int msCounter = 0;
        while (this.threadRunning){
            String s = String.valueOf(Tweening.getRandomNumber(0, 360)) + ","
                    + String.valueOf(Tweening.getRandomNumber(0, 100)) + ","
                    + String.valueOf(Tweening.getRandomNumber(0, 400)) + ","
                    + String.valueOf(Tweening.getRandomNumber(0, 3)) + ","
                    + String.valueOf(msCounter);
            
            this.parseMessage(s);
            randomMillis = Tweening.getRandomNumber(100, 1000);
            msCounter += randomMillis;
            if (msCounter > 65535){
                msCounter -= 65535;
            }
            SystemClock.sleep(randomMillis);
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
        void onNewData(Vector2D position, Vector2D heading, long millis, int lidar, boolean gap);
    }
}
