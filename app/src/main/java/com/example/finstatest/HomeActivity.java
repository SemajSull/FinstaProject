package com.example.finstatest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.finstatest.api.ApiService;
import com.example.finstatest.api.ApiServiceInstance;
import com.example.finstatest.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements PostAdapter.OnPostInteractionListener {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    private String loggedInUserId;

    private ApiService apiService;
    private TextView tvNoPostsMessage; // Added for the message

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        apiService = ApiServiceInstance.getService();

        // Get the logged-in user ID from the Intent
        if (getIntent().hasExtra("loggedInUserId")) {
            loggedInUserId = getIntent().getStringExtra("loggedInUserId");
        } else {
            Toast.makeText(this, "User ID not found, please log in again.", Toast.LENGTH_LONG).show();
            // Optionally, redirect to sign-in
            finish();
            return;
        }

        // 1) Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvNoPostsMessage = findViewById(R.id.tvNoPostsMessage); // Initialize the TextView
        // Initially hide the message
        tvNoPostsMessage.setVisibility(View.GONE);

        // 2) Initialize empty post list and attach adapter
        postList = new ArrayList<>();
        adapter = new PostAdapter(postList, this);
        recyclerView.setAdapter(adapter);

        // Fetch posts from followed users
        fetchFollowedPosts();

        // 4) Setup Create Post FAB
        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        fabCreatePost.setOnClickListener(v -> {
            // TODO: Launch CreatePostActivity
            Toast.makeText(this, "Create Post coming soon!", Toast.LENGTH_SHORT).show();
        });

        // 5) Bottom nav handling
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Already here
                return true;
            }
            else if (id == R.id.nav_search) {
                // TODO: launch SearchActivity
                return true;
            }
            else if (id == R.id.nav_profile) {
                Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                profileIntent.putExtra("username", "user"); // TODO: Pass actual username
                profileIntent.putExtra("loggedInUserId", loggedInUserId); // Pass the logged in user ID
                startActivity(profileIntent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void fetchFollowedPosts() {
        if (loggedInUserId == null) {
            Toast.makeText(this, "User ID not available to fetch posts.", Toast.LENGTH_SHORT).show();
            Log.e("HOME_ACTIVITY", "loggedInUserId is null, cannot fetch posts.");
            return;
        }

        if (apiService == null) {
            Toast.makeText(this, "API Service not initialized.", Toast.LENGTH_SHORT).show();
            Log.e("HOME_ACTIVITY", "ApiService is null, cannot fetch posts.");
            return;
        }

        Log.d("HOME_ACTIVITY", "Attempting to fetch posts for userId: " + loggedInUserId);
        apiService.getFollowedPosts(loggedInUserId).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                Log.d("HOME_ACTIVITY_API", "API Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("HOME_ACTIVITY_API", "Response successful. Body size: " + response.body().size());
                    postList.clear();
                    postList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    Log.d("HOME_ACTIVITY_DATA", "Post list size after update: " + postList.size());

                    // Show/hide message based on whether there are posts
                    if (postList.isEmpty()) {
                        tvNoPostsMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        Log.d("HOME_ACTIVITY_UI", "Displaying no posts message.");
                    } else {
                        tvNoPostsMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        Log.d("HOME_ACTIVITY_UI", "Displaying posts.");
                    }

                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load posts: " + response.message(), Toast.LENGTH_SHORT).show();
                    tvNoPostsMessage.setVisibility(View.VISIBLE); // Show message on failure
                    recyclerView.setVisibility(View.GONE);
                    Log.e("HOME_ACTIVITY_API", "Failed to fetch posts. Response: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network error fetching posts: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvNoPostsMessage.setVisibility(View.VISIBLE); // Show message on network error
                recyclerView.setVisibility(View.GONE);
                Log.e("HOME_ACTIVITY_API", "Network error: " + t.getMessage(), t);
            }
        });
    }

    @Override
    public void onLikeClick(Post post, int position) {
        // Toggle like state
        post.setLiked(!post.isLiked());
        post.setLikesCount(post.getLikesCount() + (post.isLiked() ? 1 : -1));
        adapter.notifyItemChanged(position);
        
        // TODO: Update like in backend
    }

    @Override
    public void onCommentClick(Post post, int position) {
        // Show comment dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_comment, null);
        EditText etCommentInput = dialogView.findViewById(R.id.etCommentInput);

        builder.setView(dialogView)
                .setTitle("Add a Comment")
                .setPositiveButton("Add", (dialog, which) -> {
                    String commentText = etCommentInput.getText().toString().trim();
                    if (!commentText.isEmpty()) {
                        // TODO: Replace "current_user" with actual authenticated username
                        String fullComment = "current_user: " + commentText;
                        post.addComment(fullComment);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show();
                        // TODO: Update comment in backend
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }
}
