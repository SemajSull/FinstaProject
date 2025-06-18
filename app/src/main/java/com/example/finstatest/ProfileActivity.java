package com.example.finstatest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.finstatest.api.ApiService;
import com.example.finstatest.api.ApiServiceInstance;
import com.example.finstatest.models.User;
import com.example.finstatest.Post;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements PostAdapter.OnPostInteractionListener {
    private static final String TAG = "ProfileActivity";
    private ImageView profileImage;
    private TextView usernameText;
    private EditText bioText;
    private TextView postsCount;
    private TextView followersCount;
    private TextView followingCount;
    private RecyclerView postsGrid;
    private Button editProfileButton;
    private Button changeThemeButton;
    private Button changeMusicButton;
    private Button followButton;
    private PostAdapter postAdapter;
    private List<Post> userPosts;
    private String currentUsername;
    private String currentUserId;
    private String loggedInUserId;
    private Uri currentProfileImage;
    private String currentBio = "";
    private String currentTheme = "default";
    private String currentMusic = "";

    private ApiService apiService;
    private TextView tvNoPostsMessage;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    currentProfileImage = uri;
                    profileImage.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        apiService = ApiServiceInstance.getService();

        // Get username and user ID from intent
        currentUserId = getIntent().getStringExtra("userId");
        loggedInUserId = getIntent().getStringExtra("loggedInUserId");

        if (currentUserId == null || loggedInUserId == null) {
            Toast.makeText(this, "User information not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupBottomNavigation();
        setupClickListeners();

        // Fetch user profile
        fetchUserProfile();

        // Fetch user posts
        fetchUserPosts();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        usernameText = findViewById(R.id.usernameText);
        bioText = findViewById(R.id.bioText);
        postsCount = findViewById(R.id.postsCount);
        followersCount = findViewById(R.id.followersCount);
        followingCount = findViewById(R.id.followingCount);
        editProfileButton = findViewById(R.id.editProfileButton);
        changeThemeButton = findViewById(R.id.changeThemeButton);
        changeMusicButton = findViewById(R.id.changeMusicButton);
        followButton = findViewById(R.id.followButton);

        // Setup posts grid
        postsGrid = findViewById(R.id.postsGrid);
        postsGrid.setLayoutManager(new GridLayoutManager(this, 3));
        userPosts = new ArrayList<>();
        postAdapter = new PostAdapter(userPosts, this);
        postsGrid.setAdapter(postAdapter);

        // Show follow button only if viewing another user's profile
        if (!currentUserId.equals(loggedInUserId)) {
            followButton.setVisibility(View.VISIBLE);
            editProfileButton.setVisibility(View.GONE);
            changeThemeButton.setVisibility(View.GONE);
            changeMusicButton.setVisibility(View.GONE);
        } else {
            followButton.setVisibility(View.GONE);
            editProfileButton.setVisibility(View.VISIBLE);
            changeThemeButton.setVisibility(View.VISIBLE);
            changeMusicButton.setVisibility(View.VISIBLE);
        }

        // Initialize no posts message
        tvNoPostsMessage = findViewById(R.id.tvNoPostsMessage);
        tvNoPostsMessage.setVisibility(View.GONE);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent homeIntent = new Intent(this, HomeActivity.class);
                homeIntent.putExtra("loggedInUserId", loggedInUserId);
                startActivity(homeIntent);
                finish();
                return true;
            } else if (id == R.id.nav_search) {
                Intent searchIntent = new Intent(this, SearchActivity.class);
                searchIntent.putExtra("loggedInUserId", loggedInUserId);
                startActivity(searchIntent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                // Already on profile
                return true;
            }
            return false;
        });
        // Set profile as selected
        bottomNav.setSelectedItemId(R.id.nav_profile);
    }

    private void loadUserData() {
        // Set username
        usernameText.setText(currentUsername);

        // Load sample data (replace with actual data later)
        postsCount.setText("0");
        followersCount.setText("0");
        followingCount.setText("0");
        bioText.setText(currentBio);

        // Load sample posts (replace with actual user posts later)
        userPosts.clear();
        userPosts.add(new Post(
                currentUsername,
                "https://via.placeholder.com/300x300.png?text=Post+1",
                "My first post!",
                0,
                new ArrayList<>()
        ));
        postAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            // TODO: Implement edit profile functionality
            Toast.makeText(this, "Edit profile coming soon!", Toast.LENGTH_SHORT).show();
        });

        changeThemeButton.setOnClickListener(v -> {
            // TODO: Implement theme change functionality
            Toast.makeText(this, "Theme change coming soon!", Toast.LENGTH_SHORT).show();
        });

        changeMusicButton.setOnClickListener(v -> {
            // TODO: Implement music change functionality
            Toast.makeText(this, "Music change coming soon!", Toast.LENGTH_SHORT).show();
        });

        profileImage.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        followButton.setOnClickListener(v -> {
            if (currentUserId != null && loggedInUserId != null) {
                followUser(loggedInUserId, currentUserId);
            }
        });
    }

    private void fetchUserProfile() {
        apiService.getUser(currentUserId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    currentUsername = user.getUsername();
                    usernameText.setText(currentUsername);
                    bioText.setText(user.getBio() != null ? user.getBio() : "No bio yet");

                    // Show follow button only if viewing another user's profile
                    if (!currentUserId.equals(loggedInUserId)) {
                        followButton.setVisibility(View.VISIBLE);
                        checkFollowStatus();
                    } else {
                        followButton.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserPosts() {
        Log.d(TAG, "Fetching posts for user: " + currentUserId);
        apiService.getUserPosts(currentUserId).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                Log.d(TAG, "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    userPosts.clear();
                    userPosts.addAll(response.body());
                    postAdapter.notifyDataSetChanged();

                    // Show/hide appropriate views based on whether there are posts
                    if (userPosts.isEmpty()) {
                        tvNoPostsMessage.setVisibility(View.VISIBLE);
                        postsGrid.setVisibility(View.GONE);
                        tvNoPostsMessage.setText("No posts yet");
                    } else {
                        tvNoPostsMessage.setVisibility(View.GONE);
                        postsGrid.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch posts. Response: " + response.message());
                    tvNoPostsMessage.setVisibility(View.VISIBLE);
                    postsGrid.setVisibility(View.GONE);
                    tvNoPostsMessage.setText("Failed to load posts");
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.e(TAG, "Network error fetching posts", t);
                tvNoPostsMessage.setVisibility(View.VISIBLE);
                postsGrid.setVisibility(View.GONE);
                tvNoPostsMessage.setText("Network error loading posts");
            }
        });
    }

    private void checkFollowStatus() {
        apiService.checkFollowStatus(loggedInUserId, currentUserId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isFollowing = response.body();
                    updateFollowButton(isFollowing);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Error checking follow status", t);
            }
        });
    }

    private void updateFollowButton(boolean isFollowing) {
        followButton.setText(isFollowing ? "Unfollow" : "Follow");
        followButton.setOnClickListener(v -> {
            if (isFollowing) {
                unfollowUser(loggedInUserId, currentUserId);
            } else {
                followUser(loggedInUserId, currentUserId);
            }
        });
    }

    private void followUser(String followerId, String followeeId) {
        apiService.followUser(followerId, followeeId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    updateFollowButton(true);
                    Toast.makeText(ProfileActivity.this, "Followed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to follow user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unfollowUser(String followerId, String followeeId) {
        apiService.unfollowUser(followerId, followeeId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    updateFollowButton(false);
                    Toast.makeText(ProfileActivity.this, "Unfollowed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to unfollow user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLikeClick(Post post, int position) {
        // Toggle like state
        post.setLiked(!post.isLiked());
        post.setLikesCount(post.getLikesCount() + (post.isLiked() ? 1 : -1));
        postAdapter.notifyItemChanged(position);
        
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
                        postAdapter.notifyItemChanged(position);
                        Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show();
                        // TODO: Update comment in backend
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }
} 