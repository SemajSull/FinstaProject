package com.example.finstatest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.finstatest.api.ApiService;
import com.example.finstatest.api.ApiServiceInstance;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.finstatest.models.ProfileImageResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreatePostActivity extends AppCompatActivity {
    private static final String TAG = "CreatePostActivity";
    private ImageView imagePreview;
    private EditText captionInput;
    private EditText tagsInput;
    private EditText imageUrlInput;
    private Button createButton;
    private Button useUrlButton;
    private Uri selectedImageUri;
    private String loggedInUserId;
    private ApiService apiService;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                imagePreview.setImageURI(uri);
                imagePreview.setVisibility(View.VISIBLE);
                imageUrlInput.setVisibility(View.GONE);
                useUrlButton.setText("Use Image URL");
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Get the logged-in user ID
        loggedInUserId = getIntent().getStringExtra("loggedInUserId");
        if (loggedInUserId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiServiceInstance.getService();

        // Initialize views
        imagePreview = findViewById(R.id.imagePreview);
        captionInput = findViewById(R.id.captionInput);
        tagsInput = findViewById(R.id.tagsInput);
        imageUrlInput = findViewById(R.id.imageUrlInput);
        createButton = findViewById(R.id.createButton);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        useUrlButton = findViewById(R.id.useUrlButton);

        // Setup image selection
        selectImageButton.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        // Setup URL input
        useUrlButton.setOnClickListener(v -> {
            if (imageUrlInput.getVisibility() == View.VISIBLE) {
                imageUrlInput.setVisibility(View.GONE);
                useUrlButton.setText("Use Image URL");
            } else {
                imageUrlInput.setVisibility(View.VISIBLE);
                imagePreview.setVisibility(View.GONE);
                selectedImageUri = null;
                useUrlButton.setText("Use Gallery");
            }
        });

        // Setup create button
        createButton.setOnClickListener(v -> handleCreatePost());
    }

    private void handleCreatePost() {
        String caption = captionInput.getText().toString().trim();
        String tagsText = tagsInput.getText().toString().trim();
        List<String> tags = tagsText.isEmpty() ? new ArrayList<>() : Arrays.asList(tagsText.split("\\s*,\\s*"));

        if (selectedImageUri != null) {
            // Option 1: Upload local image and create post in one call
            createPostWithUpload(caption, tags);
        } else if (imageUrlInput.getVisibility() == View.VISIBLE) {
            // Option 2: Create post directly with a URL
            String imageUrl = imageUrlInput.getText().toString().trim();
            if (imageUrl.isEmpty()) {
                Toast.makeText(this, "Please enter an image URL", Toast.LENGTH_SHORT).show();
                return;
            }
            createPostWithUrl(caption, tags, imageUrl);
        } else {
            Toast.makeText(this, "Please select an image or provide a URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void createPostWithUrl(String caption, List<String> tags, String imageUrl) {
        setLoadingState(true);
        CreatePostRequest request = new CreatePostRequest(loggedInUserId, imageUrl, caption, new ArrayList<>(tags));
        
        Log.d(TAG, "Creating post with URL - authorId: " + loggedInUserId);
        Log.d(TAG, "Creating post with URL - imageUrl: " + imageUrl);
        Log.d(TAG, "Creating post with URL - caption: " + caption);
        Log.d(TAG, "Creating post with URL - tags: " + tags);

        apiService.createPostWithUrl(request).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                setLoadingState(false);
                Log.d(TAG, "Create post response code: " + response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Post created successfully!");
                    Toast.makeText(CreatePostActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the previous screen
                } else {
                    Log.e(TAG, "Failed to create post. Response: " + response.message());
                    try {
                        Log.e(TAG, "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "Could not read error body", e);
                    }
                    Toast.makeText(CreatePostActivity.this, "Failed to create post.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "Network error creating post", t);
                Toast.makeText(CreatePostActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPostWithUpload(String caption, List<String> tags) {
        setLoadingState(true);
        Log.d(TAG, "Creating post with upload - authorId: " + loggedInUserId);
        Log.d(TAG, "Creating post with upload - caption: " + caption);
        Log.d(TAG, "Creating post with upload - tags: " + tags);

        File imageFile;
        try {
            imageFile = createTempFileFromUri(selectedImageUri);
            Log.d(TAG, "Created temp file: " + imageFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to create temp file", e);
            Toast.makeText(this, "Failed to read image file.", Toast.LENGTH_SHORT).show();
            setLoadingState(false);
            return;
        }

        RequestBody authorIdBody = RequestBody.create(MediaType.parse("text/plain"), loggedInUserId);
        RequestBody captionBody = RequestBody.create(MediaType.parse("text/plain"), caption);
        RequestBody tagsBody = RequestBody.create(MediaType.parse("text/plain"), String.join(",", tags));

        RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedImageUri)), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        Log.d(TAG, "Making API call to createPostWithUpload");

        apiService.createPostWithUpload(authorIdBody, captionBody, tagsBody, body).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                setLoadingState(false);
                Log.d(TAG, "Create post upload response code: " + response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Post created successfully via upload!");
                    Toast.makeText(CreatePostActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Failed to create post via upload. Response: " + response.message());
                    try {
                        Log.e(TAG, "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "Could not read error body", e);
                    }
                    Toast.makeText(CreatePostActivity.this, "Failed to create post.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "Network error creating post via upload", t);
                Toast.makeText(CreatePostActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        createButton.setEnabled(!isLoading);
        createButton.setText(isLoading ? "Creating..." : "Create Post");
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("temp_image", ".jpg", getCacheDir());
        tempFile.deleteOnExit();
        try (OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        inputStream.close();
        return tempFile;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
} 