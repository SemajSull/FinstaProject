package com.example.finstatest.api;

import com.example.finstatest.models.CountResponse;
import com.example.finstatest.models.User;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

/**
 * Retrofit API service interface defining endpoints for the backend server.
 * Each method represents an HTTP call to a specific route on the Node.js API.
 */
public interface ApiService {
    @GET ("/users")
    Call<List<User>> getUsers();

    @GET("/usercount")
    Call<CountResponse> getUserCount();

    @POST("/users")
    Call<User> createUser(@Body User user);

    /**
     * Finds a user by the mongo _id
     */
    @GET("/users/{id}")
    Call<User> getUserById(@Path("id") String id);

    /**
     * Finds a user by username
     */
    @GET("/users/username/{username}")
    Call<User> getUserByUsername(@Path("username") String username);
}
