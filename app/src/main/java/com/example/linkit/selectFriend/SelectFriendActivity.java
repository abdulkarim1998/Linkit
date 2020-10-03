package com.example.linkit.selectFriend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.service.autofill.Dataset;
import android.view.View;
import android.widget.Toast;

import com.example.linkit.Node;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

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
}