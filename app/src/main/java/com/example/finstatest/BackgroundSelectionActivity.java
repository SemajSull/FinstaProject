package com.example.finstatest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.finstatest.api.ApiService;
import com.example.finstatest.api.ApiServiceInstance;
import com.example.finstatest.models.ProfileImageResponse;
import com.example.finstatest.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import java.io.File;

public class BackgroundSelectionActivity extends AppCompatActivity {
    private ImageView currentBackgroundPreview;
    private Button btnDefaultBackground;
    private Button btnUploadFromGallery;
    private Button btnEnterUrl;
    private LinearLayout urlInputLayout;
    private EditText etImageUrl;
    private Button btnSetUrl;
    
    private String userId;
    private String currentBackgroundUrl;
    private Uri selectedImageUri;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_selection);

        // Initialize views
        currentBackgroundPreview = findViewById(R.id.currentBackgroundPreview);
        btnDefaultBackground = findViewById(R.id.btnDefaultBackground);
        btnUploadFromGallery = findViewById(R.id.btnUploadFromGallery);
        btnEnterUrl = findViewById(R.id.btnEnterUrl);
        urlInputLayout = findViewById(R.id.urlInputLayout);
        etImageUrl = findViewById(R.id.etImageUrl);
        btnSetUrl = findViewById(R.id.btnSetUrl);

        // Get data from intent
        userId = getIntent().getStringExtra("userId");
        currentBackgroundUrl = getIntent().getStringExtra("backgroundUrl");
        
        apiService = ApiServiceInstance.getService();

        // Load current background preview
        if (currentBackgroundUrl != null && !currentBackgroundUrl.isEmpty()) {
            Glide.with(this).load(currentBackgroundUrl).into(currentBackgroundPreview);
        }

        // Image picker
        ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        uploadBackgroundImage();
                    }
                }
        );

        // Set click listeners
        btnDefaultBackground.setOnClickListener(v -> setDefaultBackground());
        btnUploadFromGallery.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnEnterUrl.setOnClickListener(v -> toggleUrlInput());
        btnSetUrl.setOnClickListener(v -> setBackgroundFromUrl());
    }

    private void setDefaultBackground() {
        updateUserBackground("");
    }

    private void toggleUrlInput() {
        if (urlInputLayout.getVisibility() == View.VISIBLE) {
            urlInputLayout.setVisibility(View.GONE);
        } else {
            urlInputLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setBackgroundFromUrl() {
        String url = etImageUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
            return;
        }
        updateUserBackground(url);
    }

    private void uploadBackgroundImage() {
        if (selectedImageUri != null && userId != null) {
            File file = new File(FileUtils.getPath(this, selectedImageUri));
            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedImageUri)), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            
            // Use the dedicated background image upload endpoint
            apiService.uploadBackgroundImage(userId, body).enqueue(new Callback<ProfileImageResponse>() {
                @Override
                public void onResponse(Call<ProfileImageResponse> call, Response<ProfileImageResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String uploadedImageUrl = response.body().getImageUrl();
                        updateUserBackground(uploadedImageUrl);
                    } else {
                        Toast.makeText(BackgroundSelectionActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ProfileImageResponse> call, Throwable t) {
                    Toast.makeText(BackgroundSelectionActivity.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUserBackground(String backgroundUrl) {
        if (userId == null) {
            Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        User updateUser = new User();
        updateUser.setTheme(backgroundUrl); // Using theme field for background URL
        
        apiService.updateUserProfile(userId, updateUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BackgroundSelectionActivity.this, "Background updated!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(BackgroundSelectionActivity.this, "Failed to update background", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(BackgroundSelectionActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 