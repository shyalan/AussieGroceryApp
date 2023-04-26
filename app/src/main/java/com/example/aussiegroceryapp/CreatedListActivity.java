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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreatedListActivity extends AppCompatActivity {

    private EditText listNameEditText;
    private Spinner productSpinner;
    private ListView selectedProductsListView;
    private ArrayAdapter<Product> selectedProductsList;

    // Firestore instance variable
    private FirebaseFirestore firestore;

    public class Product {
        public String name;
        public int quantity;

        public Product(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return name + " - Qty: " + quantity;
        }
    }

    private Map<String, Integer> selectedProductsMap;
    private ArrayAdapter<Product> selectedProductsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created_list);

        // UI Elements
        listNameEditText = findViewById(R.id.list_name_edit_text);
        productSpinner = findViewById(R.id.product_spinner);
        selectedProductsListView = findViewById(R.id.selected_products_listview);
        selectedProductsMap = new HashMap<>();
        selectedProductsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<Product>());
        selectedProductsListView.setAdapter(selectedProductsAdapter);

        // Spinner
        final ArrayAdapter<String> productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productSpinner.setAdapter(productAdapter);

        //Firestore initialization
        firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userEmail = currentUser.getEmail();

        // Firebase listener to populate spinner
        FirebaseFirestore.getInstance().collection("products").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String productName = documentSnapshot.getString("name");
                            Double productPrice = documentSnapshot.getDouble("price");
                            int productQty = 0;
                            if (documentSnapshot.getLong("quantity") != null) {
                                productQty = documentSnapshot.getLong("quantity").intValue();
                            }
                            String productText = productName + " - $" + productPrice + " - Qty: " + productQty;
                            productAdapter.add(productText);
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle database error
                    }
                });

        // Set up buttons
        Button addProductButton = findViewById(R.id.add_product_button);
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedProduct = (String) productSpinner.getSelectedItem();
                String[] productNameAndPriceAndQty = selectedProduct.split(" - ");
                String productName = productNameAndPriceAndQty[0];
                int productQty = Integer.parseInt(productNameAndPriceAndQty[2].substring(5));
                if (selectedProductsMap.containsKey(productName)) {
                    // Increment the quantity if the product already exists
                    selectedProductsMap.put(productName, selectedProductsMap.get(productName) + 1);
                } else {
                    // Add the product to the map with a quantity of 1 if it doesn't exist
                    selectedProductsMap.put(productName, 1);
                }
                // Clear the adapter and re-populate it with the contents of the map
                selectedProductsAdapter.clear();
                for (String name : selectedProductsMap.keySet()) {
                    selectedProductsAdapter.add(new Product(name, selectedProductsMap.get(name)));
                }
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
                // Creating the list document
                Map<String, Object> list = new HashMap<>();
                list.put("name", listName);
                list.put("email", userEmail);

                // Adding selected products to the list
                ArrayList<Map<String, Object>> selectedProductsList = new ArrayList<>();
                double totalPrice = 0; // new variable to keep track of total price
                for (int i = 0; i < selectedProductsAdapter.getCount(); i++) {
                    Product product = selectedProductsAdapter.getItem(i);
                    Map<String, Object> selectedProduct = new HashMap<>();
                    selectedProduct.put("name", product.name);
                    selectedProduct.put("quantity", product.quantity);
                    // Add the price to the selected product map
                    String selectedProductText = productSpinner.getItemAtPosition(i).toString();
                    String[] productNameAndPriceAndQty = selectedProductText.split(" - ");
                    Double productPrice = Double.parseDouble(productNameAndPriceAndQty[1].substring(2)); // get the price from the spinner text
                    selectedProduct.put("price", productPrice);
                    selectedProductsList.add(selectedProduct);
                    totalPrice += productPrice * product.quantity; // update the total price
                }
                list.put("products", selectedProductsList);

                // Format the total price to 2 decimal places
                String formattedTotalPrice = String.format("%.2f", totalPrice);

                // Add the formatted total price to the list document
                list.put("total_price", formattedTotalPrice);

                DocumentReference docRef = firestore.collection("lists").document();
                docRef.set(list)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CreatedListActivity.this, "List created successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CreatedListActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CreatedListActivity.this, "Error creating list", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        //Button to return to the Home Screen
        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreatedListActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set up text views
        TextView welcomeTextView = findViewById(R.id.welcome_text_view);
        welcomeTextView.setText(R.string.create_list_title);

        TextView listNameLabelTextView = findViewById(R.id.list_name_edit_text);
        listNameLabelTextView.setText(R.string.list_name_label);
    }
}