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

public class SignInActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnSignIn;
    private TextView tvGoToSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Wire up the views:
        etUsername       = findViewById(R.id.etSignInUsername);
        etPassword    = findViewById(R.id.etSignInPassword);
        btnSignIn     = findViewById(R.id.btnSignIn);
        tvGoToSignUp  = findViewById(R.id.tvGoToSignUp);

        // When “Sign In” button is tapped, attempt to login:
        btnSignIn.setOnClickListener(v -> attemptSignIn());

        // When “Don’t have an account? Sign Up” is tapped, go to SignUpActivity:
        tvGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            finish();
        });
    }

    private void attemptSignIn() {
        String username    = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }

        // Run DB query on background thread:
        new ValidateUserTask(username, password).execute();
    }

    private class ValidateUserTask extends AsyncTask<Void, Void, Boolean> {
        private final String username, password;
        private String errorMessage = null;

        ValidateUserTask(String username, String password) {
            this.username    = username;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // 1. Get the database and "users" collection
                MongoDatabase db = MongoUtil.getAppDatabase();
                MongoCollection<Document> usersColl = db.getCollection("users");

                // 2. Look up a document where username/password match
                Document query = new Document("username", username)
                        .append("password", password);
                Document userDoc = usersColl.find(query).first();

                return (userDoc != null);

            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean found) {
            if (found) {
                Toast.makeText(SignInActivity.this,
                        "Sign in successful!", Toast.LENGTH_SHORT).show();
                // TODO: Launch your main/home activity, e.g.:
                // startActivity(new Intent(SignInActivity.this, FeedActivity.class));
                // finish();
            } else {
                if (errorMessage != null) {
                    Toast.makeText(SignInActivity.this,
                            errorMessage, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SignInActivity.this,
                            "Invalid username or password.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
