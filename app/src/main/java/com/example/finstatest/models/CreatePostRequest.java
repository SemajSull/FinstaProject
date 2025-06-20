package com.example.finstatest.models;

import java.util.List;

public class CreatePostRequest {
    private String authorId;
    private String imageUrl;
    private String caption;
    private List<String> tags;

    public CreatePostRequest(String authorId, String imageUrl, String caption, List<String> tags) {
        this.authorId = authorId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.tags = tags;
    }
} 