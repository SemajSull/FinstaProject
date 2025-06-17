package com.example.finstatest;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// If you're using MongoDB Java Driver:
import com.example.finstatest.api.ApiService;
import com.example.finstatest.api.ApiServiceInstance;
import com.example.finstatest.models.User;
import com.example.finstatest.models.SignInRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnSignIn;
    private TextView tvGoToSignUp;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        apiService = ApiServiceInstance.getService();

        etUsername = findViewById(R.id.etSignInUsername);
        etPassword = findViewById(R.id.etSignInPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);

        btnSignIn.setOnClickListener(v -> attemptSignIn());

        tvGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            finish();
        });
    }

    private void attemptSignIn() {
        Log.d("SIGNIN", "Got here attemptSignIn");
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }

        apiService.getUserByUsername(username).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d("SIGNIN", "onResponse: code[" + response.code() + "]  message[" + response.message() + "]");
                if (response.isSuccessful()) {
                    User user = response.body();
                    Log.d("SIGNIN", "Initial user response: " + (user != null ? "User found" : "User null"));
                    if (user != null) {
                        Log.d("SIGNIN", "User ID from initial response: " + user.getId());
                        // Now call the sign-in API with both username and password
                        SignInRequest signInRequest = new SignInRequest(username, password);
                        apiService.signInUser(signInRequest).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> signInResponse) {
                                Log.d("SIGNIN", "Sign-in response code: " + signInResponse.code());
                                if (signInResponse.isSuccessful()) {
                                    // After successful sign-in, fetch the user details to get their ID
                                    apiService.getUserByUsername(username).enqueue(new Callback<User>() {
                                        @Override
                                        public void onResponse(Call<User> call, Response<User> userResponse) {
                                            Log.d("SIGNIN", "Final user response code: " + userResponse.code());
                                            if (userResponse.isSuccessful() && userResponse.body() != null) {
                                                User loggedInUser = userResponse.body();
                                                Log.d("SIGNIN_USER_ID", "Retrieved User ID: " + loggedInUser.getId());
                                                if (loggedInUser.getId() == null) {
                                                    Log.e("SIGNIN", "User ID is still null after successful response!");
                                                }
                                                Toast.makeText(SignInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                                intent.putExtra("loggedInUserId", loggedInUser.getId());
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Log.e("SIGNIN", "Failed to get user details. Response: " + userResponse.message());
                                                Toast.makeText(SignInActivity.this, "Failed to retrieve user details.", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<User> call, Throwable t) {
                                            Toast.makeText(SignInActivity.this, "Network error fetching user details", Toast.LENGTH_SHORT).show();
                                            Log.e("SIGNIN", "Error fetching user details: " + t.getMessage(), t);
                                        }
                                    });
                                } else {
                                    Toast.makeText(SignInActivity.this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(SignInActivity.this, "Network error during sign-in", Toast.LENGTH_SHORT).show();
                                Log.e("API_ERROR", "Sign-in failed: " + t.getMessage(), t);
                            }
                        });
                    } else {
                        Toast.makeText(SignInActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignInActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(SignInActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage(), t);
            }
        });
    }
}