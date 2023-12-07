package com.ls.lostfound;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ls.lostfound.models.LostAndFoundItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;



public class MapBottomSheetFragment extends BottomSheetDialogFragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final LatLng DEFAULT_LOCATION = new LatLng(43.4516, -80.4925); // Kitchener, Ontario, Canada
    private static final float DEFAULT_ZOOM = 15.0f;
    private List<LostAndFoundItem> items;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public void onStart() {
        super.onStart();
        checkLocationPermissionAndFetch();
    }

    private void checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchUserLocation();
        }
    }

    private void fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            setMapToDefaultLocation();
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));
                    } else {
                        setMapToDefaultLocation();
                    }
                });
    }

    private void setMapToDefaultLocation() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
    }

    public MapBottomSheetFragment(List<LostAndFoundItem> items) {
        this.items = items;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation();
            } else {
                setMapToDefaultLocation();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_bottom_sheet, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());




        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fetchUserLocation();
        } else {
            setMapToDefaultLocation();
        }
        fetchItemsAndAddMarkers();
    }


    private void addMarkerForItem(LostAndFoundItem item) {
        if (item != null && mMap != null) {
            LatLng location = new LatLng(item.getLatitude(), item.getLongitude());
            mMap.addMarker(new MarkerOptions().position(location).title(item.getItemName()));
            // Optionally, move camera or update UI
            Log.d("AddMarker", "Adding marker for item: " + item.getItemName());

        }
    }
    // Method to fetch items from Firestore and add markers
    private void fetchItemsAndAddMarkers() {
        Log.d("FirestoreQuery", "Fetching items...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("lostAndFoundItems")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        if (snapshot.exists()) {
                            LostAndFoundItem item = snapshot.toObject(LostAndFoundItem.class);
                            addMarkerForItem(item); // Add a marker for each item
                        }
                    }
                    Log.d("FirestoreSuccess", "Fetched items: " + queryDocumentSnapshots.size());

                })
                .addOnFailureListener(e -> {
                    Log.e("MapError", "Error fetching items", e);
                    // Handle any errors here

                });



    }

}
