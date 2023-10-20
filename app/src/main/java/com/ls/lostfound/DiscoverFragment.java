package com.ls.lostfound;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ls.lostfound.R;
import java.util.ArrayList;
import java.util.List;
import models.LostAndFoundItem;
import models.ItemAdapter;

public class DiscoverFragment extends Fragment {
    private List<LostAndFoundItem> itemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ItemAdapter adapter; // Declare an ItemAdapter

    private static final String TAG = "DiscoverFragment";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemsCollection = db.collection("lostAndFoundItems");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Set up your RecyclerView with an adapter
        adapter = new ItemAdapter(itemList, requireContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set up swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Handle the refresh action here
            refreshData();
        });

        // Fetch lost and found items from Firestore and update the RecyclerView
        fetchItemsFromFirestore();

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
                            itemList.add(item);
                        }
                        // Notify the adapter of the data change
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle the error
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }

                    // Finish the refresh operation
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void refreshData() {
        // Implement the logic to refresh your data
        // For example, you can call fetchItemsFromFirestore() again
        fetchItemsFromFirestore();
    }
}
