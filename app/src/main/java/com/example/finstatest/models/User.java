package com.example.finstatest.models;

// import org.bson.types.ObjectId; // Removed this import
import java.util.Date;
import com.google.gson.annotations.SerializedName;

/**
 * Model class for the User
 */
public class User {
    @SerializedName("id")
    private String id;
    private String username;
    private String passwordHash;
    private String bio;
    private String profileImageUrl;
    private String theme;
    private String backgroundMusicUrl;
    private Date createdAt;

    public User() {}

    public User(String username, String passwordHash, Date createdAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getBackgroundMusicUrl() {
        return backgroundMusicUrl;
    }

    public void setBackgroundMusicUrl(String backgroundMusicUrl) {
        this.backgroundMusicUrl = backgroundMusicUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

