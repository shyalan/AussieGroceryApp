package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StoreProductsActivity extends AppCompatActivity {

    private TextView titleTextView;
    private ListView listView;
    private Button backButton, homeButton;
    private ArrayList<String> productList;
    private ArrayAdapter<String> adapter;
    private String storeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_products);

        // Get store name passed from previous activity
        storeName = getIntent().getStringExtra("storeName");

        // Get references to UI elements
        titleTextView = findViewById(R.id.title_text_view);
        listView = findViewById(R.id.list_view);
        backButton = findViewById(R.id.back_button);
        homeButton = findViewById(R.id.home_button);

        // Set the title of the activity to the store name
        titleTextView.setText(storeName);

        // Initialize the product list
        productList = new ArrayList<>();

        // Getting data and populating the list view
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").whereEqualTo("storeName", storeName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String name = documentSnapshot.getString("name");
                            double price = documentSnapshot.getDouble("price");
                            String product = name + " - $" + price;
                            productList.add(product);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors
                    }
                });


        // Set up the adapter and connect it to the list view
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);
        listView.setAdapter(adapter);

        // Set up a click listener for the list view items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(StoreProductsActivity.this, CreatedListActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listeners for the buttons
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreProductsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}