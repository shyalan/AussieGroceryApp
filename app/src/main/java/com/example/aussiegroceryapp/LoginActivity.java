package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private MaterialButton loginButton;
    private FirebaseAuth mAuth;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_login);

        // Find the views
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        textView = findViewById(R.id.title_text);

        // Reset Listner and Send
        TextView resetText = findViewById(R.id.reset_text);
        resetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Go back
        TextView backText = findViewById(R.id.back_text);
        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, Screen2Activity.class);
                startActivity(intent);
            }
        });


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set click listener for login button
        loginButton.setOnClickListener(v -> {
            // Get email and password input
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate input
            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email is required"); // Display error message for empty email field
                emailEditText.requestFocus(); // Set focus on email field
                return; // Exit the function and do not proceed with authentication
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required"); // Display error message for empty password field
                passwordEditText.requestFocus(); // Set focus on password field
                return; // Exit the function and do not proceed with authentication
            }

            if (!isValidEmail(email)) {
                emailEditText.setError("Invalid email"); // Display error message for invalid email format
                emailEditText.requestFocus(); // Set focus on email field
                return; // Exit the function and do not proceed with authentication
            }

            if (!isValidPassword(password)) {
                passwordEditText.setError("Invalid password"); // Display error message for invalid password format
                passwordEditText.requestFocus(); // Set focus on password field
                return; // Exit the function and do not proceed with authentication
            }

            // Disable login button and change text
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");

            // Sign in with email and password
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            // Enable login button and change text back to the original text
                            loginButton.setEnabled(true);
                            loginButton.setText("LOGIN");

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Exception exception = task.getException();
                                if (exception instanceof FirebaseAuthInvalidUserException) {
                                    // The user account does not exist or has been disabled
                                    Toast.makeText(LoginActivity.this, "Invalid email or password",
                                            Toast.LENGTH_SHORT).show();
                                } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                    // The password is invalid
                                    Toast.makeText(LoginActivity.this, "Invalid email or password",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // Other exceptions
                                    Toast.makeText(LoginActivity.this, "Authentication failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        });
    }

    // Validate email format
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Validate password length
    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }
}
