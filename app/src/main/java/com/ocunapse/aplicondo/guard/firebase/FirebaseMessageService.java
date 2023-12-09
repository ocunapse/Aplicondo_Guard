package com.ocunapse.aplicondo.guard.firebase;

import static com.ocunapse.aplicondo.guard.api.RequestBase.LOG;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ocunapse.aplicondo.guard.GuardApp;
import com.ocunapse.aplicondo.guard.HomeActivity;
import com.ocunapse.aplicondo.guard.api.PushTokenUpdateRequest;
import com.ocunapse.aplicondo.guard.ui.dashboard.SOSActivity;

import java.util.Map;
import java.util.Objects;

public class FirebaseMessageService extends FirebaseMessagingService {


    public static Gson g = new GsonBuilder().create();
    public FirebaseMessageService() {
    }
    public class Payload {
        String topic;
        int user_id;
        int sos_id;
        String user_phone;
        String user_name;
        String unit;
        String owners_name;
        String owners_phone;
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Log.e("-token-", "From: " + message.getFrom());
        Log.e("-token-", "Message Notification Body: " + message.getData());

        Map<String, String> data = message.getData();
        String o = data.get("payload");
        Payload p = g.fromJson(o, Payload.class);
        Log.e("-token-", p.topic);
        if(p.topic.equals("SOS")){
            Intent i = new Intent(this, SOSActivity.class);
            i.putExtra("name",p.user_name);
            i.putExtra("unit",p.unit);
            i.putExtra("phone",p.user_phone);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

//        super.onMessageReceived(message);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        LOG("-token-", token);
        ((GuardApp)getApplication()).updatePushToken(token);
//        super.onNewToken(token);
    }
}