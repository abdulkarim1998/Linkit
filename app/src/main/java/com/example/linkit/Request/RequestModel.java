package com.example.linkit.Request;
// the model of the requests
public class RequestModel {
    private String userID;
    private String username;
    private String photoFileName;

    public RequestModel(String userID, String username, String photoFileName) {
        this.userID = userID;
        this.username = username;
        this.photoFileName = photoFileName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoFileName() {
        return photoFileName;
    }

    public void setPhotoFileName(String photoFileName) {
        this.photoFileName = photoFileName;
    }
}
