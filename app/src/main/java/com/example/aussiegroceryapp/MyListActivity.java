package com.example.aussiegroceryapp;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyListActivity extends AppCompatActivity {

    private ListView createdListView;
    private ArrayAdapter<SpannableStringBuilder> createdListAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private QuerySnapshot querySnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylist);

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

        //Button to delete lists with the same email as logged in user
        Button deleteListButton = findViewById(R.id.delete_list_button);
        deleteListButton.setOnClickListener(v -> {
            String loggedInUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if (loggedInUserEmail != null) {
                CollectionReference listsRef = db.collection("lists");
                listsRef.whereEqualTo("email", loggedInUserEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    document.getReference().delete();
                                }
                                Toast.makeText(MyListActivity.this, "Lists deleted successfully!", Toast.LENGTH_SHORT).show();
                                createdListAdapter.clear();
                            } else {
                                Toast.makeText(MyListActivity.this, "No lists found!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle error
                            Toast.makeText(MyListActivity.this, "Error occurred while fetching data!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // ListView to show all the created lists and their products
        createdListView = findViewById(R.id.created_list_view);
        createdListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        createdListView.setAdapter(createdListAdapter);

        // Fetching the data from the Firestore database and populating the ListView
        String loggedInUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (loggedInUserEmail != null) {
            CollectionReference listsRef = db.collection("lists");
            listsRef.whereEqualTo("email", loggedInUserEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            List<SpannableStringBuilder> listData = new ArrayList<>();
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Map<String, Object> data = document.getData();
                                String listName = data.get("name") != null ? data.get("name").toString() : "";
                                SpannableStringBuilder sb = new SpannableStringBuilder(listName + "\n");
                                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                                sb.setSpan(boldSpan, 0, listName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                for (String key : data.keySet()) {
                                    if (!key.equals("name") && !key.equals("email")) { // Don't show email
                                        Object value = data.get(key);
                                        sb.append(String.format("\t%s\n", value != null ? value.toString() : "null"));
                                    }
                                }
                                listData.add(sb);
                            }
                            createdListAdapter.addAll(listData);
                        }
                    } else {
                        // Handle error
                        Toast.makeText(MyListActivity.this, "Error occurred while fetching data!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Handling click events on ListView items
            createdListView.setOnItemClickListener((parent, view, position, id) -> {
                // Get the selected list from the adapter
                SpannableStringBuilder selectedList = createdListAdapter.getItem(position);

                // Parse the name of the selected list
                String listName = selectedList.toString().split("\n")[0];

                // Open the SelectedListActivity for the selected list
                Intent intent = new Intent(MyListActivity.this, CreatedListActivity.class);
                intent.putExtra("listName", listName);
                startActivity(intent);
            });
        }
    }
}