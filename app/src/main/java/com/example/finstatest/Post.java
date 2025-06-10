package com.example.finstatest;

import java.util.List;

public class Post {
    private String username;
    private String imageUrl;
    private String caption;
    private int likesCount;
    private List<String> comments;

    public Post(String username, String imageUrl, String caption, int likesCount, List<String> comments) {
        this.username   = username;
        this.imageUrl   = imageUrl;
        this.caption    = caption;
        this.likesCount = likesCount;
        this.comments   = comments;
    }

    // Getters
    public String getUsername()    { return username; }
    public String getImageUrl()    { return imageUrl; }
    public String getCaption()     { return caption; }
    public int    getLikesCount()  { return likesCount; }
    public List<String> getComments() { return comments; }
}
