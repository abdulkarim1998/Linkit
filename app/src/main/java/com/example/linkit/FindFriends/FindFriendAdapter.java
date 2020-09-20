package com.example.linkit.FindFriends;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.linkit.Node;
import com.example.linkit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder> {

    private Context context;
    private List<FindFriendModel> findFriendModels;

    private DatabaseReference databaseReference;
    private FirebaseUser user;

    private String userID;

    public FindFriendAdapter(Context context, List<FindFriendModel> findFriendModels) {
        this.context = context;
        this.findFriendModels = findFriendModels;
    }

    @NonNull
    @Override
    public FindFriendAdapter.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.find_friends_layout,parent,false);

        return new FindFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FindFriendAdapter.FindFriendViewHolder holder, int position) {

        final FindFriendModel f = findFriendModels.get(position);

        holder.username.setText(f.getUsername());
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/"+f.getPhotoID());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(holder.profile);
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference().child(Node.Friend_request);
        user = FirebaseAuth.getInstance().getCurrentUser();

        holder.sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.sendRequest.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);

                userID = f.getUserId();

                databaseReference.child(user.getUid()).child(userID).child(Node.REQUEST_TYPE)
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            databaseReference.child(userID).child(user.getUid()).child(Node.REQUEST_TYPE)
                                    .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(context, "Request sent successfully", Toast.LENGTH_SHORT).show();

                                        holder.sendRequest.setVisibility(View.GONE);
                                        holder.progressBar.setVisibility(View.GONE);
                                        holder.cancelRequest.setVisibility(View.VISIBLE);

                                    }
                                    else
                                    {
                                        Toast.makeText(context, "Request failed, try again", Toast.LENGTH_SHORT).show();

                                        holder.sendRequest.setVisibility(View.VISIBLE);
                                        holder.progressBar.setVisibility(View.GONE);
                                        holder.cancelRequest.setVisibility(View.GONE);

                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(context, "Request failed, try again", Toast.LENGTH_SHORT).show();

                            holder.sendRequest.setVisibility(View.VISIBLE);
                            holder.progressBar.setVisibility(View.GONE);
                            holder.cancelRequest.setVisibility(View.GONE);

                        }
                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return findFriendModels.size();
    }

    public class FindFriendViewHolder extends RecyclerView.ViewHolder {

        private TextView username;
        private ImageView profile;
        private Button sendRequest, cancelRequest;
        private ProgressBar progressBar;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profile);
            username = itemView.findViewById(R.id.username);
            sendRequest = itemView.findViewById(R.id.sendButton);
            cancelRequest = itemView.findViewById(R.id.cancelButton);
            progressBar = itemView.findViewById(R.id.progressBar);


        }
    }
}
