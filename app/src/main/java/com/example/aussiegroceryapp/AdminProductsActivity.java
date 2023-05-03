package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsActivity extends AppCompatActivity {

    private ImageView mLogoImageView;
    private TextView mWelcomeTextView;
    private EditText mProductNameEditText;
    private EditText mProductPriceEditText;
    private TextView mStoreTextView;
    private Spinner mProductSpinner;
    private Button mFinishButton;
    private Button mBackButton;
    private Button mStoreButton;

    public class Product {
        private String name;
        private double price;
        private String storeName;

        public Product() {
        }

        public Product(String name, double price, String storeName) {
            this.name = name;
            this.price = price;
            this.storeName = storeName;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public String getStoreName() {
            return storeName;
        }
    }

    // Declare a Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Initialize UI elements
        mLogoImageView = findViewById(R.id.logo_image_view);
        mWelcomeTextView = findViewById(R.id.welcome_text_view);
        mProductNameEditText = findViewById(R.id.product_name_edit_text);
        mProductPriceEditText = findViewById(R.id.product_price_edit_text);
        mStoreTextView = findViewById(R.id.store_text_view);
        mProductSpinner = findViewById(R.id.product_spinner);
        mFinishButton = findViewById(R.id.finish_button);
        mBackButton = findViewById(R.id.back_button);
        mStoreButton = findViewById(R.id.store_button);

        // Initialize an array list to store the names of stores
        List<String> storeNames = new ArrayList<>();

        // Query for documents in the "stores" collection
        db.collection("stores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Iterate over the documents and add the store names to the array list
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                storeNames.add(name);
                            }

                            // Set the store names as options for the spinner
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminProductsActivity.this, android.R.layout.simple_spinner_item, storeNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mProductSpinner.setAdapter(adapter);
                        } else {
                            // Error occurred while retrieving documents, show a toast message with the error
                            Toast.makeText(AdminProductsActivity.this, "Error retrieving stores: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        // Set OnClickListener for "Finish" button
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable the button
                mFinishButton.setEnabled(false);

                // Get the name and price entered by the user
                String ename = mProductNameEditText.getText().toString().trim();
                String priceString = mProductPriceEditText.getText().toString().trim();

                // Check if both fields are entered
                if (ename.isEmpty() || priceString.isEmpty()) {
                    // Enable the button
                    mFinishButton.setEnabled(true);

                    // Show an error message
                    Toast.makeText(AdminProductsActivity.this, "Please enter both product name and price", Toast.LENGTH_SHORT).show();
                } else {
                    // Get the name and price entered by the user
                    String name = mProductNameEditText.getText().toString().trim();
                    double price = Double.parseDouble(mProductPriceEditText.getText().toString().trim());
                    String storeName = mProductSpinner.getSelectedItem().toString();

                    // Check if both fields are entered
                    if (!name.isEmpty() && !mProductPriceEditText.getText().toString().isEmpty()) {
                        // Create a new product object
                        Product product = new Product(name, price, storeName);

                        // Add the product to the Firestore "products" collection
                        // Query for documents in the "products" collection to check if the product already exists
                        db.collection("products")
                                .whereEqualTo("name", name)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            boolean productExists = false;
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                // If a document with the same name exists, set productExists to true and break the loop
                                                if (document.exists()) {
                                                    productExists = true;
                                                    break;
                                                }
                                            }

                                            if (productExists) {
                                                // Product with the same name already exists, show a toast message
                                                Toast.makeText(AdminProductsActivity.this, "Product with the same name already exists", Toast.LENGTH_SHORT).show();
                                                mFinishButton.setEnabled(true);
                                            } else {
                                                // Add the product to the Firestore "products" collection
                                                db.collection("products")
                                                        .add(product)
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                if (task.isSuccessful()) {
                                                                    // Product added successfully, show a toast message
                                                                    Toast.makeText(AdminProductsActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                                                                    mFinishButton.setEnabled(true);
                                                                } else {
                                                                    // Product failed to be added, show a toast message with the error
                                                                    Toast.makeText(AdminProductsActivity.this, "Error adding product: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    mFinishButton.setEnabled(true);
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            // Error occurred while retrieving documents, show a toast message with the error
                                            Toast.makeText(AdminProductsActivity.this, "Error retrieving products: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            mFinishButton.setEnabled(true);
                                        }
                                    }
                                });
                    } else {
                        // One or both fields are empty, show a toast message
                        Toast.makeText(AdminProductsActivity.this, "Please enter both name and price of the product", Toast.LENGTH_SHORT).show();
                        mFinishButton.setEnabled(true);
                    }
                }
            }
        });

        // Set OnClickListener for "Return to Admin Page" button
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminProductsActivity.this, AdminHomeActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for "Add a Store" Button
        mStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminProductsActivity.this, AdminStoreActivity.class);
                startActivity(intent);
            }
        });
    }
}
