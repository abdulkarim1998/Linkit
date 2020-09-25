package com.example.linkit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.internal.Util;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private ImageView sentBtn;
    private ImageView attachBtn;
    private EditText typingSpace;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    private String currentUserID, chatUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        typingSpace = findViewById(R.id.typingSpace);
        sentBtn = findViewById(R.id.sendingBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        if(getIntent().hasExtra("user_id"))
        {
            chatUserID = getIntent().getStringExtra("user_id");
        }


    }

    public void sendMessage(View view)
    {
        if(haveNetworkConnection()) {
            DatabaseReference userMessage = rootRef.child(Node.MESSAGE).child(currentUserID).child(chatUserID).push();
            String msgID = userMessage.getKey();
            createMessage(typingSpace.getText().toString().trim(), "text", msgID);
        }
        else
        {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void createMessage(String msg, String msgType, String msgID) {
        try {


            if (!msg.equals("")) {
                HashMap hashMap = new HashMap<>();
                hashMap.put(Node.MESSAGE_ID, msgID);
                hashMap.put(Node.MESSAGE, msg);
                hashMap.put(Node.MESSAGE_TYPE, msgType);
                hashMap.put(Node.MESSAGE_FROM, currentUserID);
                hashMap.put(Node.MESSAGE_TIME, ServerValue.TIMESTAMP);

                String currentUserRef = Node.MESSAGES + "/" + currentUserID + "/" + chatUserID;
                String chatUserRef = Node.MESSAGES + "/" + chatUserID + "/" + currentUserID;

                HashMap msgMap = new HashMap();

                msgMap.put(currentUserRef + "/" + msgID, hashMap);
                msgMap.put(chatUserRef + "/" + msgID, hashMap);

                typingSpace.setText("");

                rootRef.updateChildren(msgMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Toast.makeText(ChatActivity.this, "failed to send a message", Toast.LENGTH_SHORT).show();
                        } else {

                        }
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(ChatActivity.this, "failed to send a message", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}