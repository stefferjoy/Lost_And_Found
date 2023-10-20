
package com.ls.lostfound;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import models.LostAndFoundItem;
import myAccount.MyAccountAdapter;
import myAccount.MyAccountListItem;
import myAccount.MyPostsFragment;


public class MyAccountFragment extends Fragment {

    private static final String TAG = "MyAccountFragment";
    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;


    private List<LostAndFoundItem> userPostsList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_account, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        button = rootView.findViewById(R.id.logout);
        textView = rootView.findViewById(R.id.user_details);

        if (user == null) {
            Intent intent = new Intent(requireActivity(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        } else {
            textView.setText(user.getEmail());
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(requireActivity(), Login.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        // Initialize the RecyclerView
        RecyclerView recyclerViewMyAccount = rootView.findViewById(R.id.recyclerViewMyAccount);
        recyclerViewMyAccount.setLayoutManager(new LinearLayoutManager(requireContext()));


        // Initialize the RecyclerView
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerViewMyAccount);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Create a list of MyAccountListItem
        List<MyAccountListItem> items = new ArrayList<>();
        items.add(new MyAccountListItem(R.drawable.account_icon, "My Profile"));
        items.add(new MyAccountListItem(R.drawable.edit_post_icon, "My Posts"));
        items.add(new MyAccountListItem(R.drawable.edit_image_icon, "Change Profile Photo"));

        // Create the adapter and set it to the RecyclerView
        MyAccountAdapter adapter = new MyAccountAdapter(items);
        recyclerView.setAdapter(adapter);

        // Set the item click listener
        adapter.setOnItemClickListener(new MyAccountAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d(TAG, "Item clicked at position: " + position);
                if (position == 1) { // Check if the "My Posts" item was clicked
                    openMyPostsFragment(); // Open the MyPostsFragment
                }
            }
        });


        return rootView;
    }


    public void openMyPostsFragment() {
        MyPostsFragment myPostsFragment = new MyPostsFragment();

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.container_my_account, myPostsFragment); // Use the correct container ID
        transaction.addToBackStack(null); // Add to the back stack if needed
        transaction.commit();
    }
}





