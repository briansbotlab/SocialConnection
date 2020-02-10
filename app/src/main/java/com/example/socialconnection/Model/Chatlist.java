package com.example.socialconnection.Model;

public class Chatlist {
    private String id;
    private boolean notify;

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Chatlist(){

    }

    public Chatlist(String id) {
        this.id = id;
    }
}
