package com.hero.elias.conanapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class WifiHandler extends BroadcastReceiver {
    
    private static WifiHandler sSoleInstance;
    
    private MainActivity mainActivity;
    
    private int sessionId;
    private boolean sessionSet;
    
    private WifiInState wifiState;
    
    private ArrayList<WifiCallback> wifiCallback;
    
    private WifiHandler() {
        this.sessionSet = false;
        this.wifiCallback = new ArrayList<WifiCallback>();
    }
    
    public static WifiHandler getInstance() {
        if (WifiHandler.sSoleInstance == null) {
            WifiHandler.sSoleInstance = new WifiHandler();
        }
        return WifiHandler.sSoleInstance;
    }
    
    public void getLastPosition(PositionGetListener getListener) {
        new AsyncHTTPGet("http://3.122.218.59/api/position", response -> {
            try {
                JSONArray jsonArray = new JSONArray(response);
                if (jsonArray.length() > 0){
                    JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length()-1);
                    getListener.onFinished(jsonObject.getInt("id"), jsonObject.getDouble("x"), jsonObject.getDouble("y"), jsonObject.getInt("sessionId"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).execute();
    }
    
    interface PositionGetListener {
        void onFinished(int id, double x, double y, int sessionId);
    }
    
    public void postPosition(double x, double y, PositionPostListener postListener) {
        if (!this.sessionSet){ return; }
        
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
        String date = dateFormat.format(now);
        
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("x", x);
            jsonParam.put("y", y);
            jsonParam.put("sessionId", this.sessionId);
            jsonParam.put("read_at", date);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        new AsyncHTTPPost("http://3.122.218.59/api/position", jsonParam, responseString -> {
            try {
                JSONObject jsonObject = new JSONObject(responseString);
                postListener.onFinished(jsonObject.getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).execute();
    }
    interface PositionPostListener {
        void onFinished(int posId);
    }
    
    public void postCollision(double x, double y, CollisionPostListener collisionListener){
        if (!this.sessionSet){ return; }
        
        postPosition(x, y, (id) -> {
            JSONObject jsonParam = new JSONObject();
            try {
                jsonParam.put("positionId", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
    
            new AsyncHTTPPost("http://3.122.218.59/api/collision", jsonParam, responseString -> {
                collisionListener.onFinished();
            }).execute();
        });
    }
    
    interface CollisionPostListener {
        void onFinished();
    }
    
    public void createSession(String sessionName, SessionCreateListener createListener){
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("name", sessionName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    
        new AsyncHTTPPost("http://3.122.218.59/api/session", jsonParam, responseString -> {
            try {
                JSONObject jsonObject = new JSONObject(responseString);
                this.sessionId = jsonObject.getInt("id");
                this.sessionSet = true;
                createListener.onFinished();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).execute();
    }
    interface SessionCreateListener {
        void onFinished();
    }
    
    public void setMainActivity(final MainActivity mainActivity){
        this.mainActivity = mainActivity;
        this.registerReceivers();
    }
    
    private void registerReceivers(){
        if (this.mainActivity != null) {
            mainActivity.registerReceiver(this, new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION));
        } else {
            Log.i("WIFI", "Main Activity Not Linked");
        }
    }
    
    public void unregisterReceivers() {
        if (this.mainActivity != null) {
            this.mainActivity.unregisterReceiver(this);
        } else {
            Log.i("WIFI", "Main Activity Not Linked");
        }
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                this.updateState(WifiInState.CONNECTED);
            } else {
                this.updateState(WifiInState.DISCONNECTED);
            }
        }
    }
    
    public void checkConnection(){
        if (this.mainActivity != null){
            WifiManager wifiMgr = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiMgr.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                if( wifiInfo.getNetworkId() == -1 ){
                    this.updateState(WifiInState.DISCONNECTED);
                }else{
                    this.updateState(WifiInState.CONNECTED);
                }
            }
            else {
                this.updateState(WifiInState.DISCONNECTED);
            }
        }else{
            Log.i("WIFI", "Main Activity Not Linked");
        }
    }
    
    private void alertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        
        builder.setTitle("Wifi")
                .setMessage("In order to Send data to the Backend Database, Wifi must be Turned on")
                .setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        
        dialog.show();
    }
    
    enum WifiInState {
        CONNECTED,
        DISCONNECTED,
    }
    
    public WifiInState getState() {
        return this.wifiState;
    }
    
    interface WifiCallback {
        void onStateChange(WifiInState state);
    }
    
    public void removeCallback(final WifiCallback callback) {
        this.wifiCallback.remove(callback);
    }
    public void addCallback(final WifiCallback callback) {
        this.wifiCallback.add(callback);
    }
    
    private void updateState(WifiInState newState) {
        this.wifiState = newState;
        for (int i = 0; i < this.wifiCallback.size(); i++) {
            this.wifiCallback.get(i).onStateChange(this.wifiState);
        }
        
        if (this.wifiState == WifiInState.DISCONNECTED){
            this.alertDialog();
        }
    }
}


class AsyncHTTPPost extends AsyncTask<Void, Void, String> {
    
    private final TaskListener taskListener;
    private final String urlString;
    private final JSONObject jsonObject;
    
    public AsyncHTTPPost(String urlString, JSONObject jsonObject, TaskListener listener) {
        this.taskListener = listener;
        this.urlString = urlString;
        this.jsonObject = jsonObject;
    }
    
    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL(this.urlString);
    
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);
    
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(this.jsonObject.toString());
    
            writer.flush();
            writer.close();
            os.close();
            
            int responseCode = conn.getResponseCode();
    
            String response = "";
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response += line;
                }
            }
            else {
                response = null;
            }
            
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return null;
    }
    
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (this.taskListener != null && result != null) {
            this.taskListener.onFinished(result);
        }
    }
    
    interface TaskListener {
        void onFinished(String responseString);
    }
}

class AsyncHTTPGet extends AsyncTask<Void, Void, String> {
    
    private final TaskListener taskListener;
    private final String urlString;
    
    public AsyncHTTPGet(String urlString, TaskListener listener) {
        this.taskListener = listener;
        this.urlString = urlString;
    }
    
    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL(this.urlString);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.connect();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (this.taskListener != null && result != null) {
            this.taskListener.onFinished(result);
        }
    }
    
    interface TaskListener {
        void onFinished(String responseString);
    }
}

