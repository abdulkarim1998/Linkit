package com.example.linkit.Chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

        final MessageModel messageModel = messageModels.get(position);
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        String fromUserID = messageModel.getFrom();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm");
        String dateTime = simpleDateFormat.format(new Date(messageModel.getMessageTime()));
        String[] splitted = dateTime.split(" ");
        String trueTime = splitted[1];

        if(fromUserID.equals(currentUserID))
        {
            if(messageModel.getMessageType().equals(Constants.MESSAGE_TYPE_TEXT))
            {
                holder.llSent.setVisibility(View.VISIBLE);
                holder.llSentImage.setVisibility(View.GONE);
            }
            else {
                holder.llSent.setVisibility(View.GONE);
                holder.llSentImage.setVisibility(View.VISIBLE);
            }

            holder.llReceived.setVisibility(View.GONE);
            holder.llReceivedImage.setVisibility(View.GONE);

            holder.sentMessage.setText(messageModel.getMessage());
            holder.sentTime.setText(trueTime);
            holder.sentImageTime.setText(trueTime);

            Glide.with(context)
                    .load(messageModel.getMessage())
                    .placeholder(R.drawable.ic_default_image)
                    .error(R.drawable.ic_default_image)
                    .into(holder.sentImage);
        }
        else
        {
            if(messageModel.getMessageType().equals(Constants.MESSAGE_TYPE_TEXT))
            {
                holder.llReceived.setVisibility(View.VISIBLE);
                holder.llReceivedImage.setVisibility(View.GONE);
            }
            else {
                holder.llReceived.setVisibility(View.GONE);
                holder.llReceivedImage.setVisibility(View.VISIBLE);
            }

            holder.llSent.setVisibility(View.GONE);
            holder.llSentImage.setVisibility(View.GONE);

            holder.receivedMessage.setText(messageModel.getMessage());
            holder.receiveTime.setText(trueTime);
            holder.receivedImageTime.setText(trueTime);

            Glide.with(context)
                    .load(messageModel.getMessage())
                    .placeholder(R.drawable.ic_default_image)
                    .error(R.drawable.ic_default_image)
                    .into(holder.receivedImage);

        }
        holder.constraintLayout.setTag(R.id.TAG_MESSAGE, messageModel.getMessage());
        holder.constraintLayout.setTag(R.id.TAG_MESSAGE_ID, messageModel.getMessageID());
        holder.constraintLayout.setTag(R.id.TAG_MESSAGE_TYPE, messageModel.getMessageType());

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("nooo", messageModel.getMessageType());
                String msgType = v.getTag(R.id.TAG_MESSAGE_TYPE).toString();
                Uri uri = Uri.parse(v.getTag(R.id.TAG_MESSAGE).toString());
                if(msgType.equals(Constants.MESSAGE_TYPE_VIDEO))
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setDataAndType(uri, "video/mp4");
                    context.startActivity(intent);
                }
                else if(msgType.equals(Constants.MESSAGE_TYPE_IMAGE))
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setDataAndType(uri, "image/jpg");
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout llSent, llReceived, llSentImage, llReceivedImage;
        private TextView sentMessage, receivedMessage, sentTime, receiveTime, sentImageTime, receivedImageTime;
        private ImageView sentImage, receivedImage;
        private ConstraintLayout constraintLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            llSent = itemView.findViewById(R.id.llSent);
            llReceived = itemView.findViewById(R.id.llReceive);
            sentMessage = itemView.findViewById(R.id.messageSent);
            receivedMessage = itemView.findViewById(R.id.messageReceive);
            sentTime = itemView.findViewById(R.id.sentTime);
            receiveTime = itemView.findViewById(R.id.receiveTime);
            llSentImage = itemView.findViewById(R.id.llImageSent);
            llReceivedImage = itemView.findViewById(R.id.llImageReceived);
            sentImageTime = itemView.findViewById(R.id.sentImageTime);
            receivedImageTime = itemView.findViewById(R.id.receivedImageTime);
            sentImage = itemView.findViewById(R.id.sentImage);
            receivedImage = itemView.findViewById(R.id.receivedImage);


            constraintLayout = itemView.findViewById(R.id.msgConstraint);


        }
    }


}
