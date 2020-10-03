package com.example.linkit.Chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.linkit.Extras.Constants;
import com.example.linkit.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessagedAdapter extends RecyclerView.Adapter<MessagedAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageModel> messageModels;
    private FirebaseAuth firebaseAuth;
    private ActionMode actionMode;
    private ConstraintLayout selectedView;


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
    public void onBindViewHolder(@NonNull final MessagedAdapter.MessageViewHolder holder, int position) {

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


        holder.constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(actionMode!= null) {
                    return false;
                }

                selectedView = holder.constraintLayout;

                actionMode = ((AppCompatActivity)context).startSupportActionMode(actionModeCallback);
                holder.constraintLayout.setBackgroundColor(context.getResources().getColor(R.color.mr_cast_progressbar_background_light));

                return true;
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

    public ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.message_menu, menu);

            String selectedMessageType= String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE_TYPE));

            if(selectedMessageType.equals(Constants.MESSAGE_TYPE_TEXT)){

                MenuItem itemDownload = menu.findItem(R.id.item_download);
                itemDownload.setVisible(false);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {


            return false;

        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

            String selectedMessageId = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE_ID));
            String selectedMessage = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE));
            String selectedMessageType = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE_TYPE));


            int itemId = menuItem.getItemId();
            switch (itemId)
            {
                case R.id.item_delete:
                    if(context instanceof  ChatActivity)
                    {
                        ((ChatActivity)context).deleteMessage(selectedMessageId, selectedMessageType);
                    }
                    actionMode.finish();
                    break;
                case R.id.item_download:
                    if(context instanceof  ChatActivity)
                    {
                        ((ChatActivity)context).downloadFile(selectedMessageId, selectedMessageType, false);
                    }
                    actionMode.finish();
                    break;
                case R.id.item_share:
                    if(selectedMessageType.equals(Constants.MESSAGE_TYPE_TEXT))
                    {
                        Intent intentForShare = new Intent();
                        intentForShare.setAction(Intent.ACTION_SEND);
                        intentForShare.putExtra(Intent.EXTRA_TEXT, selectedMessage);
                        intentForShare.setType("text/plain");
                        context.startActivity(intentForShare);

                    }
                    else
                    {
                        if(context instanceof  ChatActivity)
                        {
                            ((ChatActivity)context).downloadFile(selectedMessageId, selectedMessageType, true);
                        }
                    }
                    actionMode.finish();
                    break;
                case R.id.item_forward:
                    if(context instanceof ChatActivity)
                    {
                        ((ChatActivity) context).forwardAMessage(selectedMessageId, selectedMessage, selectedMessageType);
                    }
                    actionMode.finish();
                    break;
            }



            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

            actionMode = null;
            selectedView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        }
    };


}
