package com.hero.elias.conanapp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WifiHandler {
    
    private static MainActivity mainActivity;
    private static int sessionId;
    private static boolean sessionSet = false;
    
    public static void getLastPosition(PositionGetListener getListener) {
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
    
    public static void postPosition(double x, double y, PositionPostListener postListener) {
        if (!sessionSet){ return; }
        
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
        String date = dateFormat.format(now);
        
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("x", x);
            jsonParam.put("y", y);
            jsonParam.put("sessionId", sessionId);
            jsonParam.put("read_at", date);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        new AsyncHTTPPost("http://3.122.218.59/api/position", jsonParam, responseString -> {
            Log.i("WIFI", String.valueOf(responseString));
    
            try {
                JSONArray jsonArray = new JSONArray(responseString);
                if (jsonArray.length() > 0){
                    JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length()-1);
                    postListener.onFinished(jsonObject.getInt("id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).execute();
    }
    interface PositionPostListener {
        void onFinished(int posId);
    }
    
    public static void postCollision(double x, double y, CollisionPostListener collisionListener){
        if (!sessionSet){ return; }
        
        postPosition(x, y, (id) -> {
            JSONObject jsonParam = new JSONObject();
            try {
                jsonParam.put("positionId", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
    
            new AsyncHTTPPost("http://3.122.218.59/api/collision", jsonParam, responseString -> {
                Log.i("WIFI", String.valueOf(responseString));
                collisionListener.onFinished();
            }).execute();
        });
    }
    
    interface CollisionPostListener {
        void onFinished();
    }
    
    public static void createSession(String sessionName, SessionCreateListener createListener){
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("name", sessionName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    
        new AsyncHTTPPost("http://3.122.218.59/api/session", jsonParam, responseString -> {
            Log.i("WIFI", String.valueOf(responseString));
            try {
                JSONArray jsonArray = new JSONArray(responseString);
                if (jsonArray.length() > 0){
                    JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length()-1);
                    Log.i("WIFI", String.valueOf(jsonObject.getInt("id")));
                    sessionId = jsonObject.getInt("id");
                    sessionSet = true;
                    createListener.onFinished();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).execute();
    }
    
    interface SessionCreateListener {
        void onFinished();
    }
    
    public static void setMainActivity(MainActivity ma) {
        mainActivity = ma;
    }
    
    private boolean checkConnection(){
        WifiManager wifiMgr = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    
        if (wifiMgr.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if( wifiInfo.getNetworkId() == -1 ){
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
    
                builder.setMessage("Wifi")
                        .setTitle("In order to Send data to the Backend Database Wifi must be Turned on, Please turn on.");
                AlertDialog dialog = builder.create();
    
                dialog.show();
                return false;
            }
            return true;
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
    
            builder.setMessage("Wifi")
                    .setTitle("In order to Send data to the Backend Database Wifi must be Turned on, Please turn on.");
            AlertDialog dialog = builder.create();
    
            dialog.show();
            return false;
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
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(this.jsonObject.toString());
            os.flush();
            os.close();
    
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

