package com.hero.elias.conanapp;

import android.os.SystemClock;

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
    
    private int lastMillis;
    private int millisCounter;
    
    private Vector2D mbotPosition;
    private Vector2D mbotHeading;
    
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
            }else if (Character.toString ((char) bytes[i]).equals("\\")){
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
    
    public int getMillis() {
        return this.millisCounter;
    }
    
    public Vector2D getPosition() {
        return this.mbotPosition;
    }
    
    private void parseMessage(String message){
        String[] splitMessage = message.split(",");
        
        // FORMAT : GYRO,DISTANCE,LIDAR,LIGHTLEFT,LIGHTRIGHT,MILLIS
        if (splitMessage.length == 6){
            int gyroData = Integer.parseInt(splitMessage[0]); // 0-360 heading in degrees
            int distData = Integer.parseInt(splitMessage[1]); // 0-n distance in cm
            int lidarData = Integer.parseInt(splitMessage[2]); // 0-300 strength of lidar sensor
            boolean lightDataLeft = Boolean.parseBoolean(splitMessage[3]); // 0,1 bool of light sensor
            boolean lightDataRight = Boolean.parseBoolean(splitMessage[4]); // 0,1 bool of light sensor
            int millis = Integer.parseInt(splitMessage[5]); // 0-uint16 millis overflow
    
            int millisDifference;
            if (millis < this.lastMillis){
                millisDifference = (65535 - this.lastMillis) + millis;
                this.millisCounter += millisDifference;
    
            }else{
                millisDifference = (millis - this.lastMillis);
                this.millisCounter += millisDifference;
            }
            this.lastMillis = millis;
    
            this.mbotHeading = Vector2D.degreeToVector(gyroData);
            this.mbotPosition.add(new Vector2D(this.mbotHeading.x * distData, this.mbotHeading.y * distData));
            float lidar = (float) Tweening.linear(lidarData, 0, 300, 0f, 2f);
            
            boolean gap = lightDataLeft || lightDataRight;
            
            this.onNewData(this.mbotPosition, this.mbotHeading, this.millisCounter, lidar, gap);
        }
    }
    
    private void onNewData(Vector2D position, Vector2D heading, int millis, float lidar,  boolean gap) {
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
                    + String.valueOf(Tweening.getRandomNumber(0, 300)) + ","
                    + String.valueOf(Tweening.getRandomNumber(0, 1)) + ","
                    + String.valueOf(Tweening.getRandomNumber(0, 1)) + ","
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
        void onNewData(Vector2D position, Vector2D heading, int millis, float lidar, boolean gap);
    }
}
