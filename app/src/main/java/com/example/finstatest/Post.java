package com.example.finstatest;

import java.util.Date;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class Post {
    @SerializedName("_id")
    private String id;
    private String username;
    private String imageUrl;
    private String caption;
    private int likesCount;
    private List<String> comments;
    private Date createdAt;
    private boolean isLiked;
    private String pfpUrl;


    public Post(String username, String imageUrl, String caption, int likesCount, List<String> comments) {
        this.username = username;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.likesCount = likesCount;
        this.comments = comments;
        this.createdAt = new Date();
        this.isLiked = false;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getImageUrl() { return imageUrl; }
    public String getCaption() { return caption; }
    public int getLikesCount() { return likesCount; }
    public List<String> getComments() { return comments; }
    public Date getCreatedAt() { return createdAt; }
    public boolean isLiked() { return isLiked; }

    public String getPfpUrl() { return pfpUrl; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public void setLiked(boolean liked) { isLiked = liked; }
    public void addComment(String comment) { comments.add(comment); }
}
