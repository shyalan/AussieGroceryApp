package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class StoreProductsActivity extends AppCompatActivity {

    private TextView titleTextView;
    private RecyclerView recyclerView;
    private List<Product> productList;
    private ProductAdapter adapter;
    private String storeName;
    private LinearLayout homeLayout;
    private LinearLayout backLayout;

    public class Product {
        private String name;
        private double price;
        private String imageUrl;

        public Product(String name, double price, String imageUrl) {
            this.name = name;
            this.price = price;
            this.imageUrl = imageUrl;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_products);

        // Get store name passed from previous activity
        storeName = getIntent().getStringExtra("storeName");

        // Get references to UI elements
        titleTextView = findViewById(R.id.welcome_text_view);
        recyclerView = findViewById(R.id.recycler_view);
        backLayout = findViewById(R.id.back_layout);
        homeLayout = findViewById(R.id.home_layout);

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Set the title of the activity to the store name
        titleTextView.setText(storeName);

        // Initialize the product list
        productList = new ArrayList<>();

        // Create an instance of ItemDecoration
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_view_spacing);
        RecyclerView.ItemDecoration itemDecoration = new SpacesItemDecoration(spacingInPixels);

        // Set ItemDecoration on the RecyclerView
        recyclerView.addItemDecoration(itemDecoration);


        // Set up the adapter and connect it to the recycler view
        adapter = new ProductAdapter(productList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Getting data and populating the product list
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereEqualTo("storeName", storeName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String name = documentSnapshot.getString("name");
                            double price = documentSnapshot.getDouble("price");
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            Product product = new Product(name, price, imageUrl);
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

        // Set up click listeners for the nav
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreProductsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    // Custom adapter for the product list
    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

        private List<Product> productList;

        public ProductAdapter(List<Product> productList) {
            this.productList = productList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.latest_deals, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = productList.get(position);

            holder.nameTextView.setText(product.getName());
            holder.priceTextView.setText("$" + product.getPrice());
            Picasso.get().load(product.getImageUrl()).into(holder.imageView);

            holder.addToListButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StoreProductsActivity.this, CreatedListActivity.class);
                    // Pass any necessary data to the CreatedListActivity
                    intent.putExtra("productName", product.getName());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView priceTextView;
            ImageView imageView;
            Button addToListButton;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.product_name);
                priceTextView = itemView.findViewById(R.id.product_price);
                imageView = itemView.findViewById(R.id.product_image);
                addToListButton = itemView.findViewById(R.id.add_button);
            }
        }
    }
}
