package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private Button createListButton;
    private Button myListButton;
    private Button latestDealsButton;
    private Button topStoreButton;
    private Button logoutButton;
    private Button accountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize buttons
        accountButton = findViewById(R.id.account_button);
        createListButton = findViewById(R.id.create_list_button);
        myListButton = findViewById(R.id.my_list_button);
        latestDealsButton = findViewById(R.id.latest_deals_button);
        topStoreButton = findViewById(R.id.top_store_button);
        logoutButton = findViewById(R.id.logout_button);

        // Set onClickListeners for buttons

        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        createListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, CreatedListActivity.class);
                startActivity(intent);
            }
        });

        myListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MyListActivity.class);
                startActivity(intent);
            }
        });

        latestDealsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LatestDealsActivity.class);
                startActivity(intent);
            }
        });

        topStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, TopStoreActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out user
                FirebaseAuth.getInstance().signOut();

                // Return to screen2 activity
                Intent intent = new Intent(HomeActivity.this, Screen2Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // clear all activities on top of main activity
                startActivity(intent);
                finish(); // close current activity
            }
        });
    }
}
