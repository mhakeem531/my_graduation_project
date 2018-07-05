package com.example.hakeem.demo.helper;

public class Feedback {

    private long feedbackId;
    private String name;
    private String date;
    private String feedbackText;
    private String attachedPhotoUrl;

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    private String profilePhotoUrl;

    private byte[] profileImage;

    public byte[] getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }

    public Feedback(long feedbackId, String name, String date, String feedbackText, String attachedPhotoUrl, byte[] profileImage) {
        this.feedbackId = feedbackId;
        this.name = name;
        this.date = date;
        this.feedbackText = feedbackText;
        this.attachedPhotoUrl = attachedPhotoUrl;
        this.profileImage = profileImage;
    }

    public Feedback(long feedbackId, String name, String date, String feedbackText, String attachedPhotoUrl, String profileImage) {
        this.feedbackId = feedbackId;
        this.name = name;
        this.date = date;
        this.feedbackText = feedbackText;
        this.attachedPhotoUrl = attachedPhotoUrl;
        this.profilePhotoUrl = profileImage;
    }


    public void setFeedbackId(long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    public void setAttachedPhotoUrl(String attachedPhotoUrl) {
        this.attachedPhotoUrl = attachedPhotoUrl;
    }


    public long getFeedbackId() {
        return feedbackId;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public String getAttachedPhotoUrl() {
        return attachedPhotoUrl;
    }
}
