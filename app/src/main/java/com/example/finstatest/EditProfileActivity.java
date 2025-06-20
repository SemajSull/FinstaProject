package com.example.finstatest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.finstatest.api.ApiService;
import com.example.finstatest.api.ApiServiceInstance;
import com.example.finstatest.models.User;
import com.example.finstatest.models.ProfileImageResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import java.io.File;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView editProfileImage;
    private Button btnSelectImage;
    private Button btnUseUrl;
    private EditText editImageUrl;
    private EditText editBio;
    private Button btnSaveProfile;
    private Uri selectedImageUri;
    private String userId;
    private String currentProfileImageUrl;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editProfileImage = findViewById(R.id.editProfileImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUseUrl = findViewById(R.id.btnUseUrl);
        editImageUrl = findViewById(R.id.editImageUrl);
        editBio = findViewById(R.id.editBio);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        apiService = ApiServiceInstance.getService();

        // Load current profile image and bio from intent extras
        currentProfileImageUrl = getIntent().getStringExtra("profileImageUrl");
        String currentBio = getIntent().getStringExtra("bio");
        userId = getIntent().getStringExtra("userId");
        if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
            Glide.with(this).load(currentProfileImageUrl).placeholder(R.drawable.default_profile).into(editProfileImage);
        }
        if (currentBio != null) {
            editBio.setText(currentBio);
        }

        // Image picker
        ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        editProfileImage.setImageURI(uri);
                        editImageUrl.setVisibility(View.GONE);
                        btnUseUrl.setText("Use Image URL");
                    }
                }
        );

        btnSelectImage.setOnClickListener(v -> {
            if (editImageUrl.getVisibility() == View.VISIBLE) {
                // In URL mode, switch back to gallery mode
                editImageUrl.setVisibility(View.GONE);
                btnUseUrl.setText("Use Image URL");
                selectedImageUri = null;
            } else {
                // In gallery mode, open image picker
                imagePickerLauncher.launch("image/*");
            }
        });

        // Setup URL input toggle
        btnUseUrl.setOnClickListener(v -> {
            if (editImageUrl.getVisibility() == View.VISIBLE) {
                editImageUrl.setVisibility(View.GONE);
                btnUseUrl.setText("Use Image URL");
                selectedImageUri = null;
            } else {
                editImageUrl.setVisibility(View.VISIBLE);
                selectedImageUri = null;
                btnUseUrl.setText("Use Gallery");
            }
        });

        // Add text change listener to preview URL images
        editImageUrl.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    Glide.with(EditProfileActivity.this)
                         .load(url)
                         .placeholder(R.drawable.default_profile)
                         .error(R.drawable.default_profile)
                         .into(editProfileImage);
                }
            }
        });

        btnSaveProfile.setOnClickListener(v -> {
            String newBio = editBio.getText().toString();
            handleProfileUpdate(newBio);
        });
    }

    private void handleProfileUpdate(String newBio) {
        if (selectedImageUri != null) {
            // Upload local image first, then save profile
            uploadProfileImageAndSave(newBio);
        } else if (editImageUrl.getVisibility() == View.VISIBLE) {
            // Use image URL directly
            String imageUrl = editImageUrl.getText().toString().trim();
            if (imageUrl.isEmpty()) {
                Toast.makeText(this, "Please enter an image URL", Toast.LENGTH_SHORT).show();
                return;
            }
            saveProfile(newBio, imageUrl);
        } else {
            // No new image, just save with current image
            saveProfile(newBio, currentProfileImageUrl);
        }
    }

    private void uploadProfileImageAndSave(String newBio) {
        if (selectedImageUri != null && userId != null) {
            // Only upload if the image is a local file
            File file = new File(FileUtils.getPath(this, selectedImageUri));
            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedImageUri)), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            apiService.uploadProfileImage(userId, body).enqueue(new Callback<ProfileImageResponse>() {
                @Override
                public void onResponse(Call<ProfileImageResponse> call, Response<ProfileImageResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String uploadedImageUrl = response.body().getImageUrl();
                        saveProfile(newBio, uploadedImageUrl);
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ProfileImageResponse> call, Throwable t) {
                    Toast.makeText(EditProfileActivity.this, "Image upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // No new image, just save
            saveProfile(newBio, currentProfileImageUrl);
        }
    }

    private void saveProfile(String bio, String imageUrl) {
        User updateUser = new User();
        updateUser.setBio(bio);
        updateUser.setProfileImageUrl(imageUrl);
        apiService.updateUserProfile(userId, updateUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 