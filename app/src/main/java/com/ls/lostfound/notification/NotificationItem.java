package com.ls.lostfound.notification;

import com.google.firebase.Timestamp;

public class NotificationItem {

    private String userId;
    private Timestamp timestamp;
    private String title;
    private String message;

    // Empty constructor for Firebase
    public NotificationItem() {
    }

    // Getters and setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() { // Return type changed to 'Timestamp'
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) { // Parameter type changed to 'Timestamp'
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
