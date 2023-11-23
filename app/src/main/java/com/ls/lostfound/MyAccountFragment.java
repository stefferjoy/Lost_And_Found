
package com.ls.lostfound;
import android.app.DatePickerDialog;
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
import java.util.Calendar;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ls.lostfound.models.LostAndFoundItem;
import com.ls.lostfound.myAccount.MyAccountAdapter;
import com.ls.lostfound.myAccount.MyAccountListItem;
import com.ls.lostfound.myAccount.MyPostsFragment;
import com.ls.lostfound.myAccount.MyProfileFragment;
import com.ls.lostfound.userdetails.Login;


public class MyAccountFragment extends Fragment {

    private static final String TAG = "MyAccountFragment";
    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;

    public MyAccountFragment() {
        // Required empty public constructor
    }



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
        items.add(new MyAccountListItem(R.drawable.edit_image_icon, "Contact Us"));

        // Create the adapter and set it to the RecyclerView
        MyAccountAdapter adapter = new MyAccountAdapter(items);
        recyclerView.setAdapter(adapter);

        // Set the item click listener
        adapter.setOnItemClickListener(new MyAccountAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d(TAG, "Item clicked at position: " + position);
                if (position == 0) {
                    openMyProfileFragment();
                } else if (position == 1) {
                    openMyPostsFragment();
                }
                //
            }
        });


        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        // Perform actions when the fragment becomes visible
    }

    @Override
    public void onPause() {
        super.onPause();
        // Add code here to handle actions when MyAccountFragment is paused
    }
    @Override
    public void onStop() {
        super.onStop();
        // Add code here to handle actions when MyAccountFragment is paused
    }


    public void openMyProfileFragment() {
        // Pause MyAccountFragment
        onPause();

        // Clear any existing fragments that might be in the container to prevent overlapping
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        // Check if an instance of MyProfileFragment already exists
        MyProfileFragment existingFragment = (MyProfileFragment) getParentFragmentManager().findFragmentByTag("MyProfileFragment");

        if (existingFragment != null) {
            // If it exists, show the existing fragment
            transaction.show(existingFragment);
        } else {
            // If it doesn't exist, create a new instance and add it
            MyProfileFragment myProfileFragment = new MyProfileFragment();
            transaction.replace(R.id.container_my_account, myProfileFragment, "MyProfileFragment");
        }

        transaction.addToBackStack(null); // Add transaction to the back stack if needed
        transaction.commit(); // Commit the transaction
    }

    public void openMyPostsFragment() {
        // Pause MyAccountFragment
        onPause();

        // Clear the back stack to prevent fragment overlap
        clearBackStack();

        // Begin the transaction
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        // Create new instance of MyPostsFragment
        MyPostsFragment myPostsFragment = new MyPostsFragment();

        // Replace the container with MyPostsFragment
        transaction.replace(R.id.container_my_account, myPostsFragment);

        // Optional: Add the transaction to the back stack
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commitAllowingStateLoss();

        // Log current fragments for debugging
        logCurrentFragments();

        // Ensure the UI is updated
        getParentFragmentManager().executePendingTransactions();
    }


    private void clearBackStack() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }



    private void logCurrentFragments() {
        for (Fragment frag : getParentFragmentManager().getFragments()) {
            Log.d(TAG, "Current Fragment: " + frag.toString());
        }
    }


}










