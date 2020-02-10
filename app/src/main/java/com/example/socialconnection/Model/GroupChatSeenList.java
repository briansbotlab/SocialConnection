package com.example.socialconnection.Model;

public class GroupChatSeenList {
    private boolean seen;
    private String id;

    public GroupChatSeenList(){}

    public GroupChatSeenList(boolean seen, String id) {
        this.seen = seen;
        this.id = id;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
