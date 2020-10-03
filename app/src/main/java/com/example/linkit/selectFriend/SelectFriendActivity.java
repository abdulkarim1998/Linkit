package com.example.linkit.selectFriend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.linkit.Extras.Extras;
import com.example.linkit.Extras.Node;
import com.example.linkit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SelectFriendAdapter adapter;
    private List<SelectFriendModel> selectFriendModels;

    private View progressBar;

    private DatabaseReference databaseReferenceUsers, databaseReferenceChats;

    private FirebaseUser user;


    private ValueEventListener valueEventListener;

    private String selectedMessage, selectedMessageId, selectedMessageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);


        if(getIntent().hasExtra(Extras.MESSAGE))
        {
            selectedMessage = getIntent().getStringExtra(Extras.MESSAGE);
            selectedMessageId = getIntent().getStringExtra(Extras.MESSAGE_ID);
            selectedMessageType = getIntent().getStringExtra(Extras.MESSAGE_TYPE);
        }

        recyclerView = findViewById(R.id.rvSelectFriend);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        selectFriendModels = new ArrayList<>();
        adapter = new SelectFriendAdapter(this, selectFriendModels);

        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(Node.USERS);
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(Node.CHATS).child(user.getUid());

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    final String userID = ds.getKey();
                    databaseReferenceUsers.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String username = dataSnapshot.child(Node.NICKNAME).getValue() != null?
                                    dataSnapshot.child(Node.NICKNAME).getValue().toString() : "";

                            SelectFriendModel selectFriendModel = new SelectFriendModel(userID, username, userID+".jpg");
                            selectFriendModels.add(selectFriendModel);
                            adapter.notifyDataSetChanged();

                            progressBar.setVisibility(View.GONE);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(SelectFriendActivity.this, "failed to fetch friends", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SelectFriendActivity.this, "failed to fetch friends", Toast.LENGTH_SHORT).show();
            }
        };

        databaseReferenceChats.addValueEventListener(valueEventListener);
    }

    public void selectedFriendReturned(String userID, String username, String photo)
    {
        databaseReferenceChats.removeEventListener(valueEventListener);
        Intent intent = new Intent();

        intent.putExtra(Extras.USER_KEY, userID);
        intent.putExtra(Extras.USER_NAME, username);
        intent.putExtra(Extras.USER_PHOTO, photo);

        intent.putExtra(Extras.MESSAGE, selectedMessage);
        intent.putExtra(Extras.MESSAGE_ID, selectedMessageId);
        intent.putExtra(Extras.MESSAGE_TYPE, selectedMessageType);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}