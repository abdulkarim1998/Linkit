package com.example.linkit.Chat;

import android.content.Context;
import android.media.Image;
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
import com.example.linkit.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private Context context;
    private List<ChatListModel> chatListModels;

    public ChatListAdapter(Context context, List<ChatListModel> chatListModels) {
        this.context = context;
        this.chatListModels = chatListModels;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_layout, parent, false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListViewHolder holder, int position) {
        ChatListModel chatListModel = chatListModels.get(position);

        holder.username.setText(chatListModel.getUsername());
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/"+ chatListModel.getPhotoName());
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
        return chatListModels.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayout;
        private TextView username, lastMessage, unreadMessageCount, lastMessageTime;
        private ImageView profile;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.chatList);
            username = itemView.findViewById(R.id.username);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            unreadMessageCount = itemView.findViewById(R.id.unreadMessageCount);
            lastMessageTime = itemView.findViewById(R.id.lastMessageTime);
            profile = itemView.findViewById(R.id.profile);
        }
    }
}