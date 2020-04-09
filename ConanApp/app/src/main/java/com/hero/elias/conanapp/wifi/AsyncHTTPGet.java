package com.hero.elias.conanapp.wifi;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncHTTPGet extends AsyncTask<Void, Void, String> {
    
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
            
            String string = sb.toString();
            
            return string;
            
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
    
    public interface TaskListener {
        public void onFinished(String jsonObject);
    }
}

