package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminUsersDetailsActivity extends AppCompatActivity {

    private ListView userListView;
    private Button homeButton;
    private Button backButton;
    private Button enableButton;
    private Button disableButton;
    private Button deleteButton;
    private FirebaseFirestore db;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users_detials);

        homeButton = findViewById(R.id.home_button);
        backButton = findViewById(R.id.back_button);
        deleteButton = findViewById(R.id.delete_button);
        disableButton = findViewById(R.id.disable_button);
        enableButton = findViewById(R.id.enable_button);
        userListView = findViewById(R.id.user_list_view);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        // Create a reference to the "users" collection
        db = FirebaseFirestore.getInstance();

        // Query the "users" collection to get the documents that match the email
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> user = document.getData();
                            userList.add(user);
                        }
                        ArrayAdapter<Map<String, Object>> adapter = new ArrayAdapter<Map<String, Object>>(this, 0, userList) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                if (convertView == null) {
                                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);
                                }

                                Map<String, Object> user = getItem(position);

                                TextView emailTextView = convertView.findViewById(R.id.email_text_view);
                                TextView fullNameTextView = convertView.findViewById(R.id.full_name_text_view);
                                TextView addressTextView = convertView.findViewById(R.id.address_text_view);

                                emailTextView.setText(user.get("email").toString());
                                fullNameTextView.setText(user.get("fullName").toString());
                                addressTextView.setText(user.get("address").toString());

                                return convertView;
                            }
                        };
                        userListView.setAdapter(adapter);
                    } else {
                        // Handle errors
                    }
                });

        //Button to return to the Admin All Users Screen
        backButton.setOnClickListener(v -> {
            Intent backIntent = new Intent(AdminUsersDetailsActivity.this, AdminAllUsersActivity.class);
            startActivity(backIntent);
        });

        //Button to return to the Admin Home Screen
        homeButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(AdminUsersDetailsActivity.this, AdminHomeActivity.class);
            startActivity(homeIntent);
        });

        //Button to delete the user with that email
        deleteButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminUsersDetailsActivity.this);
            builder.setTitle("Confirm Deletion");
            builder.setMessage("Are you sure you want to delete this user?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().delete();
                                }
                                Intent backIntent = new Intent(AdminUsersDetailsActivity.this, AdminAllUsersActivity.class);
                                startActivity(backIntent);
                            } else {
                                // Handle errors
                            }
                        });
            });
            builder.setNegativeButton("No", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //Button to disable the user with that email
        disableButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminUsersDetailsActivity.this);
            builder.setTitle("Confirm Disabling");
            builder.setMessage("Are you sure you want to disable this user?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().update("disabled", true);
                                }
                                Intent backIntent = new Intent(AdminUsersDetailsActivity.this, AdminAllUsersActivity.class);
                                startActivity(backIntent);
                            } else {
                                // Handle errors
                            }
                        });
            });
            builder.setNegativeButton("No", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //Button to enable back the user
        enableButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminUsersDetailsActivity.this);
            builder.setTitle("Confirm Enabling");
            builder.setMessage("Are you sure you want to enable this user?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().update("disabled", false);
                                }
                                Intent backIntent = new Intent(AdminUsersDetailsActivity.this, AdminAllUsersActivity.class);
                                startActivity(backIntent);
                            } else {
                                // Handle errors
                            }
                        });
            });
            builder.setNegativeButton("No", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        });

    }
}
