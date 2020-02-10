package com.example.socialconnection.Model;


public class Chat {
    private String id;
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private long senttime;
    private String type;

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
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

    public Chat(){

    }

    public Chat( String id, String sender, String receiver, String message, boolean isseen, long senttime, String type) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.senttime = senttime;
        this.type = type;
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
}
