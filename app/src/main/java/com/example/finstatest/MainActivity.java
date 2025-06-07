package com.example.finstatest;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Immediately go to SignInActivity, and close MainActivity
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
