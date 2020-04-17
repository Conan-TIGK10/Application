package com.hero.elias.conanapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WifiHandler {
    
    public static void getPosition(PositionGetListener positionListener) {
        new AsyncHTTPGet("http://3.122.218.59/position", new AsyncHTTPGet.TaskListener() {
            @Override
            public void onFinished(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    positionListener.onFinished(jsonObject.getDouble("x"), jsonObject.getDouble("y"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute();
    }
    
    public static void postPosition(double x, double y) {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
        String date = dateFormat.format(now);
        
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("x", x);
            jsonParam.put("y", y);
            jsonParam.put("read_at", date);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        new AsyncHTTPPost("http://3.122.218.59/position", jsonParam, new AsyncHTTPPost.TaskListener() {
            @Override
            public void onFinished(Integer responseCode) {
                Log.i("MSG", String.valueOf(responseCode));
            }
        }).execute();
    }
    
    public interface PositionGetListener {
        void onFinished(double x, double y);
    }
}
