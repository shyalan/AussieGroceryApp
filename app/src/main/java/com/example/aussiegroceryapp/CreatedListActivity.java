package com.example.aussiegroceryapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CreatedListActivity extends AppCompatActivity {

    private EditText listNameEditText;
    private Spinner productSpinner;
    private ListView selectedProductsListView;
    private ArrayAdapter<String> selectedProductsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created_list);

        // Get references to UI elements
        listNameEditText = findViewById(R.id.list_name_edit_text);
        productSpinner = findViewById(R.id.product_spinner);
        selectedProductsListView = findViewById(R.id.selected_products_listview);
        selectedProductsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        selectedProductsListView.setAdapter(selectedProductsAdapter);

        // Set up spinner
        ArrayAdapter<CharSequence> productAdapter = ArrayAdapter.createFromResource(this, R.array.product_array, android.R.layout.simple_spinner_item);
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productSpinner.setAdapter(productAdapter);

        // Set up buttons
        Button addProductButton = findViewById(R.id.add_product_button);
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedProduct = (String) productSpinner.getSelectedItem();
                selectedProductsAdapter.add(selectedProduct);
                selectedProductsAdapter.notifyDataSetChanged();
            }
        });

        Button finishButton = findViewById(R.id.finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something with selected products
            }
        });

        // Set up text views
        TextView welcomeTextView = findViewById(R.id.welcome_text_view);
        welcomeTextView.setText(R.string.create_list_title);

        TextView listNameLabelTextView = findViewById(R.id.list_name_edit_text);
        listNameLabelTextView.setText(R.string.list_name_label);

    }
}