package com.example.linkit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.example.linkit.Extras.Utility;
import com.google.firebase.messaging.FirebaseMessagingService;

public class ChatMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        Utility.updateDeviceToken(this, s);
    }
}
