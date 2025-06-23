package com.example.finstatest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finstatest.api.ApiService;
import com.example.finstatest.api.ApiServiceInstance;
import com.example.finstatest.models.MusicPayload;
import com.example.finstatest.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicSelectionActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SwitchCompat removeMusicToggle;
    private EditText urlInput;
    private Button loadButton, saveButton;
    private WebView webView;
    private String embedUrl;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_embed);

        toolbar = findViewById(R.id.musicToolbar);
        setSupportActionBar(toolbar);
        // Show back arrow to go back to profile
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile Music Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        urlInput = findViewById(R.id.soundcloudUrlInput);
        loadButton = findViewById(R.id.loadButton);
        webView = findViewById(R.id.soundcloudWebView);
        saveButton = findViewById(R.id.saveButton);
        removeMusicToggle = findViewById(R.id.removeMusicToggle);
        apiService = ApiServiceInstance.getService();

        String currentUrl = getIntent().getStringExtra("currentMusic");

        if (currentUrl != null && !currentUrl.isEmpty()) {
            removeMusicToggle.setVisibility(View.VISIBLE);
        } else {
            removeMusicToggle.setVisibility(View.GONE);
        }

        loadButton.setOnClickListener(v -> {
            String rawUrl = urlInput.getText().toString().trim();
            if (!rawUrl.contains("soundcloud.com")) {
                Toast.makeText(this, "Invalid SoundCloud URL", Toast.LENGTH_SHORT).show();
                return;
            }

            embedUrl = "https://w.soundcloud.com/player/?url=" + Uri.encode(rawUrl) + "&auto_play=true&hide_related=false&show_comments=false";
            webView.setVisibility(View.VISIBLE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(embedUrl);

            saveButton.setEnabled(true);
        });

        saveButton.setOnClickListener(v -> {
            MusicPayload payload;
            String userId = getIntent().getStringExtra("userId");
            boolean removeMusic = removeMusicToggle.isChecked();
            if (removeMusic) {
                payload = new MusicPayload(null);
            } else {
                String finalUrl = urlInput.getText().toString().trim();
                payload = new MusicPayload(finalUrl);
            }
            apiService.updateMusic(userId, payload).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    Toast.makeText(MusicSelectionActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(MusicSelectionActivity.this, "Error saving", Toast.LENGTH_SHORT).show();
                }
            });

            goBackToProfile();
        });

        removeMusicToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            urlInput.setEnabled(!isChecked);
            if (loadButton != null) loadButton.setEnabled(!isChecked);

            if (urlInput != null) urlInput.setEnabled(!isChecked);

            saveButton.setEnabled(isChecked);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBackToProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goBackToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        String currentUserId = getIntent().getStringExtra("userId");
        String loggedInUserId = getIntent().getStringExtra("loggedInUserId");
        intent.putExtra("userId", currentUserId);
        intent.putExtra("loggedInUserId", loggedInUserId);
        startActivity(intent);
        finish();
    }
}