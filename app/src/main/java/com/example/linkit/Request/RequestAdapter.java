package com.example.linkit.Request;

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

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;

    private List<RequestModel> requestModels;

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

        RequestModel requestModel = requestModels.get(position);

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
