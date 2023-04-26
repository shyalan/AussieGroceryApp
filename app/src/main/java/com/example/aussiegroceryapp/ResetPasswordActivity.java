package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText mEmailEditText;
    private EditText mConfirmEmailEditText;
    private EditText mResetCodeEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mEmailEditText = findViewById(R.id.email_edit);
        mConfirmEmailEditText = findViewById(R.id.confirm_email_edit);

        // Go back
        TextView backText = findViewById(R.id.back_text);
        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button getResetCodeButton = findViewById(R.id.get_reset_code_button);
        getResetCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEditText.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    mEmailEditText.setError("Email is required");
                    mEmailEditText.requestFocus();
                    return;
                }

                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            List<String> signInMethods = result.getSignInMethods();
                            if (signInMethods == null || signInMethods.isEmpty()) {
                                Toast.makeText(ResetPasswordActivity.this, "Email ID is incorrect", Toast.LENGTH_SHORT).show();
                            } else {
                                auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ResetPasswordActivity.this, "Reset code sent to " + email, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            String errorMessage = task.getException().getMessage();
                                            Toast.makeText(ResetPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Failed to check email ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}