package com.example.finstatest;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// If you’re using MongoDB Java Driver:
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvGoToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Wire up the views:
        etUsername           = findViewById(R.id.etSignUpUsername);
        etPassword        = findViewById(R.id.etSignUpPassword);
        etConfirmPassword = findViewById(R.id.etSignUpConfirmPassword);
        btnSignUp         = findViewById(R.id.btnSignUp);
        tvGoToSignIn      = findViewById(R.id.tvGoToSignIn);

        // “Sign Up” button → attempt registration:
        btnSignUp.setOnClickListener(v -> attemptSignUp());

        // “Already have an account? Sign In” → go back to SignInActivity:
        tvGoToSignIn.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            finish();
        });
    }

    private void attemptSignUp() {
        String username           = etUsername.getText().toString().trim();
        String password        = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Insert a new user on a background thread:
        new InsertUserTask(username, password).execute();
    }

    private class InsertUserTask extends AsyncTask<Void, Void, Boolean> {
        private final String username, password;
        private String errorMessage = null;

        InsertUserTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // 1. Get the database and “users” collection
                MongoDatabase db = MongoUtil.getAppDatabase();
                MongoCollection<Document> usersColl = db.getCollection("users");

                // 2. Check if this username already exists
                Document existing = usersColl.find(new Document("username", username)).first();
                if (existing != null) {
                    errorMessage = "An account with that username already exists.";
                    return false;
                }

                // 3. Create and insert a new user document
                Document newUser = new Document();
                newUser.put("username", username);
                newUser.put("password", password); // plaintext for demo-only

                usersColl.insertOne(newUser);
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(SignUpActivity.this,
                        "Registration successful! Please sign in.",
                        Toast.LENGTH_LONG).show();
                // After successful registration, go back to SignInActivity:
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            } else {
                Toast.makeText(SignUpActivity.this,
                        errorMessage != null ? errorMessage : "Unknown error",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
