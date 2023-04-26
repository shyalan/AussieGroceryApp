package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LatestDealsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest_deals);

        // Find views by ID
        ImageView logoImageView = findViewById(R.id.logo_image_view);
        TextView welcomeTextView = findViewById(R.id.welcome_text_view);
        Button addProductButton = findViewById(R.id.add_product_button);
        Button homeButton = findViewById(R.id.home_button);

        // Set logo image resource
        logoImageView.setImageResource(R.drawable.logo);

        // Set text for welcome TextView
        welcomeTextView.setText("Latest Deals");

        // Set adapter for deals ListView

        // Set click listeners for buttons
        addProductButton.setOnClickListener(v -> {
        });

        homeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LatestDealsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
