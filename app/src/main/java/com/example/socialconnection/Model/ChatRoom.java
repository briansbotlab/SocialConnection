package com.example.socialconnection.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoom {
    private String id;
    private String chatRoomName;
    private String manager;
    private String password;
    private String imageURL;
    private String secret_status;
    private String status;
    private String search;

    public ChatRoom(){

    }

    public ChatRoom(String id, String chatRoomName, String manager, String password, String imageURL, String status, String search, String secret_status) {
        this.id = id;
        this.chatRoomName = chatRoomName;
        this.manager = manager;
        this.password = password;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
        this.secret_status = secret_status;
    }

    public String getSecret_status() {
        return secret_status;
    }

    public void setSecret_status(String secret_status) {
        this.secret_status = secret_status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getChatRoomName() {
        return chatRoomName;
    }

    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
