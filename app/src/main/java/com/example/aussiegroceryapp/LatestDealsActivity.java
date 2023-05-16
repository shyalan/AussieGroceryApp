package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class LatestDealsActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private LinearLayout allProducts;
    private LinearLayout homeLayout;
    private EditText searchEditText;
    private TextView noResultsTextView;

    private List<String> allProductNames;
    private List<View> allProductViews;
    private List<View> filteredProductViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest_deals);

        // Find views by ID / Initialization
        ImageView logoImageView = findViewById(R.id.logo_image_view);
        TextView welcomeTextView = findViewById(R.id.welcome_text_view);
        allProducts = findViewById(R.id.products_container);
        homeLayout = findViewById(R.id.home_layout);
        searchEditText = findViewById(R.id.search_edit_text);
        noResultsTextView = findViewById(R.id.no_results_text_view);

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize lists for product names and product views
        allProductNames = new ArrayList<>();
        allProductViews = new ArrayList<>();
        filteredProductViews = new ArrayList<>();

        // Retrieve top products from Firestore
        retrieveProducts();

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LatestDealsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // Add text change listener to search EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

        // Show all products initially
        filterProducts("");
    }

    private void filterProducts(String query) {
        filteredProductViews.clear();

        if (query.isEmpty()) {
            // Display all products
            filteredProductViews.addAll(allProductViews);
        } else {
            for (int i = 0; i < allProductNames.size(); i++) {
                String productName = allProductNames.get(i);
                if (productName.toLowerCase().contains(query.toLowerCase())) {
                    View productView = allProductViews.get(i);
                    if (productView != null) {
                        filteredProductViews.add(productView);
                    }
                }
            }
        }

        allProducts.removeAllViews();

        if (filteredProductViews.isEmpty()) {
            noResultsTextView.setVisibility(View.VISIBLE);
        } else {
            noResultsTextView.setVisibility(View.GONE);
            for (View productView : filteredProductViews) {
                allProducts.addView(productView);
            }
        }
    }

    public void onAddToListButtonClick(View view) {
        Intent intent = new Intent(LatestDealsActivity.this, CreatedListActivity.class);
        startActivity(intent);
    }

    private void retrieveProducts() {
        firestore.collection("products")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String productName = documentSnapshot.getString("name");
                            double productPrice = documentSnapshot.getDouble("price");
                            String imageUrl = documentSnapshot.getString("imageUrl");

                            // Add product name to the list
                            allProductNames.add(productName);

                            // Inflate the latest_deals.xml layout
                            View productView = LayoutInflater.from(LatestDealsActivity.this)
                                    .inflate(R.layout.latest_deals, allProducts, false);

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

                            // Add the productView to the list of product views
                            allProductViews.add(productView);
                        }

                        // Show all products initially
                        filterProducts("");
                    }
                });
    }
}