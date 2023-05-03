package com.example.aussiegroceryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_details);
        // Get the listName and email extras from the intent
        String listName = getIntent().getStringExtra("listName");
        String email = getIntent().getStringExtra("email");
        Double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        //Button to return to the Home Screen
        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ListDetailsActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Find the welcome_text_view and replace its text with the listName
        TextView welcomeTextView = findViewById(R.id.welcome_text_view);
        welcomeTextView.setText(listName);

        // Find the total_text_view and update its text with the totalPrice
        final TextView totalTextView = findViewById(R.id.total_text_view);
        totalTextView.setText("Total Price: $" + totalPrice);

        // Find the list_view
        final ListView listView = findViewById(R.id.products_list_view);

        // Initialize the Firestore database
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Populating the ListView
        db.collection("lists")
                .whereEqualTo("email", email)
                .whereEqualTo("listName", listName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Map<String, String>> productsList = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List<Map<String, Object>> products = (List<Map<String, Object>>) document.get("products");
                                for (Map<String, Object> product : products) {
                                    String name = (String) product.get("name");
                                    String price = String.valueOf(product.get("price"));
                                    String quantity = String.valueOf(product.get("quantity"));

                                    Map<String, String> productMap = new HashMap<>();
                                    productMap.put("name", name);
                                    productMap.put("price", price);
                                    productMap.put("quantity", quantity);

                                    productsList.add(productMap);
                                }
                            }
                            String[] from = {"name", "price", "quantity"};
                            int[] to = {R.id.product_name_text_view, R.id.product_price_text_view, R.id.product_quantity_text_view};

                            final SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), productsList, R.layout.activity_product_item, from, to);
                            listView.setAdapter(adapter);

                            // Set OnItemClickListener to the ListView to remove the selected product
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    // Get the selected item from the adapter
                                    final Map<String, String> selectedItem = (Map<String, String>) adapter.getItem(position);

                                    // Create an alert dialog to confirm the removal of the selected item
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ListDetailsActivity.this);
                                    builder.setMessage("Are you sure you want to remove this product?")
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // Remove the selected item from the productsList
                                                    productsList.remove(position);

                                                    // Update the adapter
                                                    adapter.notifyDataSetChanged();

                                                    // Update the total price
                                                    totalTextView.setText("Total Price: $" + (totalPrice - Double.parseDouble(selectedItem.get("price")) * Double.parseDouble(selectedItem.get("quantity"))));
                                                    totalTextView.setText("Total Price: $" + totalPrice);

                                                    // Update the Firestore database
                                                    db.collection("lists")
                                                            .whereEqualTo("email", email)
                                                            .whereEqualTo("listName", listName)
                                                            .get()
                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                                            String documentId = document.getId();
                                                                            db.collection("lists").document(documentId)
                                                                                    .update("products", productsList)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            Toast.makeText(ListDetailsActivity.this, "Product removed successfully", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    })
                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            Toast.makeText(ListDetailsActivity.this, "Error removing product", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    } else {
                                                                        Toast.makeText(ListDetailsActivity.this, "Error updating database", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // User cancelled the dialog
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            });

                        }
                    }
                });

        // Find the delete_list_button and set a click listener to delete the entire list
        findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an alert dialog to confirm the removal of the list
                AlertDialog.Builder builder = new AlertDialog.Builder(ListDetailsActivity.this);
                builder.setMessage("Are you sure you want to delete this list?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Delete the list from the Firestore database
                                db.collection("lists")
                                        .whereEqualTo("email", email)
                                        .whereEqualTo("listName", listName)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        String documentId = document.getId();
                                                        db.collection("lists").document(documentId)
                                                                .delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(ListDetailsActivity.this, "List deleted successfully", Toast.LENGTH_SHORT).show();
                                                                        // Return to the MyListActivity
                                                                        Intent intent = new Intent(ListDetailsActivity.this, MyListActivity.class);
                                                                        startActivity(intent);
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(ListDetailsActivity.this, "Error deleting list", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                } else {
                                                    Toast.makeText(ListDetailsActivity.this, "Error deleting list", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }
}