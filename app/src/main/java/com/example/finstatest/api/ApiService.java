package com.example.finstatest.api;

import com.example.finstatest.models.CountResponse;
import com.example.finstatest.models.User;
import com.example.finstatest.models.SignInRequest;
import com.example.finstatest.Post;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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

    @POST("/signin")
    Call<Void> signInUser(@Body SignInRequest signInRequest);

    @GET("/posts/followed/{userId}")
    Call<List<Post>> getFollowedPosts(@Path("userId") String userId);

    @POST("/users/{followerId}/follow/{followeeId}")
    Call<Void> followUser(@Path("followerId") String followerId, @Path("followeeId") String followeeId);

    @GET("/users/search/{query}")
    Call<List<User>> searchUsers(@Path("query") String query);

    @GET("posts/user/{userId}")
    Call<List<Post>> getUserPosts(@Path("userId") String userId);

    @GET("users/{userId}")
    Call<User> getUser(@Path("userId") String userId);

    @GET("follows/check/{followerId}/{followeeId}")
    Call<Boolean> checkFollowStatus(@Path("followerId") String followerId, @Path("followeeId") String followeeId);

    @DELETE("follows/{followerId}/{followeeId}")
    Call<Void> unfollowUser(@Path("followerId") String followerId, @Path("followeeId") String followeeId);

    @Multipart
    @POST("posts")
    Call<Void> createPost(
        @Part MultipartBody.Part image,
        @Part("caption") RequestBody caption,
        @Part("tags") RequestBody tags,
        @Part("authorId") RequestBody authorId
    );
}
