package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminTopProductActivity extends AppCompatActivity {

    private Spinner productSpinner;

    private FirebaseFirestore db;
    private List<String> productNames;

    public static class TopProductItem {
        private final String name;
        private final Double price;

        public TopProductItem(String name, Double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public Double getPrice() {
            return price;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_top_product);

        // Find views by their IDs
        productSpinner = findViewById(R.id.product_spinner);
        Button finishButton = findViewById(R.id.finish_button);
        Button productButton = findViewById(R.id.product_button);
        Button backButton = findViewById(R.id.back_button);

        db = FirebaseFirestore.getInstance();

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Set up the product spinner
        productNames = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, productNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productSpinner.setAdapter(adapter);

        // Retrieve the names from Firestore
        db.collection("products")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("name");
                            productNames.add(name);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // handle error
                    }
                });

        // Finish Button
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedProductName = productSpinner.getSelectedItem().toString();
                db.collection("products")
                        .whereEqualTo("name", selectedProductName)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                Double price = documentSnapshot.getDouble("price");

                                // Add to top product collection
                                TopProductItem topProductItem = new TopProductItem(selectedProductName, price);
                                db.collection("top_products")
                                        .add(topProductItem)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(AdminTopProductActivity.this, "Product added to top products", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(AdminTopProductActivity.this, "Failed to add product to top products", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AdminTopProductActivity.this, "Failed to retrieve product information", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Other Buttons
        productButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminTopProductActivity.this, AdminStoreActivity.class);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminTopProductActivity.this, AdminHomeActivity.class);
                startActivity(intent);
            }
        });
    }
}