package com.example.socialconnection.Notifications;

public class Token {
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String token;

    public Token(String token) {
        this.token = token;
    }

    public Token(){

    }
}
