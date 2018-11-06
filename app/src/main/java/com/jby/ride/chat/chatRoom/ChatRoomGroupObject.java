package com.jby.ride.chat.chatRoom;

import java.util.ArrayList;

public class ChatRoomGroupObject {
    private String id, senderID, senderName, senderProfilePicture, message, status, sendFrom, sendDate;
    private ArrayList<ChatRoomChildObject> chatRoomChildObjectArrayList;

    //for api purpose
    public ChatRoomGroupObject(String id, String senderID, String senderName, String senderProfilePicture, String message, String status, String sendFrom, String sendDate) {
        this.id = id;
        this.senderID = senderID;
        this.senderName = senderName;
        this.senderProfilePicture = senderProfilePicture;
        this.message = message;
        this.status = status;
        this.sendFrom = sendFrom;
        this.sendDate = sendDate;
    }

    public ChatRoomGroupObject(String sendDate, ArrayList<ChatRoomChildObject> chatRoomChildObjectArrayList) {
        this.sendDate = sendDate;
        this.chatRoomChildObjectArrayList = chatRoomChildObjectArrayList;
    }


    public ArrayList<ChatRoomChildObject> getChatRoomChildObjectArrayList() {
        return chatRoomChildObjectArrayList;
    }

    public String getSendDate() {
        return sendDate;
    }

    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", senderID:'" + senderID + '\'' +
                ", senderName:'" + senderName + '\'' +
                ", senderProfilePicture:'" + senderProfilePicture + '\'' +
                ", message:'" + message + '\'' +
                ", status:'" + status + '\'' +
                ", sendFrom:'" + sendFrom + '\'' +
                ", sendDate:'" + sendDate + '\'' +
                '}';
    }
}
