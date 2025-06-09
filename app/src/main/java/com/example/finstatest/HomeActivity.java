package com.example.finstatest;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

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
                Arrays.asList("Nice!", "Love it!")
        ));
        postList.add(new Post(
                "user2",
                "https://via.placeholder.com/600x400.png?text=Post+Image+2",
                "Welcome to Finsta!",
                34,
                Arrays.asList("Cool pic", "Awesome!")
        ));

        // 3) Attach adapter
        adapter = new PostAdapter(postList);
        recyclerView.setAdapter(adapter);

        // 4) Bottom nav handling
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
                // TODO: launch ProfileActivity
                return true;
            }
            return false;
        });

    }
}
