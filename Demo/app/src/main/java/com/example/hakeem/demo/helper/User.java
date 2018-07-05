package com.example.hakeem.demo.helper;

/**
 * Created by hakeem on 4/9/18.
 */

public class User {

    private String userName;
    private String email;
    private String displayedName;
    private String id;
    private String createdAt;
    private byte[] profileImage;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDisplayedName(String displayedName) {
        this.displayedName = displayedName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }


    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayedName() {
        return displayedName;
    }

    public String getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public byte[] getProfileImage() {
        return profileImage;
    }
}
