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
import java.nio.charset.StandardCharsets;
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
    
    private final ArrayList<WifiCallback> wifiCallback;
    
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
    
    public void getLastPosition(final PositionGetListener getListener) {
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
    
    public void postPosition(final double x, final double y, final PositionPostListener postListener) {
        if (!this.sessionSet){ return; }
        
        final Date now = new Date();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.GERMANY);
        final String date = dateFormat.format(now);
        
        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("x", x);
            jsonParam.put("y", y);
            jsonParam.put("sessionId", this.sessionId);
            jsonParam.put("read_at", date);
        } catch (final JSONException e) {
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
    
    public void postCollision(final double x, final double y, final CollisionPostListener collisionListener){
        if (!this.sessionSet){ return; }
    
        this.postPosition(x, y, (id) -> {
            final JSONObject jsonParam = new JSONObject();
            try {
                jsonParam.put("positionId", id);
            } catch (final JSONException e) {
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
    
    public void createSession(final String sessionName, final SessionCreateListener createListener){
        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("name", sessionName);
        } catch (final JSONException e) {
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
            this.mainActivity.registerReceiver(this, new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION));
            this.mainActivity.registerReceiver(this, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            this.mainActivity.registerReceiver(this, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
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
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            
            switch (state){
                case WifiManager.WIFI_STATE_ENABLED:
                    this.updateState(WifiInState.CONNECTED);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    this.updateState(WifiInState.DISCONNECTED);
                    break;
            }
        }
    }
    
    public void checkConnection(){
        if (this.mainActivity != null){
            final WifiManager wifiMgr = (WifiManager) this.mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiMgr.isWifiEnabled()) {
                final WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.mainActivity);
        
        builder.setTitle("Wifi")
                .setMessage("In order to Send data to the Backend Database, Wifi must be Turned on")
                .setPositiveButton("OK", null);
        final AlertDialog dialog = builder.create();
        
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
    
    private void updateState(final WifiInState newState) {
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
    
    public AsyncHTTPPost(final String urlString, final JSONObject jsonObject, final TaskListener listener) {
        this.taskListener = listener;
        this.urlString = urlString;
        this.jsonObject = jsonObject;
    }
    
    @Override
    protected String doInBackground(final Void... params) {
        try {
            final URL url = new URL(this.urlString);
    
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);
    
            final OutputStream os = conn.getOutputStream();
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(this.jsonObject.toString());
    
            writer.flush();
            writer.close();
            os.close();
            
            final int responseCode = conn.getResponseCode();
    
            String response = "";
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                final BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response += line;
                }
            }
            else {
                response = null;
            }
            
            return response;
            
        } catch (final Exception e) {
            e.printStackTrace();
        }
    
        return null;
    }
    
    @Override
    protected void onPostExecute(final String result) {
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
    
    public AsyncHTTPGet(final String urlString, final TaskListener listener) {
        this.taskListener = listener;
        this.urlString = urlString;
    }
    
    @Override
    protected String doInBackground(final Void... params) {
        try {
            final URL url = new URL(this.urlString);
            
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.connect();
            
            final BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            final StringBuilder sb = new StringBuilder();
            
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            
            return sb.toString();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override
    protected void onPostExecute(final String result) {
        super.onPostExecute(result);
        if (this.taskListener != null && result != null) {
            this.taskListener.onFinished(result);
        }
    }
    
    interface TaskListener {
        void onFinished(String responseString);
    }
}

