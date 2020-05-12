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
    
    private long millisCounter;
    private long lastMillis;
    private long timeStamp;
    private boolean collision;
    
    private Vector2D mbotPosition;
    private Vector2D mbotHeading;
    private int lastDistance;
    private int firstGyro;
    
    private MbotHandler() {
        this.mbotCallbacks = new ArrayList<MbotHandler.MbotCallback>();
        this.byteArray = new ByteArrayOutputStream();
        this.playbackThread = new Thread(this);
        this.mbotPosition = new Vector2D(0f, 0f);
        this.mbotHeading = new Vector2D(0f, 0f);
        this.collision = false;
        this.firstGyro = -10000;
        BluetoothHandler.getInstance().addCallback(this);
    }
    
    public static MbotHandler getInstance() {
        if (MbotHandler.sSoleInstance == null) {
            MbotHandler.sSoleInstance = new MbotHandler();
        }
        return MbotHandler.sSoleInstance;
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
    
    @Override
    public void onStateChange(BluetoothHandler.BluetoothInState state) {
    }
    
    private void parseMessage(String message){
        //Log.i("MBOT", message);
        String[] splitMessage = message.split(",");
    
        // FORMAT : GYRO,DISTANCE,LIDAR,LIGHT,MILLIS
        if (splitMessage.length == 5){
            int gyroData = Integer.parseInt(splitMessage[0]); // -360-360 absolute heading in degrees
            int distData = Integer.parseInt(splitMessage[1]); // -n-n distance accumlative in forwards/backwards in cm
            int lidarData = Integer.parseInt(splitMessage[2]); // 0-400 absolute strength of lidar sensor
            int lightData = Integer.parseInt(splitMessage[3]); // absolute lightsensor 0 = none, 1 = left, 2 = right, 3 = both
            long millisData = Integer.parseInt(splitMessage[4]); // 0-n millis accumlative overflow
            
            if (this.firstGyro == -10000){
                this.firstGyro = gyroData;
            }
            
            gyroData -= this.firstGyro;
    
            long diffrenceMillis = 0;
            if (millisData <= this.lastMillis){ // overflow, starts from 0 again
                diffrenceMillis = millisData;
            }else{
                diffrenceMillis =  (millisData - this.lastMillis); // current - previous to get difference
            }
            this.millisCounter += diffrenceMillis;
            this.lastMillis = millisData;
            
            int differenceDist = 0;
            if (distData >= 0){ // driving forwards
                if (this.lastDistance < 0){ // switching from backwards to fowards, reset to 0
                    distData = 0;
                    this.lastDistance = 0;
                }
                
                if (distData >= this.lastDistance){ // driving in same direction, distData is accumlating
                    differenceDist = distData - this.lastDistance; // current - previous to get difference
                    this.lastDistance = distData;
                }else{ // driving in new direction, distData starts from 0
                    differenceDist = 0;
                }
            }else{ // driving backwards
                if (this.lastDistance > 0){ // switcing from forwards to backwards, reset to 0
                    distData = 0;
                    this.lastDistance = 0;
                }
                if (distData <= this.lastDistance){ // backwards in same direction, distData is accumilating
                    differenceDist = distData - this.lastDistance; // current - previous to get difference
                    this.lastDistance = distData;
                }else{ // backwards in new direction, distData starts from 0
                    differenceDist = 0;
                }
            }
    
            Log.i("ROBO", String.valueOf(message));
            if (Math.abs(differenceDist) > 40){
                Log.i("DIFF", String.valueOf(differenceDist));
            }
    
            this.mbotHeading = Vector2D.degreeToVector(gyroData);
            Vector2D v = new Vector2D(this.mbotHeading.x * differenceDist, this.mbotHeading.y * differenceDist);
            v.multiply(2);
            this.mbotPosition.add(v);
            
            this.onNewData(this.mbotPosition, this.mbotHeading, this.millisCounter, lidarData, lightData);
            
            if (((lidarData < 20) || (lightData > 0))){
                this.collision = true;
            }
            
            if ((this.millisCounter - this.timeStamp) > 500) {
                this.timeStamp = this.millisCounter;
                
                if (this.collision){
                    this.collision = false;
                    WifiHandler.getInstance().postCollision(this.mbotPosition.x, this.mbotPosition.y, gyroData, this.millisCounter, () -> {
                    });
                }else{
                    WifiHandler.getInstance().postPosition(this.mbotPosition.x, this.mbotPosition.y, gyroData, this.millisCounter,  posId -> {
                    });
                }
            }
        }
    }
    
    public void addCallback(final MbotHandler.MbotCallback callback) {
        this.mbotCallbacks.add(callback);
    }
    
    public void removeCallback(final MbotHandler.MbotCallback callback) {
        this.mbotCallbacks.remove(callback);
    }
    
    interface MbotCallback {
        void onNewData(Vector2D position, Vector2D heading, long millis, int lidar, int gap);
    }
    
    private void onNewData(Vector2D position, Vector2D heading, long millis, int lidar,  int gap) {
        for (int i = 0; i < this.mbotCallbacks.size(); i++){
            this.mbotCallbacks.get(i).onNewData(position, heading, millis, lidar, gap);
        }
    }
    
    @Override
    public void run() {
        int msCounter = 0;
        int angleCounter = 0;
        
        this.threadRunning = true;
        while (this.threadRunning){
            int randomMillis = Tweening.getRandomNumber(100, 1000);
            angleCounter += 10;
            SystemClock.sleep(randomMillis);
            
            msCounter = (msCounter + randomMillis) % 65535; // simulate overflow
            
            String s = String.valueOf(Tweening.getRandomNumber(-360, 360)) + ","
                    + String.valueOf(Tweening.getRandomNumber(-100, 100)) + ","
                    + String.valueOf(Tweening.getRandomNumber(0, 400)) + ","
                    + String.valueOf(Tweening.getRandomNumber(-50, 3)) + ","
                    + String.valueOf(msCounter);
            
            this.parseMessage(s);
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
}
