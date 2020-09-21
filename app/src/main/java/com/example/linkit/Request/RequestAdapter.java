package com.example.linkit.Request;

import android.content.Context;
import android.net.Uri;
import android.provider.SyncStateContract;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.core.Constants;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;

    private List<RequestModel> requestModels;
    private DatabaseReference databaseReferenceFriendRequest, databaseReferenceChat;
    private FirebaseUser currentUser;

    public RequestAdapter(Context context, List<RequestModel> requestModels) {
        this.context = context;
        this.requestModels = requestModels;
    }

    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_layout, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestAdapter.RequestViewHolder holder, int position) {

        final RequestModel requestModel = requestModels.get(position);

        holder.username.setText(requestModel.getUsername());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/"+ requestModel.getPhotoFileName());

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

        databaseReferenceFriendRequest = FirebaseDatabase.getInstance().getReference().child(Node.FRIEND_REQUEST);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        /*databaseReferenceChat = FirebaseDatabase.getInstance().getReference().child(Node.CHATS);

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.declineButton.setVisibility(view.GONE);
                holder.acceptButton.setVisibility(view.GONE);

                databaseReferenceFriendRequest.child(currentUser.getUid()).child(requestModel.getUserID()).child(Node.TIME_STAMP).setValue(ServerValue.TIMESTAMP);
                databaseReferenceFriendRequest.child(requestModel.getUserID()).child(currentUser.getUid()).child(Node.TIME_STAMP).setValue(ServerValue.TIMESTAMP);

                databaseReferenceFriendRequest.child(currentUser.getUid()).child(requestModel.getUserID()).child(Node.REQUEST_TYPE).setValue();
                databaseReferenceFriendRequest.child(requestModel.getUserID()).child(currentUser.getUid()).child(Node.REQUEST_TYPE).setValue();
            }
        });*/
        holder.declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                holder.progressBar.setVisibility(View.VISIBLE);
                holder.declineButton.setVisibility(view.GONE);
                holder.acceptButton.setVisibility(view.GONE);

                databaseReferenceFriendRequest.child(currentUser.getUid()).child(requestModel.getUserID()).child(Node.REQUEST_TYPE)
                        .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            databaseReferenceFriendRequest.child(requestModel.getUserID()).child(currentUser.getUid()).child(Node.REQUEST_TYPE)
                                    .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(context, context.getString(R.string.request_declined_successfully), Toast.LENGTH_SHORT).show();
                                        holder.progressBar.setVisibility(view.GONE );
                                        holder.declineButton.setVisibility(view.VISIBLE);
                                        holder.acceptButton.setVisibility(view.VISIBLE);
                                    }
                                    else {
                                        Toast.makeText(context, context.getString(R.string.fail_to_decline), Toast.LENGTH_SHORT).show();
                                        holder.progressBar.setVisibility(view.GONE );
                                        holder.declineButton.setVisibility(view.VISIBLE);
                                        holder.acceptButton.setVisibility(view.VISIBLE);
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(context, context.getString(R.string.fail_to_decline), Toast.LENGTH_SHORT).show();
                            holder.progressBar.setVisibility(view.GONE );
                            holder.declineButton.setVisibility(view.VISIBLE);
                            holder.acceptButton.setVisibility(view.VISIBLE);
                        }

                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return requestModels.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView username;
        private ImageView profile;
        private Button acceptButton, declineButton;
        private ProgressBar progressBar;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile = itemView.findViewById(R.id.profile);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
            progressBar = itemView.findViewById(R.id.progressBar);

        }
    }
}
