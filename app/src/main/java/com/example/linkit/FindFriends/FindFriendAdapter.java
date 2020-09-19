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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.linkit.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder> {

    private Context context;
    private List<FindFriendModel> findFriendModels;

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

        FindFriendModel f = findFriendModels.get(position);

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
