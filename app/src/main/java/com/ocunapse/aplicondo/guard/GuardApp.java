package com.ocunapse.aplicondo.guard;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class GuardApp extends Application {
    private static String appDef = "AppDefault";
    private static SharedPreferences sharedPref ;
    private static SharedPreferences.Editor editor;


    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = getApplicationContext().getSharedPreferences(appDef, Context.MODE_PRIVATE);
    }

    public String getToken() {
        return sharedPref.getString("accessToken", null);
    }

    public void setToken(String accessToken){
        editor = sharedPref.edit();
        editor.putString("accessToken",accessToken);
        editor.apply();
    }

    public int getSite(){
        return sharedPref.getInt("siteId", 0);
    }

    public void setSite(int site){
        editor = sharedPref.edit();
        editor.putInt("siteId",site);
        editor.apply();
    }

    public void clear(){
        sharedPref.edit().clear().apply();
    }



}
