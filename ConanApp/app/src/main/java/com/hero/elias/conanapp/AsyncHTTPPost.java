package com.hero.elias.conanapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncHTTPPost extends AsyncTask<Void, Void, Integer> {
    
    public interface TaskListener {
        public void onFinished(Integer responseCode);
    }
    
    private final TaskListener taskListener;
    private final String urlString;
    private final JSONObject jsonObject;
    
    public AsyncHTTPPost(String urlString, JSONObject jsonObject, TaskListener listener) {
        this.taskListener = listener;
        this.urlString = urlString;
        this.jsonObject = jsonObject;
    }
    
    @Override
    protected Integer doInBackground(Void... params) {
        try {
            URL url = new URL(this.urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
        
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(this.jsonObject.toString());
        
            os.flush();
            os.close();
        
            //Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            //Log.i("MSG" , conn.getResponseMessage());
            
            int reponseCode = conn.getResponseCode();
        
            conn.disconnect();
            
            return reponseCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 404;
    }
    
    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if(this.taskListener != null && result != null) {
            this.taskListener.onFinished(result);
        }
    }
}
