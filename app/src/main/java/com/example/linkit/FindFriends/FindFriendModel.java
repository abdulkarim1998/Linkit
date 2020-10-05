package com.example.linkit.FindFriends;
// model for contacts
public class FindFriendModel {

    private String username;
    private String photoID;
    private String userId;
    private boolean requestStatus;

    public FindFriendModel(String username, String photoID, String userId, boolean requestStatus) {
        this.username = username;
        this.photoID = photoID;
        this.userId = userId;
        this.requestStatus = requestStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoID() {
        return photoID;
    }

    public void setPhotoID(String photoID) {
        this.photoID = photoID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(boolean requestStatus) {
        this.requestStatus = requestStatus;
    }
}
