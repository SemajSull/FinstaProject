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

public class ProfileActivity extends AppCompatActivity {
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
        postAdapter = new PostAdapter(userPosts);
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
        // Profile image click to change
        profileImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Edit profile button
        editProfileButton.setOnClickListener(v -> {
            // Toggle bio editability
            bioText.setEnabled(!bioText.isEnabled());
            if (!bioText.isEnabled()) {
                currentBio = bioText.getText().toString();
                Toast.makeText(this, "Bio updated!", Toast.LENGTH_SHORT).show();
            }
        });

        // Change theme button
        changeThemeButton.setOnClickListener(v -> {
            // TODO: Implement theme selection
            Toast.makeText(this, "Theme selection coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Change music button
        changeMusicButton.setOnClickListener(v -> {
            // TODO: Implement music selection
            Toast.makeText(this, "Music selection coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save any changes made to the profile
        saveProfileChanges();
    }

    private void saveProfileChanges() {
        // TODO: Implement saving profile changes to database
        // For now, just update local variables
        currentBio = bioText.getText().toString();
    }
} 