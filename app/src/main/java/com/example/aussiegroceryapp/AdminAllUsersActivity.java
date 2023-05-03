package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminAllUsersActivity extends AppCompatActivity {

    private ListView userListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_users);

        Button homeButton = findViewById(R.id.home_button);
        userListView = findViewById(R.id.user_list_view);
        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Create a reference to the "users" collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> emails = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String email = document.getString("email");
                            emails.add(email);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_list_item_1, emails);
                        userListView.setAdapter(adapter);
                    } else {
                        // Handle errors
                    }

                    userListView.setOnItemClickListener((parent, view, position, id) -> {
                        String email = (String) parent.getItemAtPosition(position);
                        Intent intent = new Intent(AdminAllUsersActivity.this, AdminUsersDetailsActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    });

                });

        //Button to return to the Admin Home Screen
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminAllUsersActivity.this, AdminHomeActivity.class);
            startActivity(intent);
        });
    }
}