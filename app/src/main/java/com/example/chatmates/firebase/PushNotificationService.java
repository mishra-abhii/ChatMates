package com.example.chatmates.firebase;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.chatmates.ChatActivity;
import com.example.chatmates.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.Random;

public class PushNotificationService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
//        Log.d("FCM", "Token : " + token);
    }

    @SuppressLint({"NewApi", "UnspecifiedImmutableFlag"})
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

//        Log.d("FCM", "Message : " + remoteMessage.getNotification().getBody());

        SharedPreferences getSharedPreferences=getSharedPreferences("stateData",MODE_PRIVATE);
        String state = getSharedPreferences.getString("userState","user+_state");

        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();
        String CHANNEL_ID = "MESSAGE";
        PendingIntent pendingIntent = null;
        
        if(Objects.equals(state, "offline")){
            Intent intent = new Intent(this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        }
        

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Chat Notification",
                NotificationManager.IMPORTANCE_HIGH);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notifications)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat.from(this).notify(1, notification.build());

        super.onMessageReceived(remoteMessage);

    }

}
