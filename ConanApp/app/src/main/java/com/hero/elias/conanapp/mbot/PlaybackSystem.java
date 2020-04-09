package com.hero.elias.conanapp.mbot;

import android.util.Log;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PlaybackSystem implements Runnable {
    
    public Thread playbackThread;
    public boolean threadRunning;
    public ArrayList<MbotData> data;
    
    public PlaybackSystem() {
        this.playbackThread = new Thread(this, "Playback Thread");
        this.threadRunning = false;
        this.data = new ArrayList<MbotData>();
    }
    
    public void init() {
        this.data = new ArrayList<MbotData>();
        
        Date startTime = new Date();
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < 32; i++) {
            MbotData m = new MbotData();
            m.position = new Vector2D(0, 0);
            m.facingAngle = new Vector2D(1, 0);
            m.timeStamp = new Date();
            
            c.setTime(startTime);
            c.add(Calendar.SECOND, 2);
            startTime = c.getTime();
            
            m.timeStamp = startTime;
            this.data.add(m);
        }
    }
    
    public void stopThread() {
        this.threadRunning = false;
    }
    
    public void startThread() {
        if (!this.playbackThread.isAlive()) {
            this.playbackThread = new Thread(this, "Playback Thread");
            this.playbackThread.start();
        }
    }
    
    @Override
    public void run() {
        this.init();
        
        for (int i = 0; i < this.data.size() - 1; i++) {
            MbotData first = this.data.get(i);
            MbotData second = this.data.get(i + 1);
            
            long diffInMs = Math.abs(first.timeStamp.getTime() - second.timeStamp.getTime());
            
            try {
                Log.i("PLAYBACK", String.valueOf(first.position));
                this.playbackThread.sleep(diffInMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
