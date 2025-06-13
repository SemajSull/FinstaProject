package com.example.finstatest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements PostAdapter.OnPostInteractionListener {
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
    private PostAdapter postAdapter;
    private List<Post> userPosts;
    private String currentUsername;
    private Uri currentProfileImage;
    private String currentBio = "";
    private String currentTheme = "default";
    private String currentMusic = "";

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

        // Get username from intent
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null) {
            currentUsername = "user"; // Default username if none provided
        }

        initializeViews();
        setupBottomNavigation();
        loadUserData();
        setupClickListeners();
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

        // Setup posts grid
        postsGrid = findViewById(R.id.postsGrid);
        postsGrid.setLayoutManager(new GridLayoutManager(this, 3));
        userPosts = new ArrayList<>();
        postAdapter = new PostAdapter(userPosts, this);
        postsGrid.setAdapter(postAdapter);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_search) {
                // TODO: Implement search activity
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
        // TODO: Show comment dialog/activity
        Toast.makeText(this, "Comment functionality coming soon!", Toast.LENGTH_SHORT).show();
    }
} 