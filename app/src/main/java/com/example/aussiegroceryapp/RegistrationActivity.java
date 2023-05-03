package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistrationActivity extends AppCompatActivity {

    private EditText fullNameEditText, emailEditText, addressEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;
    public class User {
        private String fullName;
        private String email;
        private String address;

        public User() {}

        public User(String fullName, String email, String address) {
            this.fullName = fullName;
            this.email = email;
            this.address = address;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        // Find views in the layout
        fullNameEditText = findViewById(R.id.full_name);
        emailEditText = findViewById(R.id.email);
        addressEditText = findViewById(R.id.address);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.register_button);
        loginTextView = findViewById(R.id.login_textview);

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Set click listener for register button
        registerButton.setOnClickListener(view -> {
            // Disable the register button
            registerButton.setEnabled(false);

            // Get user input
            String fullName = fullNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            // Check if any of the fields are empty
            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(address)
                    || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                // Enable the register button
                registerButton.setEnabled(true);

                // Highlight the empty fields in red
                if (TextUtils.isEmpty(fullName)) {
                    fullNameEditText.setError("This field is required");
                }
                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("This field is required");
                }
                if (TextUtils.isEmpty(address)) {
                    addressEditText.setError("This field is required");
                }
                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("This field is required");
                }
                if (TextUtils.isEmpty(confirmPassword)) {
                    confirmPasswordEditText.setError("This field is required");
                }
                return;
            }

            // Check if the email is valid
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                emailEditText.setError("Please enter a valid email address");
                // Enable the register button
                registerButton.setEnabled(true);
                return;
            }

            // Check if the passwords match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                passwordEditText.setError("Passwords do not match");
                confirmPasswordEditText.setError("Passwords do not match");
                // Enable the register button
                registerButton.setEnabled(true);
                return;
            }

            // Register user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Enable the register button
                            registerButton.setEnabled(true);

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                String uid = user.getUid();

                                // Create a new user object with the user's details
                                User newUser = new User(fullName, email, address);

                                // Add the new user object to the "users" collection in Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(uid).set(newUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Send email verification
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if (user != null) {
                                                    user.sendEmailVerification()
                                                            .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(RegistrationActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }

                                                Toast.makeText(RegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                                // Move user to Screen2Activity
                                                Intent intent = new Intent(RegistrationActivity.this, Screen2Activity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // If registration fails, display a message to the user.
                                                Toast.makeText(RegistrationActivity.this, "Registration failed. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // If registration fails, display a message to the user.
                                Toast.makeText(RegistrationActivity.this, "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        // Set click listener for login textview
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    // Helper method to check if email is valid
    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    // Helper method to show a toast message
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}