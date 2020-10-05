package com.example.linkit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.linkit.Extras.Constants;
import com.example.linkit.Extras.Utility;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        Utility.updateDeviceToken(this, s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String message = remoteMessage.getData().get(Constants.NOTIFICATION_MESSAGE);
        String title = remoteMessage.getData().get(Constants.NOTIFICATION_TITLE);

        Intent intent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2020, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notification;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID,
                    Constants.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription(Constants.NOTIFICATION_CHANNEL_DESC);
            notificationManager.createNotificationChannel(notificationChannel);
            notification = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID);
        }
        else
        {
            notification = new NotificationCompat.Builder(this);
        }

        notification.setSmallIcon(R.drawable.ic_linkit);
        notification.setColor(getResources().getColor(R.color.colorPrimary));
        notification.setContentTitle(title);
        notification.setAutoCancel(true);
        notification.setContentIntent(pendingIntent);
        notification.setContentText(message);

        notificationManager.notify(101, notification.build());

    }
}
