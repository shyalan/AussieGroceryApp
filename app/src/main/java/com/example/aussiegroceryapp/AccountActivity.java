package com.example.aussiegroceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountActivity extends AppCompatActivity {
    private String newEmail;
    private LinearLayout homeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Button emailButton = findViewById(R.id.email_button);
        Button passwordButton = findViewById(R.id.password_button);
        Button deleteButton = findViewById(R.id.delete_button);
        Button logoutButton = findViewById(R.id.logout_button);
        homeLayout = findViewById(R.id.home_layout);

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // Get the new email from user input
                    AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
                    builder.setTitle("Change Email");
                    final EditText input = new EditText(AccountActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    builder.setView(input);
                    builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            newEmail = input.getText().toString();
                            // Handle the new email address | Update the email in Firebase Authentication
                            user.updateEmail(newEmail)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Email update successful, update email in databases
                                                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                                                String uid = user.getUid();

                                                // Update email in "lists" database
                                                databaseRef.child("lists").orderByChild("userId").equalTo(uid)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                for (DataSnapshot child : snapshot.getChildren()) {
                                                                    child.getRef().child("userEmail").setValue(newEmail);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                // Handle error
                                                            }
                                                        });

                                                // Update email in "users" database
                                                databaseRef.child("users").child(uid).child("email").setValue(newEmail)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    // Email update successful, show success message
                                                                    Toast.makeText(AccountActivity.this, "Email updated successfully", Toast.LENGTH_SHORT).show();
                                                                    // Start HomeActivity
                                                                    Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
                                                                    startActivity(intent);
                                                                } else {
                                                                    // Email update failed, show error message
                                                                    Toast.makeText(AccountActivity.this, "Try a different email", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                // Email update failed, show error message
                                                Toast.makeText(AccountActivity.this, "Email update failed", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                } else {
                    // User is not logged in, show login screen
                    Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Build a password input dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
                builder.setTitle("Confirm Password");
                final EditText input = new EditText(AccountActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the input password
                        String password = input.getText().toString();

                        // Authenticate the user with their password
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Password is correct, delete the user
                                            user.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                // User deletion successful, show success message
                                                                Toast.makeText(AccountActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                                // Start LoginActivity
                                                                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                                                                startActivity(intent);
                                                            } else {
                                                                // User deletion failed, show error message
                                                                Toast.makeText(AccountActivity.this, "Account deletion failed", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            // Password is incorrect, show error message
                                            Toast.makeText(AccountActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out user
                FirebaseAuth.getInstance().signOut();

                // Return to Screen2Activity
                Intent intent = new Intent(AccountActivity.this, Screen2Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // clear all activities on top of the main activity
                startActivity(intent);
                finish(); // close current activity
            }
        });

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
