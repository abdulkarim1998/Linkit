package com.example.linkit.selectFriend;
// model for selected friend
public class SelectFriendModel {

    private String userID;
    private String username;
    private String photo;

    public SelectFriendModel(String userID, String username, String photo) {
        this.userID = userID;
        this.username = username;
        this.photo = photo;
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
