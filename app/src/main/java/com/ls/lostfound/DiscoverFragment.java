package com.ls.lostfound;

import android.text.Editable;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ls.lostfound.R;
import java.util.ArrayList;
import java.util.List;
import models.LostAndFoundItem;
import models.ItemAdapter;
import models.SearchAdapter;
public class DiscoverFragment extends Fragment implements ItemAdapter.OnDeleteListener, ItemAdapter.OnEditListener,OnMapReadyCallback {
    private List<LostAndFoundItem> itemList = new ArrayList<>();
    private List<LostAndFoundItem> listOfItems; // Your list of items

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ItemAdapter adapter;
    private SearchAdapter searchAdapter;

    private EditText searchEditText; // Use this class member instead of declaring it again

    private static final String TAG = "DiscoverFragment";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemsCollection = db.collection("lostAndFoundItems");
    private SupportMapFragment mapFragment;
    private FloatingActionButton fabShowMap;
    private boolean isMapVisible = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getView().setVisibility(View.GONE); // Ensure map is not visible when the fragment is created
        }
        fabShowMap = view.findViewById(R.id.fab_show_map);

        listOfItems = getYourListOfItems();

        fabShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMapVisibility();
            }
        });

        mapFragment.getMapAsync(this); // This line sets the callback for when the map is ready


        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        searchEditText = view.findViewById(R.id.editTextSearch); // Use the class member variable

        // Set up your RecyclerView with an adapter
        adapter = new ItemAdapter(itemList, requireContext(), false, this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize the search adapter here or after fetching data
        searchAdapter = new SearchAdapter(new ArrayList<>(itemList)); // Initialize with an empty list or fetched list

        // Set up swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            refreshData();
        });

        // Fetch lost and found items from Firestore and update the RecyclerView
        fetchItemsFromFirestore();

        // Add the search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString();
                if (searchText.isEmpty()) {
                    recyclerView.setAdapter(adapter); // Switch back to the original adapter
                } else {
                    recyclerView.setAdapter(searchAdapter); // Use the search adapter when searching
                }
                searchAdapter.getFilter().filter(s);
            }
        });

        return view;
    }

    private void fetchItemsFromFirestore() {
        db.collection("lostAndFoundItems")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        itemList.clear(); // Clear the previous data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            LostAndFoundItem item = document.toObject(LostAndFoundItem.class);
                            item.setDocumentId(document.getId()); // Set the document ID on the object
                            itemList.add(item);
                        }

                        // Update the search adapter with the full list
                        searchAdapter = new SearchAdapter(new ArrayList<>(itemList));
                        listOfItems = new ArrayList<>(itemList); // Update the list used by the map

                        // Notify both adapters of the data change
                        adapter.notifyDataSetChanged();
                        searchAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });


    }
    private void toggleMapVisibility() {
        View mapView = getActivity().findViewById(R.id.map);
        View recyclerView = getActivity().findViewById(R.id.recyclerView);

        if (mapView != null && recyclerView != null) {
            if (isMapVisible) {
                mapView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                fabShowMap.setImageResource(R.drawable.baseline_streetview_24);
            } else {
                recyclerView.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                fabShowMap.setImageResource(R.drawable.baseline_list_24);
            }
            isMapVisible = !isMapVisible; // Toggle the state only once here
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        for (LostAndFoundItem item : listOfItems) {
            LatLng itemLocation = new LatLng(item.getLatitude(), item.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(itemLocation).title(item.getItemName()));
        }

        if (!listOfItems.isEmpty()) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(listOfItems.get(0).getLatitude(), listOfItems.get(0).getLongitude()), 10));
        }
    }

    private List<LostAndFoundItem> getYourListOfItems() {
        // Retrieve your items from the database or data source
        return new ArrayList<>(); // Placeholder for your data retrieval logic
    }

    private void refreshData() {
        fetchItemsFromFirestore();
    }

    @Override
    public void onEditClicked(LostAndFoundItem item) {

    }

    @Override
    public void onDeleteClicked(LostAndFoundItem item) {

    }
}
