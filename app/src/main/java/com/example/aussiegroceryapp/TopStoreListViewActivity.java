package com.example.aussiegroceryapp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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
    private ArrayList<String> storeList;
    private CustomAdapter adapter;
    private LinearLayout homeLayout;
    private LinearLayout mapLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_store_list_view);

        // Get references to UI elements
        listView = findViewById(R.id.list_view);
        homeLayout = findViewById(R.id.home_layout);
        mapLayout = findViewById(R.id.map_layout);

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
        adapter = new CustomAdapter(storeList);
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
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopStoreListViewActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        mapLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopStoreListViewActivity.this, TopStoreActivity.class);
                startActivity(intent);
            }
        });
    }

    private class CustomAdapter extends ArrayAdapter<String> {

        public CustomAdapter(ArrayList<String> data) {
            super(TopStoreListViewActivity.this, R.layout.list_item, data);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = getLayoutInflater();
                view = inflater.inflate(R.layout.list_item, parent, false);
            }

            TextView storeNameTextView = view.findViewById(R.id.list_name_text_view);
            storeNameTextView.setText(storeList.get(position));

            return view;
        }
    }
}
