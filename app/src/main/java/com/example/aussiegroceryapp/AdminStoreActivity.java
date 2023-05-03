package com.example.aussiegroceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AdminStoreActivity extends AppCompatActivity {

    private EditText storeNameEditText;
    private EditText longitudeEditText;
    private EditText latitudeEditText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_store);

        // Initialize FirebaseApp
        FirebaseApp.initializeApp(this);

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Find views by ID
        storeNameEditText = findViewById(R.id.store_name_edit_text);
        longitudeEditText = findViewById(R.id.longitude_edit_text);
        latitudeEditText = findViewById(R.id.latitude_edit_text);

        // Create a reference to the Firestore database
        db = FirebaseFirestore.getInstance();

        // Set OnClickListener for "Add New Store" button
        Button storeButton = findViewById(R.id.store_button);
        storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable the button
                storeButton.setEnabled(false);

                // Get values from EditTexts
                String storeName = storeNameEditText.getText().toString().trim();
                Double longitude = Double.parseDouble(longitudeEditText.getText().toString().trim());
                Double latitude = Double.parseDouble(latitudeEditText.getText().toString().trim());

                // Validate input
                if (storeName.isEmpty() || longitude == null || latitude == null) {
                    Toast.makeText(AdminStoreActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                    // Enable the button
                    storeButton.setEnabled(true);
                } else {
                    // Query the database for any existing stores with the same name, longitude, or latitude
                    db.collection("stores")
                            .whereEqualTo("name", storeName)
                            .whereEqualTo("longitude", longitude)
                            .whereEqualTo("latitude", latitude)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            // Show error message if there is an existing store with the same name, longitude, or latitude
                                            Toast.makeText(AdminStoreActivity.this, "A store with the same name, longitude, or latitude already exists", Toast.LENGTH_SHORT).show();
                                            // Enable the button
                                            storeButton.setEnabled(true);
                                        } else {
                                            // Create a new store object
                                            Map<String, Object> store = new HashMap<>();
                                            store.put("name", storeName);
                                            store.put("longitude", longitude);
                                            store.put("latitude", latitude);

                                            // Add the new store to the "stores" collection in the Firestore database
                                            db.collection("stores")
                                                    .add(store)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            // Show success message
                                                            Toast.makeText(AdminStoreActivity.this, "New store added", Toast.LENGTH_SHORT).show();
                                                            // Clear EditTexts
                                                            storeNameEditText.setText("");
                                                            longitudeEditText.setText("");
                                                            latitudeEditText.setText("");
                                                            // Enable the button
                                                            storeButton.setEnabled(true);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Show failure message
                                                            Toast.makeText(AdminStoreActivity.this, "Failed to add new store", Toast.LENGTH_SHORT).show();
                                                            // Enable the button
                                                            storeButton.setEnabled(true);
                                                        }
                                                    });
                                        }
                                    } else {
                                        // Show failure message
                                        Toast.makeText(AdminStoreActivity.this, "Failed to query database", Toast.LENGTH_SHORT).show();
                                        // Enable the button
                                        storeButton.setEnabled(true);
                                    }
                                }
                            });
                }
            }
        });

        // Set OnClickListener for "Return to Admin Page" button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminStoreActivity.this, AdminHomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
