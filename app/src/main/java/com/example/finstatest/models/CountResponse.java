package com.example.finstatest.models;

/**
 * Wrapper class for the /usercount endpoint response
 */
public class CountResponse {
    private int postCount;
    private int followerCount;
    private int followingCount;

    public CountResponse(int postCount, int followerCount, int followingCount) {
        this.postCount = postCount;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }
}
