package com.example.linkit.Chat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkit.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessagedAdapter extends RecyclerView.Adapter<MessagedAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageModel> messageModels;
    private FirebaseAuth firebaseAuth;
    //private ActionMode actionMode;
    public MessagedAdapter(Context context, List<MessageModel> messageModels) {
        this.context = context;
        this.messageModels = messageModels;
    }

    @NonNull
    @Override
    public MessagedAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagedAdapter.MessageViewHolder holder, int position) {

        MessageModel messageModel = messageModels.get(position);
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        String fromUserID = messageModel.getFrom();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm");
        String dateTime = simpleDateFormat.format(new Date(messageModel.getMessageTime()));
        String[] splitted = dateTime.split(" ");
        String trueTime = splitted[1];

        if(fromUserID.equals(currentUserID))
        {
            Log.i("here", messageModel.getFrom());
            holder.llSent.setVisibility(View.VISIBLE);
            holder.llReceived.setVisibility(View.GONE);

            holder.sentMessage.setText(messageModel.getMessage());
            holder.sentTime.setText(trueTime);
        }
        else
        {
            holder.llSent.setVisibility(View.GONE);
            holder.llReceived.setVisibility(View.VISIBLE);

            holder.receivedMessage.setText(messageModel.getMessage());
            holder.receiveTime.setText(trueTime);
        }

    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout llSent, llReceived;
        private TextView sentMessage, receivedMessage, sentTime, receiveTime;
        private ConstraintLayout constraintLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            llSent = itemView.findViewById(R.id.llSent);
            llReceived = itemView.findViewById(R.id.llReceive);
            sentMessage = itemView.findViewById(R.id.messageSent);
            receivedMessage = itemView.findViewById(R.id.messageReceive);
            sentTime = itemView.findViewById(R.id.sentTime);
            receiveTime = itemView.findViewById(R.id.receiveTime);

            constraintLayout = itemView.findViewById(R.id.msgConstraint);


        }
    }


}
