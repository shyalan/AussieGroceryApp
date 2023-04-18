package com.example.aussiegroceryapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class Screen2Activity extends AppCompatActivity {

    private MaterialButton loginButton, registerButton;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen2);

        // Find the login and register buttons in the layout
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        loginButton.setOnClickListener(view -> {
            Intent intent = new Intent(Screen2Activity.this, LoginActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(view -> {
            Intent intent = new Intent(Screen2Activity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }
}