package com.ocunapse.aplicondo.guard.firebase;

import static com.ocunapse.aplicondo.guard.api.RequestBase.LOG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ocunapse.aplicondo.guard.GuardApp;
import com.ocunapse.aplicondo.guard.HomeActivity;
import com.ocunapse.aplicondo.guard.R;
import com.ocunapse.aplicondo.guard.api.PushTokenUpdateRequest;
import com.ocunapse.aplicondo.guard.ui.dashboard.EmergencyListActivity;
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
        LOG("test--P", String.valueOf(message.toIntent().getExtras()));
        String o = data.get("payload");
        CharSequence name = "SOS";
        String description = "SOS FOR APLICONDO";


        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("APLICONDO_SOS", name, importance);
        channel.setDescription(description);
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.siren_alarm);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        channel.setSound(soundUri, audioAttributes);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        try {
            Payload p = g.fromJson(o, Payload.class);
            Log.e("-token-", p.topic);
            if (p.topic.equals("SOS")) {
                Intent i = new Intent(this, SOSActivity.class);
                i.putExtra("name", p.user_name);
                i.putExtra("unit", p.unit);
                i.putExtra("phone", p.user_phone);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
//                Intent notifyIntent = new Intent(this, EmergencyListActivity.class);
                TaskStackBuilder sb = TaskStackBuilder.create(this);
                sb.addNextIntentWithParentStack(i);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent notifyPendingIntent = sb.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                long[] timings = new long[]{800, 300, 800, 300, 800, 300, 800, 300, 800};
                int[] amplitudes = new int[]{100, 0, 50, 0, 150, 0, 50, 0, 250};
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
                String body = String.format("Emergency Alert: %s from Unit No. %s has triggered an SOS. Immediate attention required!", p.user_name, p.unit);
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this, "APLICONDO_SOS")
                                .setContentTitle(data.get("title"))
                                .setContentText(body)
                                .setSound(soundUri)
                                .setVibrate(new long[]{800, 300, 800, 300, 800, 300, 800, 300, 800})
                                .setContentIntent(notifyPendingIntent)
                                .setSmallIcon(R.mipmap.ic_launcher_round);
                Notification noti = notificationBuilder.build();
                noti.sound = soundUri;
                noti.flags = Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify((int) System.currentTimeMillis(), noti);
            }
        } catch (Exception e) {
            Log.e("erro_log", e.getMessage(), e);
            super.onMessageReceived(message);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        LOG("-token-", token);
        ((GuardApp) getApplication()).updatePushToken(token);
//        super.onNewToken(token);
    }
}