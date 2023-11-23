package com.ls.lostfound.myAccount;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ls.lostfound.models.LostAndFoundItem;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.ls.lostfound.R;
import com.ls.lostfound.models.ItemAdapter; // Replace with the actual adapter for your items

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

public class MyPostsFragment extends Fragment implements ItemAdapter.OnEditListener, ItemAdapter.OnDeleteListener {

    public MyPostsFragment() {
        // Required empty public constructor
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private TextView textNoPosts;
    private ItemAdapter adapter;
    private List<LostAndFoundItem> userItems;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_posts, container, false);

        // Initialize TextView for 'No posts' message
        textNoPosts = rootView.findViewById(R.id.textNoPosts); // Make sure ID matches

        // Initialize RecyclerView and Adapter
        recyclerView = rootView.findViewById(R.id.recyclerViewMyPosts); // Correct ID from your layout
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        userItems = new ArrayList<>();
        adapter = new ItemAdapter(userItems, getActivity(), true, this, this);
        recyclerView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user logged in.");
            return rootView; // Early return if there's no user
        }

        String currentUserId = currentUser.getUid();
        // Fetch items posted by current user
        db.collection("lostAndFoundItems")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userItems.clear(); // Clear the list before adding new items
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            LostAndFoundItem item = document.toObject(LostAndFoundItem.class);
                            userItems.add(item);
                        }
                        adapter.setItems(userItems); // Notify the adapter of new data
                        recyclerView.setVisibility(userItems.isEmpty() ? View.GONE : View.VISIBLE);
                        textNoPosts.setVisibility(userItems.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });

        return rootView;
    }


    private void fetchItemsFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user logged in.");
            return; // Early return if there's no user
        }

        String currentUserId = currentUser.getUid();

        db.collection("lostAndFoundItems")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userItems.clear(); // Clear the list before adding new items

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            LostAndFoundItem item = document.toObject(LostAndFoundItem.class);
                            userItems.add(item);
                        }

                        // Notify the adapter that the data set has changed
                        adapter.notifyDataSetChanged();

                        // Now, set the visibility based on whether the list is empty or not
                        if (userItems.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            textNoPosts.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            textNoPosts.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }


    private void updateUIBasedOnItems() {
        if (userItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textNoPosts.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textNoPosts.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDeleteClicked(LostAndFoundItem item) {
        String documentId = item.getDocumentId(); // Use the Document ID

        if (item.getDocumentId() == null) {
            Toast.makeText(getContext(), "Error: No document ID for item", Toast.LENGTH_SHORT).show();
            return; // Exit early if documentId is null
        }
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("lostAndFoundItems").document(documentId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // First, remove the item from the adapter's data set
                                adapter.removeItem(item);

                                // Then, if the item has an image URL, delete the image from Firebase Storage
                                if (item.getFirebaseImageUrl() != null && !item.getFirebaseImageUrl().isEmpty()) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(item.getFirebaseImageUrl())
                                            .delete()
                                            .addOnSuccessListener(unused -> Log.d(TAG, "Image successfully deleted!"))
                                            .addOnFailureListener(e -> Log.e(TAG, "Error deleting image", e));
                                }

                                Toast.makeText(getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error deleting item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }



    @Override
    public void onEditClicked(LostAndFoundItem item) {
        // Make sure item has a documentId
        if (item.getDocumentId() == null) {
            Toast.makeText(getContext(), "Error: No document ID for item", Toast.LENGTH_SHORT).show();
            return; // Exit early if documentId is null
        }

        EditPostFragment editPostFragment = new EditPostFragment();
        Bundle args = new Bundle();
        args.putString("ITEM_ID", item.getDocumentId()); // Pass the Document ID here
        editPostFragment.setArguments(args);

        editPostFragment.show(getChildFragmentManager(), editPostFragment.getTag());


        // Show the EditPostFragment as a BottomSheetDialogFragment
        //editPostFragment.show(getChildFragmentManager(), "editPost");

       /* // Assuming you are within a FragmentActivity or similar context
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_my_posts, editPostFragment); // Use your container ID
        transaction.addToBackStack(null); // Add to back stack for navigation
        transaction.commit();

        */
    }



}
