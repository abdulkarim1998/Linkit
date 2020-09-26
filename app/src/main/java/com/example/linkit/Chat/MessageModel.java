package com.example.linkit.Chat;

public class MessageModel {
    private String from;
    private String message;
    private String messageID;
    private long messageTime;
    private String messageType;


    public MessageModel() {
    }

    public MessageModel(String from, String message, String messageID, long messageTime, String messageType) {
        this.from = from;
        this.message = message;
        this.messageID = messageID;
        this.messageTime = messageTime;
        this.messageType = messageType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
