package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout createListLayout;
    private LinearLayout myListLayout;
    private LinearLayout topStoreLayout;
    private LinearLayout accountLayout;
    private LinearLayout latestDealsLayout;
    private Button moreProductsButton;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize LinearLayouts
        accountLayout = findViewById(R.id.accounts_layout);
        createListLayout = findViewById(R.id.create_list_layout);
        myListLayout = findViewById(R.id.my_list_layout);
        topStoreLayout = findViewById(R.id.top_store_layout);
        latestDealsLayout = findViewById(R.id.products_container);
        moreProductsButton = findViewById(R.id.more_products_button);

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

        // Retrieve top products from Firestore
        retrieveTopProducts();

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Buttons
        moreProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LatestDealsActivity.class);
                startActivity(intent);
            }
        });

        // Set onClickListeners for LinearLayouts
        accountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        createListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, CreatedListActivity.class);
                startActivity(intent);
            }
        });

        myListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MyListActivity.class);
                startActivity(intent);
            }
        });

        topStoreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, TopStoreActivity.class);
                startActivity(intent);
            }
        });
    }
    public void onAddToListButtonClick(View view) {
        Intent intent = new Intent(this, CreatedListActivity.class);
        startActivity(intent);
    }

    private void retrieveTopProducts() {
        firestore.collection("top_products")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String productName = documentSnapshot.getString("name");
                            double productPrice = documentSnapshot.getDouble("price");
                            String imageUrl = documentSnapshot.getString("imageUrl");

                            // Inflate the latest_deals.xml layout
                            View productView = LayoutInflater.from(HomeActivity.this)
                                    .inflate(R.layout.latest_deals, latestDealsLayout, false);

                            // Set the product name
                            TextView productNameTextView = productView.findViewById(R.id.product_name);
                            productNameTextView.setText(productName);

                            // Set the product price
                            TextView productPriceTextView = productView.findViewById(R.id.product_price);
                            productPriceTextView.setText("$" + String.valueOf(productPrice));

                            // Set the product image
                            ImageView productImageView = productView.findViewById(R.id.product_image);
                            Picasso.get()
                                    .load(imageUrl)
                                    .placeholder(R.drawable.product_image_placeholder)
                                    .into(productImageView);

                            // Add margin to the bottom of each productView
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            int marginBottom = getResources().getDimensionPixelSize(R.dimen.product_margin_bottom);
                            layoutParams.setMargins(20, 0, 20, marginBottom);
                            productView.setLayoutParams(layoutParams);

                            latestDealsLayout.addView(productView);
                        }
                    }
                });
    }
}
