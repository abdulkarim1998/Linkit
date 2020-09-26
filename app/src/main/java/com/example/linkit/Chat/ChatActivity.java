package com.example.linkit.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.linkit.Node;
import com.example.linkit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ImageView sentBtn;
    private ImageView attachBtn;
    private EditText typingSpace;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    private String currentUserID, chatUserID;

    private int page = 1;
    private static final int MESSAGED_PER_PAGE = 30;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MessagedAdapter adapter;
    private List<MessageModel> messageModels;

    private DatabaseReference databaseReferenceMessaged;
    private ChildEventListener childEventListener;



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

        recyclerView = findViewById(R.id.rvMessages);
        swipeRefreshLayout = findViewById(R.id.rLayout);

        messageModels = new ArrayList<>();
        adapter = new MessagedAdapter(this, messageModels);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadMessages();
        recyclerView.scrollToPosition(messageModels.size()-1);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page++;
                loadMessages();
            }
        });

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

    private void loadMessages()
    {

        messageModels.clear();
        databaseReferenceMessaged = rootRef.child(Node.MESSAGES).child(currentUserID).child(chatUserID);

        Query mQuery = databaseReferenceMessaged.limitToLast(page * MESSAGED_PER_PAGE);

        if(childEventListener != null)
        {

            mQuery.removeEventListener(childEventListener);

            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);

                    messageModels.add(messageModel);
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageModels.size()-1);
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    swipeRefreshLayout.setRefreshing(false);
                }
            };

            mQuery.addChildEventListener(childEventListener);
        }
        else
        {
            Log.i("HERE", "reached");
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);

                    messageModels.add(messageModel);
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageModels.size()-1);
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    swipeRefreshLayout.setRefreshing(false);
                }
            };
            mQuery.addChildEventListener(childEventListener);
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