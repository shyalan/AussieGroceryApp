package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class LatestDealsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView DealsListView;
    private ArrayAdapter<SpannableStringBuilder> DealsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest_deals);

        // Find views by ID
        ImageView logoImageView = findViewById(R.id.logo_image_view);
        TextView welcomeTextView = findViewById(R.id.welcome_text_view);
        Button backButton = findViewById(R.id.back_button);

        // ListView to show all the created lists and their products
        DealsListView = findViewById(R.id.list_view);
        DealsListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        DealsListView.setAdapter(DealsListAdapter);

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Set text for welcome TextView
        welcomeTextView.setText("Latest Deals");

        // Retrieve data from Firestore and display in ListView
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference topProductsRef = db.collection("top_products");
        topProductsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String name = documentSnapshot.getString("name");
                double price = documentSnapshot.getDouble("price");
                String item = name + " - $" + price;
                SpannableStringBuilder sb = new SpannableStringBuilder(item);
                DealsListAdapter.add(sb);
            }
        }).addOnFailureListener(e -> {
            // Handle any errors
        });

        DealsListView.setOnItemClickListener(this);

        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LatestDealsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String selectedProduct = DealsListAdapter.getItem(position).toString();
        Intent intent = new Intent(LatestDealsActivity.this, CreatedListActivity.class);
        intent.putExtra("selected_product", selectedProduct);
        startActivity(intent);
    }
}