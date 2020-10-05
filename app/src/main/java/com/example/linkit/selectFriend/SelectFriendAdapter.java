package com.example.linkit.selectFriend;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.linkit.Extras.Constants;
import com.example.linkit.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class SelectFriendAdapter extends RecyclerView.Adapter<SelectFriendAdapter.SelectFriendViewHolder> {

    private Context context;
    private List<SelectFriendModel> selectFriendModels;

    public SelectFriendAdapter(Context context, List<SelectFriendModel> selectFriendModels) {
        this.context = context;
        this.selectFriendModels = selectFriendModels;
    }

    @NonNull
    @Override
    public SelectFriendAdapter.SelectFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.select_friend_layout, parent, false);
        return new SelectFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SelectFriendAdapter.SelectFriendViewHolder holder, int position) {

        final SelectFriendModel selectFriendModel = selectFriendModels.get(position);
        holder.username.setText(selectFriendModel.getUsername());

        StorageReference photoRef = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_FOLDER+ "/" + selectFriendModel.getPhoto());

        // fetch the photo
        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(holder.profile);
            }
        });
        
        holder.llSelectFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context instanceof SelectFriendActivity)
                {
                    ((SelectFriendActivity) context)
                            .selectedFriendReturned(selectFriendModel.getUserID(), selectFriendModel.getUsername()
                                    , selectFriendModel.getPhoto()+".jpg");


                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectFriendModels.size();
    }

    public class SelectFriendViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout llSelectFriend;
        private TextView username;
        private ImageView profile;
        private View progressBar;

        public SelectFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            llSelectFriend = itemView.findViewById(R.id.llSelectFriend);
            profile = itemView.findViewById(R.id.profile);
            username = itemView.findViewById(R.id.username);
        }
    }
}
