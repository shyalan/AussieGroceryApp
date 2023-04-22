package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreatedListActivity extends AppCompatActivity {

    private EditText listNameEditText;
    private Spinner productSpinner;
    private ListView selectedProductsListView;
    private ArrayAdapter<String> selectedProductsAdapter;

    // Firestore instance variable
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created_list);

        // UI Elements
        listNameEditText = findViewById(R.id.list_name_edit_text);
        productSpinner = findViewById(R.id.product_spinner);
        selectedProductsListView = findViewById(R.id.selected_products_listview);
        selectedProductsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        selectedProductsListView.setAdapter(selectedProductsAdapter);

        // Spinner
        final ArrayAdapter<String> productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productSpinner.setAdapter(productAdapter);

        // Firestore initialization
        firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userEmail = currentUser.getEmail();

        // Firebase listener to populate spinner
        FirebaseDatabase.getInstance().getReference("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String productName = productSnapshot.child("name").getValue(String.class);
                    Double productPrice = productSnapshot.child("price").getValue(Double.class);
                    String productText = productName + " - $" + productPrice;
                    productAdapter.add(productText);
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });


        // Set up buttons
        Button addProductButton = findViewById(R.id.add_product_button);
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedProduct = (String) productSpinner.getSelectedItem();
                selectedProductsAdapter.add(selectedProduct);
                selectedProductsAdapter.notifyDataSetChanged();
            }
        });

        Button finishButton = findViewById(R.id.finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String listName = listNameEditText.getText().toString();
                if (listName.isEmpty()) {
                    Toast.makeText(CreatedListActivity.this, "Please enter a list name", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();
                String userEmail = currentUser.getEmail();
                Map<String, Object> list = new HashMap<>();
                list.put("name", listName);
                list.put("email", userEmail);

                // Adding selected products to the list
                ArrayList<String> selectedProductsList = new ArrayList<>();
                for (int i = 0; i < selectedProductsAdapter.getCount(); i++) {
                    String product = selectedProductsAdapter.getItem(i);
                    String[] productNameAndPrice = product.split(" - \\$");
                    String productName = productNameAndPrice[0];
                    selectedProductsList.add(productName);
                }
                list.put("products", selectedProductsList);

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("lists").add(list)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(CreatedListActivity.this, "List created successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CreatedListActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(CreatedListActivity.this, "Error creating list", Toast.LENGTH_SHORT).show());
            }
        });

//Button to return to the Home Screen
        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreatedListActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // Set up text views
        TextView welcomeTextView = findViewById(R.id.welcome_text_view);
        welcomeTextView.setText(R.string.create_list_title);

        TextView listNameLabelTextView = findViewById(R.id.list_name_edit_text);
        listNameLabelTextView.setText(R.string.list_name_label);
    }
}