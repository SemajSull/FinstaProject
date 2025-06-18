package com.example.finstatest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.finstatest.api.ApiService;
import com.example.finstatest.api.ApiServiceInstance;
import com.example.finstatest.models.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private LinearLayout resultsContainer;
    private TextView noResultsText;
    private ApiService apiService;
    private String loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get the logged-in user ID from the Intent
        loggedInUserId = getIntent().getStringExtra("loggedInUserId");
        if (loggedInUserId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        searchInput = findViewById(R.id.searchInput);
        resultsContainer = findViewById(R.id.resultsContainer);
        noResultsText = findViewById(R.id.noResultsText);
        Button searchButton = findViewById(R.id.searchButton);

        // Initialize API service
        apiService = ApiServiceInstance.getService();

        // Set up search button click listener
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                searchUsers(query);
            } else {
                Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up bottom navigation
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
                // Already on search
                return true;
            } else if (id == R.id.nav_profile) {
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.putExtra("username", "user"); // TODO: Pass actual username
                profileIntent.putExtra("loggedInUserId", loggedInUserId);
                startActivity(profileIntent);
                finish();
                return true;
            }
            return false;
        });
        // Set search as selected
        bottomNav.setSelectedItemId(R.id.nav_search);
    }

    private void searchUsers(String query) {
        Log.d("SEARCH", "Searching for users with query: " + query);
        apiService.searchUsers(query).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                Log.d("SEARCH", "Search response code: " + response.code());
                if (response.isSuccessful()) {
                    List<User> users = response.body();
                    Log.d("SEARCH", "Found " + (users != null ? users.size() : 0) + " users");
                    if (users != null) {
                        displayResults(users);
                    } else {
                        showNoResults();
                    }
                } else {
                    Log.e("SEARCH", "Failed to search users. Response: " + response.message());
                    Toast.makeText(SearchActivity.this, "Failed to search users: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("SEARCH", "Network error: " + t.getMessage(), t);
                Toast.makeText(SearchActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoResults() {
        resultsContainer.removeAllViews();
        noResultsText.setVisibility(View.VISIBLE);
    }

    private void displayResults(List<User> users) {
        resultsContainer.removeAllViews();
        
        if (users.isEmpty()) {
            showNoResults();
            return;
        }

        noResultsText.setVisibility(View.GONE);
        
        for (User user : users) {
            TextView userButton = new TextView(this);
            userButton.setText(user.getUsername());
            userButton.setTextSize(18);
            userButton.setPadding(16, 16, 16, 16);
            userButton.setBackgroundResource(android.R.drawable.list_selector_background);
            userButton.setOnClickListener(v -> {
                Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                profileIntent.putExtra("username", user.getUsername());
                profileIntent.putExtra("userId", user.getId());
                profileIntent.putExtra("loggedInUserId", loggedInUserId);
                startActivity(profileIntent);
            });
            resultsContainer.addView(userButton);
        }
    }
} 