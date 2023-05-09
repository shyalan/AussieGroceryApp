package com.example.aussiegroceryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        public double price;

        public Product(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        @Override
        public String toString() {
            return name + (quantity > 0 ? " - Qty: " + quantity : "");
        }
    }

    private Map<String, Integer> selectedProductsMap;
    private ArrayAdapter<Product> selectedProductsAdapter;
    private double totalPrice = 0;

    // Update total price
    private void updateTotalPrice() {
        totalPrice = 0;
        for (Map.Entry<String, Integer> entry : selectedProductsMap.entrySet()) {
            String productText = entry.getKey();
            int productQty = entry.getValue();
            String[] selectedProductSplit = productText.split(" - \\$");
            double productPrice = Double.parseDouble(selectedProductSplit[1].trim());
            totalPrice += productPrice * productQty;
        }
        TextView totalPriceTextView = findViewById(R.id.total_text_view);
        totalPriceTextView.setText(String.format("Total Price: $%.2f", totalPrice));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created_list);

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        totalPrice = 0;

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
                            String productText = productName + " - $" + productPrice;
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

        //Button to add selected product to the list
        Button addButton = findViewById(R.id.add_product_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedProductText = productSpinner.getSelectedItem().toString();
                int selectedProductQty = 1;

                // Check if the product is already in the list
                if (selectedProductsMap.containsKey(selectedProductText)) {
                    selectedProductQty = selectedProductsMap.get(selectedProductText) + 1;
                }
                selectedProductsMap.put(selectedProductText, selectedProductQty);
                selectedProductsAdapter.clear();
                for (Map.Entry<String, Integer> entry : selectedProductsMap.entrySet()) {
                    String productText = entry.getKey();
                    int productQty = entry.getValue();
                    selectedProductsAdapter.add(new Product(productText, productQty, 0.0));
                }

                selectedProductsAdapter.notifyDataSetChanged();

                updateTotalPrice();
            }

            // Update total price
            private void updateTotalPrice() {
                totalPrice = 0;
                for (Map.Entry<String, Integer> entry : selectedProductsMap.entrySet()) {
                    String productText = entry.getKey();
                    int productQty = entry.getValue();
                    String[] selectedProductSplit = productText.split(" - \\$");
                    double productPrice = Double.parseDouble(selectedProductSplit[1].trim());
                    totalPrice += productPrice * productQty;
                }
                TextView totalPriceTextView = findViewById(R.id.total_text_view);
                totalPriceTextView.setText(String.format("Total Price: $%.2f", totalPrice));
            }
        });

        selectedProductsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected product and remove it from the map
                Product selectedProduct = selectedProductsAdapter.getItem(position);
                selectedProductsMap.remove(selectedProduct.name);

                // Update the adapter with the new data
                selectedProductsAdapter.clear();
                for (Map.Entry<String, Integer> entry : selectedProductsMap.entrySet()) {
                    String productText = entry.getKey();
                    int productQty = entry.getValue();
                    selectedProductsAdapter.add(new Product(productText, productQty, 0.0));
                }
                selectedProductsAdapter.notifyDataSetChanged();

                // Update the total price
                updateTotalPrice();
            }

            // Update total price
            private void updateTotalPrice() {
                totalPrice = 0;
                for (Map.Entry<String, Integer> entry : selectedProductsMap.entrySet()) {
                    String productText = entry.getKey();
                    int productQty = entry.getValue();
                    String[] selectedProductSplit = productText.split(" - \\$");
                    double productPrice = Double.parseDouble(selectedProductSplit[1].trim());
                    totalPrice += productPrice * productQty;
                }
                TextView totalPriceTextView = findViewById(R.id.total_text_view);
                totalPriceTextView.setText(String.format("Total Price: $%.2f", totalPrice));
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

        //Button to return to the Home Screen
        Button chatButton = findViewById(R.id.chat_button);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreatedListActivity.this, AiChatActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Button to finish creating the list
        Button finishButton = findViewById(R.id.finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable button when loading
                finishButton.setEnabled(false);

                String listName = listNameEditText.getText().toString().trim();
                if (listName.isEmpty()) {
                    Toast.makeText(CreatedListActivity.this, "Please enter a list name", Toast.LENGTH_SHORT).show();
                    // Enable button when done loading
                    finishButton.setEnabled(true);
                    return;
                }

                // Check if the list name already exists
                firestore.collection("lists")
                        .whereEqualTo("email", userEmail)
                        .whereEqualTo("listName", listName)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.isEmpty()) {
                                    // List name does not exist, create a new list
                                    List<Map<String, Object>> productsList = new ArrayList<>();
                                    for (Map.Entry<String, Integer> entry : selectedProductsMap.entrySet()) {
                                        String productText = entry.getKey();
                                        int productQty = entry.getValue();
                                        String[] selectedProductSplit = productText.split(" - \\$");
                                        String productName = selectedProductSplit[0].trim();
                                        double productPrice = Double.parseDouble(selectedProductSplit[1].trim());
                                        Map<String, Object> productMap = new HashMap<>();
                                        productMap.put("name", productName);
                                        productMap.put("quantity", productQty);
                                        productMap.put("price", productPrice);
                                        productsList.add(productMap);
                                    }

                                    Map<String, Object> newList = new HashMap<>();
                                    newList.put("email", userEmail);
                                    newList.put("listName", listName);
                                    newList.put("products", productsList);
                                    newList.put("totalPrice", totalPrice);

                                    firestore.collection("lists")
                                            .add(newList)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(CreatedListActivity.this, "List created successfully", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(CreatedListActivity.this, MyListActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(CreatedListActivity.this, "Error creating list", Toast.LENGTH_SHORT).show();
                                                    // Enable button when done loading
                                                    finishButton.setEnabled(true);
                                                }
                                            });
                                } else {
                                    // List name already exists
                                    Toast.makeText(CreatedListActivity.this, "List name already exists", Toast.LENGTH_SHORT).show();
                                    // Enable button when done loading
                                    finishButton.setEnabled(true);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CreatedListActivity.this, "Error checking list name", Toast.LENGTH_SHORT).show();
                                // Enable button when done loading
                                finishButton.setEnabled(true);
                            }
                        });
            }
        });

        // Set up text views
        TextView welcomeTextView = findViewById(R.id.welcome_text_view);
        welcomeTextView.setText(R.string.create_list_title);
        listNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    listNameEditText.setHint("");
                } else {
                    listNameEditText.setHint(R.string.list_name_label);
                }
            }
        });

    }

}