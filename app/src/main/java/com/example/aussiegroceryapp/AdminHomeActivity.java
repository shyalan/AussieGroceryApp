package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    private Button addProductsButton;
    private Button storesButton;
    private Button logoutButton;
    private Button userButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        addProductsButton = findViewById(R.id.add_products_button);
        storesButton = findViewById(R.id.stores_button);
        userButton = findViewById(R.id.user_button);
        logoutButton = findViewById(R.id.logout_button);

        addProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminProductsActivity.class);
                startActivity(intent);
            }
        });

        storesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this,AdminStoreActivity.class);
                startActivity(intent);
            }
        });

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out user
                FirebaseAuth.getInstance().signOut();

                // Return to screen2 activity
                Intent intent = new Intent(AdminHomeActivity.this, Screen2Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // clear all activities on top of main activity
                startActivity(intent);
                finish(); // close current activity
            }
        });
    }
}
