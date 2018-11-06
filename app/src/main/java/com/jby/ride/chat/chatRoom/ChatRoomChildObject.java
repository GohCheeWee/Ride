package com.jby.ride.chat.chatRoom;

public class ChatRoomChildObject {
    private String id, senderID, senderName, senderProfilePicture, message, status, sendFrom, sendDate;

    public ChatRoomChildObject(String id, String senderID, String senderName, String senderProfilePicture, String message, String status, String sendFrom, String sendDate) {
        this.id = id;
        this.senderID = senderID;
        this.senderName = senderName;
        this.senderProfilePicture = senderProfilePicture;
        this.message = message;
        this.status = status;
        this.sendFrom = sendFrom;
        this.sendDate = sendDate;
    }

    public String getId() {
        return id;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderProfilePicture() {
        return senderProfilePicture;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getSendFrom() {
        return sendFrom;
    }

    public String getSendDate() {
        return sendDate;
    }

}
