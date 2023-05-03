package com.example.aussiegroceryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class TopStoreActivity extends AppCompatActivity {
    private Button backButton;
    private Button listButton;
    private MapView mapView;

    private static final int REQUEST_LOCATION_PERMISSIONS = 1001;
    private static final double MAX_ZOOM_OUT = 5.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topstore);

        backButton = findViewById(R.id.back_button);
        listButton = findViewById(R.id.list_view_button);
        mapView = (MapView) findViewById(R.id.map_view);

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Set click listener for back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set click listener for list button
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopStoreActivity.this, TopStoreListViewActivity.class);
                startActivity(intent);
            }
        });

        // Set up map view
        Configuration.getInstance().load(getApplicationContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setMinZoomLevel(MAX_ZOOM_OUT);

        // Set up location overlay
        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()), mapView);
        mapView.getOverlays().add(locationOverlay);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        locationOverlay.enableFollowLocation();
        mapView.getController().setZoom(15.0);


        // Check location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS);
        } else {
            // Permissions granted, get stores from Firestore and add markers
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("stores")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                // Get store data
                                String storeName = document.getString("name");
                                double latitude = document.getDouble("latitude");
                                double longitude = document.getDouble("longitude");

                                // Create marker and add to map
                                if (mapView != null) {
                                    GeoPoint storeLocation = new GeoPoint(latitude, longitude);
                                    Marker storeMarker = new Marker(mapView);
                                    storeMarker.setPosition(storeLocation);
                                    storeMarker.setTitle(storeName);
                                    mapView.getOverlays().add(storeMarker);
                                    storeMarker.setTitle(storeName);

                                    // Add OnClickListener to marker
                                    storeMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                                            // Create intent to start StoreProductsActivity
                                            Intent intent = new Intent(TopStoreActivity.this, StoreProductsActivity.class);
                                            // Add storeName as extra
                                            intent.putExtra("storeName", marker.getTitle());
                                            startActivity(intent);
                                            return true;
                                        }
                                    });

                                    // Add text below marker
                                    storeMarker.setSubDescription("View Products");
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed to get stores from Database", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
