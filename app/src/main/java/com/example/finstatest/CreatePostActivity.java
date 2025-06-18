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
                useUrlButton.setText("Use Gallery");
                selectedImageUri = null;
                imagePreview.setVisibility(View.GONE);
            }
        });

        // Setup create button
        createButton.setOnClickListener(v -> {
            if (selectedImageUri == null && imageUrlInput.getVisibility() == View.GONE) {
                Toast.makeText(this, "Please select an image or enter an image URL", Toast.LENGTH_SHORT).show();
                return;
            }

            String caption = captionInput.getText().toString().trim();
            String tagsText = tagsInput.getText().toString().trim();
            List<String> tags = tagsText.isEmpty() ? new ArrayList<>() : 
                              Arrays.asList(tagsText.split("\\s*,\\s*"));

            if (selectedImageUri != null) {
                createPostWithLocalImage(caption, tags);
            } else {
                createPostWithUrl(caption, tags);
            }
        });
    }

    private void createPostWithLocalImage(String caption, List<String> tags) {
        try {
            File imageFile = createTempFileFromUri(selectedImageUri);
            if (imageFile == null) {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody imageRequestBody = RequestBody.create(
                MediaType.parse("image/*"),
                imageFile
            );
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.getName(),
                imageRequestBody
            );

            createPostRequest(imagePart, caption, tags);

        } catch (Exception e) {
            Log.e(TAG, "Error creating post with local image", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createPostWithUrl(String caption, List<String> tags) {
        String imageUrl = imageUrlInput.getText().toString().trim();
        if (imageUrl.isEmpty()) {
            Toast.makeText(this, "Please enter an image URL", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        createButton.setEnabled(false);
        createButton.setText("Creating post...");

        // Download image from URL
        executor.execute(() -> {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                Log.d(TAG, "Attempting to download image from URL: " + imageUrl);
                URL url = new URL(imageUrl);
                
                // Set connection timeout
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Accept", "image/*");
                
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Server response code: " + responseCode);
                
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Server returned: " + responseCode);
                }

                inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                
                if (bitmap == null) {
                    throw new IOException("Failed to decode image");
                }

                Log.d(TAG, "Successfully downloaded image, size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                
                // Save bitmap to temporary file
                File tempFile = File.createTempFile("post_image_", ".jpg", getCacheDir());
                FileOutputStream out = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();

                Log.d(TAG, "Saved image to temporary file: " + tempFile.getAbsolutePath());

                // Create request body
                RequestBody imageRequestBody = RequestBody.create(
                    MediaType.parse("image/*"),
                    tempFile
                );
                MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "image",
                    tempFile.getName(),
                    imageRequestBody
                );

                mainHandler.post(() -> {
                    createPostRequest(imagePart, caption, tags);
                    createButton.setEnabled(true);
                    createButton.setText("Create Post");
                });

            } catch (Exception e) {
                Log.e(TAG, "Error downloading image from URL: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    Toast.makeText(CreatePostActivity.this, 
                        "Error downloading image: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    createButton.setEnabled(true);
                    createButton.setText("Create Post");
                });
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing input stream", e);
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void createPostRequest(MultipartBody.Part imagePart, String caption, List<String> tags) {
        Log.d(TAG, "Creating post request with caption: " + caption + ", tags: " + tags);
        
        RequestBody captionPart = RequestBody.create(
            MediaType.parse("text/plain"),
            caption
        );
        RequestBody tagsPart = RequestBody.create(
            MediaType.parse("text/plain"),
            String.join(",", tags)
        );
        RequestBody authorIdPart = RequestBody.create(
            MediaType.parse("text/plain"),
            loggedInUserId
        );

        apiService.createPost(imagePart, captionPart, tagsPart, authorIdPart)
            .enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Post created successfully");
                        Toast.makeText(CreatePostActivity.this, 
                            "Post created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.e(TAG, "Failed to create post. Response code: " + response.code());
                        Toast.makeText(CreatePostActivity.this,
                            "Failed to create post: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Network error creating post", t);
                    Toast.makeText(CreatePostActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile("post_image_", ".jpg", getCacheDir());
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Error creating temp file", e);
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
} 