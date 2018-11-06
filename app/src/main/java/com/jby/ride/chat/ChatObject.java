package com.jby.ride.chat;

public class ChatObject {
    private String id, message, username, sendDate, profilePicture, status, sendFrom;

    public ChatObject(String id, String message, String username, String sendDate, String profilePicture, String status, String sendFrom) {
        this.id = id;
        this.message = message;
        this.username = username;
        this.sendDate = sendDate;
        this.profilePicture = profilePicture;
        this.status = status;
        this.sendFrom = sendFrom;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getSendDate() {
        return sendDate;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getStatus() {
        return status;
    }

    public String getSendFrom() {
        return sendFrom;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":\"" + id + '\"' +
                ",\"message\":\"" + message + '\"' +
                ",\"username\":\"" + username + '\"' +
                ",\"sendDate\":\"" + sendDate + '\"' +
                ",\"profilePicture\":\"" + profilePicture + '\"' +
                ",\"status\":\"" + status + '\"' +
                ",\"sendFrom\":\"" + sendFrom + '\"' +
                '}';
    }
}

