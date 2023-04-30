package com.example.aussiegroceryapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyListActivity extends AppCompatActivity {

    private ListView createdListView;
    private ArrayAdapter<SpannableStringBuilder> createdListAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylist);

        // Initialize the createdListView object
        createdListView = findViewById(R.id.created_list_view);

        //Button to return to the Home Screen
        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyListActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        //Button to create list
        Button createListButton = findViewById(R.id.create_list_button);
        createListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyListActivity.this, CreatedListActivity.class);
            startActivity(intent);
        });

        // Create a reference to the "lists" collection in Firestore
        CollectionReference listsRef = db.collection("lists");

        // Get the logged-in user's email address
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Create a query to get all the lists with the logged-in user's email address
        Query query = listsRef.whereEqualTo("email", userEmail);

        // Execute the query
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Create a list to store the data for each list
                List<SpannableStringBuilder> listData = new ArrayList<>();

                // Loop through the query results and add the listName and total price to the listData
                for (DocumentSnapshot document : task.getResult()) {
                    Map<String, Object> list = document.getData();
                    String listName = list.get("listName").toString();
                    double totalPrice = Double.parseDouble(list.get("totalPrice").toString());
                    String totalPriceString = String.format("$%.2f", totalPrice);
                    SpannableStringBuilder listItem = new SpannableStringBuilder(listName + " - " + totalPriceString);
                    listItem.setSpan(new StyleSpan(Typeface.BOLD), 0, listName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    listData.add(listItem);
                }

                // If Clicked on
                createdListAdapter = new ArrayAdapter<>(MyListActivity.this, android.R.layout.simple_list_item_1, listData);
                createdListView.setAdapter(createdListAdapter);

                // Set the click listener on the ListView
                createdListView.setOnItemClickListener((parent, view, position, id) -> {
                    SpannableStringBuilder selectedItem = (SpannableStringBuilder) parent.getItemAtPosition(position);
                    String selectedListName = selectedItem.toString().split(" - ")[0];
                    double totalPrice = Double.parseDouble(selectedItem.toString().split(" - ")[1].replace("$", ""));
                    Intent intent = new Intent(MyListActivity.this, ListDetailsActivity.class);
                    intent.putExtra("listName", selectedListName);
                    intent.putExtra("totalPrice", totalPrice);
                    intent.putExtra("email", userEmail);
                    startActivity(intent);
                });
            }

            else {
                // Display an error message if the query fails
                Toast.makeText(MyListActivity.this, "Error getting lists: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

