package com.ocunapse.aplicondo.guard;


import static com.ocunapse.aplicondo.guard.api.RequestBase.LOG;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.auth0.android.jwt.JWT;
import com.ocunapse.aplicondo.guard.api.PushTokenUpdateRequest;


public class GuardApp extends Application {
    private static String appDef = "AppDefault";
    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor editor;

    public class TokenData {
        public int id;
        public int site_id;
        public int profile_id;
        public String full_name;
        public String email;
        public String role;
        public String scopes;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = getApplicationContext().getSharedPreferences(appDef, Context.MODE_PRIVATE);
    }

    public void setPushToken(String pushToken) {
        editor = sharedPref.edit();
        editor.putString("pushToken", pushToken);
        editor.apply();
    }

    public void updatePushToken(String pushToken) {
        if(getToken() == null) return;
        String old = getPushToken();
        boolean replace;
        if(old == null) replace = false;
        else replace = !old.equals(pushToken);
        LOG("-token- O", String.valueOf(old));
        LOG("-token- N", String.valueOf(pushToken));
        new PushTokenUpdateRequest(pushToken, replace, res -> {
            LOG("-token-", "Updated at G-App Success = " + String.valueOf(res.success));
            if(res.success) {
                setPushToken(pushToken);
                LOG("-token-", "Updated at G-App");
            }
        }).execute();
    }

    public String getPushToken() {
        return sharedPref.getString("pushToken", null);
    }

    public TokenData getDecodedToken() {
        String token = sharedPref.getString("accessToken", null);
        if (token == null) return null;
        JWT jwt = new JWT(token);
        TokenData td = new TokenData();
        td.id = jwt.getClaim("id").asInt();
        td.profile_id = jwt.getClaim("profile_id").asInt();
        td.site_id = jwt.getClaim("site_id").asInt();
        td.email = jwt.getClaim("email").asString();
        td.full_name = jwt.getClaim("full_name").asString();
        td.role = jwt.getClaim("role").asString();
        td.scopes = jwt.getClaim("scopes").asString();
        return td;
    }

    public String getToken() {
        return sharedPref.getString("accessToken", null);
    }

    public void setToken(String accessToken) {
        editor = sharedPref.edit();
        editor.putString("accessToken", accessToken);
        editor.apply();
    }

    public int getSite() {
        return sharedPref.getInt("siteId", 0);
    }

    public void setSite(int site) {
        editor = sharedPref.edit();
        editor.putInt("siteId", site);
        editor.apply();
    }


    public void clear() {
        sharedPref.edit().clear().apply();
    }


}
