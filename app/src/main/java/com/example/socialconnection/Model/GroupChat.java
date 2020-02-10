package com.example.socialconnection.Model;


public class GroupChat {
    private String id;
    private String roomid;
    private String sender;
    private String sendername;
    private String receiver;
    private String message;
    private int seennum;
    private long senttime;
    private String type;


    public GroupChat(){

    }

    public GroupChat(String id, String sender, String receiver, String message, int seennum, long senttime, String type, String sendername, String roomid) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.seennum = seennum;
        this.senttime = senttime;
        this.type = type;
        this.sendername = sendername;
        this.roomid = roomid;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getSendername() {
        return sendername;
    }

    public void setSendername(String sendername) {
        this.sendername = sendername;
    }

    public int getSeennum() {
        return seennum;
    }

    public void setSeennum(int seennum) {
        this.seennum = seennum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSenttime() {
        return senttime;
    }

    public void setSenttime(long senttime) {
        this.senttime = senttime;
    }
}
