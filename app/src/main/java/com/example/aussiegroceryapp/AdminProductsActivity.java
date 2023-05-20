package com.example.aussiegroceryapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
        private String imageUrl;

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

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private Button mUploadButton;

    // Declare a Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);

        mUploadButton = findViewById(R.id.upload_button);
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // UI
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

                        // Check if an image has been selected
                        if (mImageUri != null) {
                            // Create a reference to the image in Firebase Storage
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("product_images/" + ename + ".jpg");

                            // Upload the image to Firebase Storage
                            storageRef.putFile(mImageUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // Get the download URL of the image
                                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    // Update the product object with the download URL of the image
                                                    product.setImageUrl(uri.toString());

                                                    // Add the product to Firestore
                                                    addProductToFirestore(product);
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Enable the button
                                            mFinishButton.setEnabled(true);

                                            // Show an error message
                                            Toast.makeText(AdminProductsActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Add the product to Firestore without an image
                            addProductToFirestore(product);
                        }
                    } else {
                        // Error occurred while retrieving documents, show a toast message with the error
                        Toast.makeText(AdminProductsActivity.this, "Error retrieving products: Fields are not entered", Toast.LENGTH_SHORT).show();
                        mFinishButton.setEnabled(true);
                    }
                }
            }
        });

        // Set OnClickListener for "Back" button
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous activity
                finish();
            }
        });

        // Set OnClickListener for "Store" button
        mStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the StoreActivity
                Intent intent = new Intent(AdminProductsActivity.this, AdminStoreActivity.class);
                startActivity(intent);
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mLogoImageView.setImageURI(mImageUri);
        }
    }

    private void addProductToFirestore(Product product) {
        // Add the product to Firestore
        db.collection("products")
                .add(product)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Enable the button
                        mFinishButton.setEnabled(true);

                        // Show a success message
                        Toast.makeText(AdminProductsActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Enable the button
                        mFinishButton.setEnabled(true);

                        // Show an error message
                        Toast.makeText(AdminProductsActivity.this, "Failed to add product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
