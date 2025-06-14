package com.example.finstatest;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements PostAdapter.OnPostInteractionListener {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1) Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2) Sample data (replace with real data source later)
        postList = new ArrayList<>();
        postList.add(new Post(
                "user1",
                "https://via.placeholder.com/600x400.png?text=Post+Image+1",
                "This is a test post.",
                12,
                new ArrayList<>(Arrays.asList("Nice!", "Love it!"))
        ));
        postList.add(new Post(
                "user2",
                "https://via.placeholder.com/600x400.png?text=Post+Image+2",
                "Welcome to Finsta!",
                34,
                new ArrayList<>(Arrays.asList("Cool pic", "Awesome!"))
        ));
        postList.add(new Post(
                "user3",
                "https://via.placeholder.com/600x400.png?text=Post+Image+3",
                "Enjoying the day!",
                20,
                new ArrayList<>(Arrays.asList("Great photo!", "So beautiful!"))
        ));
        postList.add(new Post(
                "user4",
                "https://via.placeholder.com/600x400.png?text=Post+Image+4",
                "New adventures ahead.",
                45,
                new ArrayList<>(Arrays.asList("Awesome!", "Where is this?"))
        ));
        postList.add(new Post(
                "user1",
                "https://via.placeholder.com/600x400.png?text=Post+Image+5",
                "Learning something new.",
                8,
                new ArrayList<>(Arrays.asList("Keep it up!", "Interesting"))
        ));
        postList.add(new Post(
                "user5",
                "https://via.placeholder.com/600x400.png?text=Post+Image+6",
                "Weekend vibes.",
                50,
                new ArrayList<>(Arrays.asList("Relaxing!", "Wish I was there."))
        ));
        postList.add(new Post(
                "user2",
                "https://via.placeholder.com/600x400.png?text=Post+Image+7",
                "Exploring new places.",
                62,
                new ArrayList<>(Arrays.asList("Amazing view!", "Travel goals"))
        ));
        postList.add(new Post(
                "user3",
                "https://via.placeholder.com/600x400.png?text=Post+Image+8",
                "Morning coffee.",
                15,
                new ArrayList<>(Arrays.asList("Good morning!", "Looks delicious"))
        ));
        postList.add(new Post(
                "user4",
                "https://via.placeholder.com/600x400.png?text=Post+Image+9",
                "Coding session.",
                28,
                new ArrayList<>(Arrays.asList("Hard work pays off!", "Get it!"))
        ));
        postList.add(new Post(
                "user5",
                "https://via.placeholder.com/600x400.png?text=Post+Image+10",
                "Sunset views.",
                70,
                new ArrayList<>(Arrays.asList("Stunning!", "Beautiful sunset"))
        ));

        // Sort posts by date (most recent first)
        Collections.sort(postList, (p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));

        // 3) Attach adapter
        adapter = new PostAdapter(postList, this);
        recyclerView.setAdapter(adapter);

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
                startActivity(profileIntent);
                finish();
                return true;
            }
            return false;
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
