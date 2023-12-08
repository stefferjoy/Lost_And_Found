package com.ls.lostfound;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.QuerySnapshot;
import com.ls.lostfound.chat.ChatActivity;
import com.ls.lostfound.chat.Message;
import com.ls.lostfound.chat.MessageAdapter;
import com.ls.lostfound.models.LostAndFoundItem;
import com.ls.lostfound.models.ItemAdapter;
import com.ls.lostfound.models.RecyclerView.LostAndFoundAdapter;
import com.ls.lostfound.models.SearchAdapter;
import com.ls.lostfound.notification.NotificationAdapter;
import com.ls.lostfound.notification.NotificationItem;

import org.jetbrains.annotations.Nullable;
import android.content.Intent;
import android.util.Log;


public class DiscoverFragment extends Fragment implements ItemAdapter.OnDeleteListener, ItemAdapter.OnEditListener,OnMapReadyCallback,MessageAdapter.OnItemClickListener  {
    private List<LostAndFoundItem> itemList = new ArrayList<>();
    private List<LostAndFoundItem> listOfItems;

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
    private DiscoverFragment discoverFragment;

    private RecyclerView messageRecyclerView ;

    private Context context;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>(); // Initialize an empty list of messages


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onItemClick(int position) {
        LostAndFoundItem selectedItem = itemList.get(position);
        String receiverUserId = selectedItem.getUserId(); // Get the receiver user ID from the selected item

        if (receiverUserId != null && !receiverUserId.isEmpty()) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("RECEIVER_USER_ID", receiverUserId);
            startActivity(intent);
        } else {
            Log.e(TAG, "Receiver User ID is null or empty for the selected item");
            // Handle the case where receiverUserId is null or empty
        }
    }



    private String determineChatId(LostAndFoundItem item) {
        String chatId = "CHAT_" + item.getDocumentId();
        Log.d(TAG, "Determined Chat ID: " + chatId);
        return chatId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        FloatingActionButton chatButton = view.findViewById(R.id.chatButton);


        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getView().setVisibility(View.GONE); // Ensure map is not visible when the fragment is created
        }
        fabShowMap = view.findViewById(R.id.fab_show_map);


        listOfItems = getYourListOfItems();


        fabShowMap.setOnClickListener(v -> {
            MapBottomSheetFragment bottomSheet = new MapBottomSheetFragment(listOfItems);
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });


        mapFragment.getMapAsync(this); // This line sets the callback for when the map is ready


        recyclerView = view.findViewById(R.id.recyclerView);


        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        searchEditText = view.findViewById(R.id.editTextSearch); // Use the class member variable

        // Set up your RecyclerView with an adapter
        adapter = new ItemAdapter(itemList, requireContext(), false, this, this);
        // Inside your onCreateView method in DiscoverFragment
        messageAdapter = new MessageAdapter(messages);

        recyclerView.setAdapter(messageAdapter);


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



        chatButton.setOnClickListener(v -> {
            if (!itemList.isEmpty()) {
                LostAndFoundItem firstItem = itemList.get(0);
                String chatId = determineChatId(firstItem); // Make sure this is not null
                String receiverUserId = firstItem.getUserId(); // Make sure this is not null
                Log.d(TAG, "Starting ChatActivity with Chat ID: " + chatId);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("RECEIVER_USER_ID", receiverUserId); // Ensure receiverUserId is not null here
                intent.putExtra("CHAT_ID", chatId); // Pass chatId as well
                startActivity(intent);
                Log.d(TAG, "Chat ID: " + chatId);
            } else {
                Log.d(TAG, "Item list is empty, cannot start chat.");
            }
        });

        /*
        // Set the onItemClickListener for your ItemAdapter
        adapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                LostAndFoundItem selectedItem = itemList.get(position);
                String receiverUserId = selectedItem.getUserId(); // Make sure this is not null

                Log.d(TAG, "Item Clicked. Receiver User ID: " + receiverUserId);

                if (receiverUserId != null && !receiverUserId.isEmpty()) {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("RECEIVER_USER_ID", receiverUserId);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "Receiver User ID is null or empty for the selected item");
                    // Handle the case where receiverUserId is null or empty
                }
            }

        });

 */

        adapter.setOnPostClickListener(new ItemAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(int position) {
                LostAndFoundItem clickedItem = itemList.get(position);
                String chatId = determineChatId(clickedItem); // Make sure this is not null

                String receiverUserId = clickedItem.getUserId(); // Make sure this is not null
                Log.d(TAG, "Starting ChatActivity with Chat ID: " + chatId);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("RECEIVER_USER_ID", receiverUserId); // Ensure receiverUserId is not null here
                intent.putExtra("CHAT_ID", chatId); // Pass chatId as well
                startActivity(intent);


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



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
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
    @Override
    public void onStop() {
        super.onStop();
        // Call onStop method in your Fragment
        if (discoverFragment != null) {
            discoverFragment.onStop();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        // Call onResume method in your Fragment
        if (discoverFragment != null) {
            discoverFragment.onResume();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        // Call onPause method in your Fragment
        if (discoverFragment != null) {
            discoverFragment.onPause();
        }
    }






}
