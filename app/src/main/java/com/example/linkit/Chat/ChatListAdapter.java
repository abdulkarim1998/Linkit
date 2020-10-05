package com.example.linkit.Chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.linkit.Extras.Constants;
import com.example.linkit.Extras.Extras;
import com.example.linkit.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
// this adaptor connect recycler View of chat fragment with all chats
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private Context context;
    private List<ChatListModel> chatListModels;
    private static ArrayList<String> listt;

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
        final ChatListModel chatListModel = chatListModels.get(position);

        holder.username.setText(chatListModel.getUsername());
        StorageReference photoRef = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_FOLDER + "/" + chatListModel.getUserId()+".jpg");
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

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(Extras.USER_KEY, chatListModel.getUserId());
                intent.putExtra(Extras.USER_NAME, chatListModel.getUsername());
                intent.putExtra(Extras.USER_PHOTO, chatListModel.getPhotoName());
                context.startActivity(intent);
            }
        });
        listt = new ArrayList<String>();
        for(int i = 0; i<chatListModels.size(); i++)
        {
            listt.add(chatListModels.get(i).getUsername());
        }

    }
    // to search for a contact
    public static ArrayList<String> searchUsername(ArrayList<String> list){

        for(int i = 0; i<listt.size(); i++)
        {
            list.add(listt.get(i));
            Log.i("asd", listt.get(i));
        }

        return list;
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
            profile = itemView.findViewById(R.id.profile);
        }
    }
}
