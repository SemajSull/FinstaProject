package com.example.finstatest.models;

/**
 * Wrapper class for the /usercount endpoint response
 */
public class CountResponse {
    int count;

    public CountResponse (int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
