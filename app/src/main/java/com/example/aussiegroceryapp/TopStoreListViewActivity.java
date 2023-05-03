package com.example.aussiegroceryapp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TopStoreListViewActivity extends AppCompatActivity {

    private ListView listView;
    private Button backButton, mapViewButton;
    private ArrayList<String> storeList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_store_list_view);

        // Get references to UI elements
        listView = findViewById(R.id.list_view);
        backButton = findViewById(R.id.back_button);
        mapViewButton = findViewById(R.id.map_view_button);

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Initialize the storeList ArrayList
        storeList = new ArrayList<>();

        // Getting data and populating the list view
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("stores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                storeList.add(name);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


        // Set up the adapter and connect it to the list view
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, storeList);
        listView.setAdapter(adapter);

        // Set up a click listener for the list view items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String storeName = storeList.get(position);
                Intent intent = new Intent(TopStoreListViewActivity.this, StoreProductsActivity.class);
                intent.putExtra("storeName", storeName);
                startActivity(intent);
            }
        });

        // Set up click listeners for the buttons
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopStoreListViewActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        mapViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopStoreListViewActivity.this, TopStoreActivity.class);
                startActivity(intent);
            }
        });
    }
}
