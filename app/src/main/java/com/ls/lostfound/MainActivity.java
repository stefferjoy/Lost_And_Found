package com.ls.lostfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ls.lostfound.models.LostAndFoundItem;
import com.ls.lostfound.notification.NotificationAdapter;
import com.ls.lostfound.notification.NotificationItem;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.Manifest;


public class MainActivity extends AppCompatActivity implements PostFragment.OnNewPostListener,ViewPagerSwipeListener {

    private static final String TAG = "MainActivity";
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList;
    private ListenerRegistration notificationListenerRegistration;
    private static final int PERMISSIONS_REQUEST_CODE = 100;






    public void onNewPost(LostAndFoundItem newItem) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "CHANNEL_ID";
        // For Android O and above, you need to add a Notification Channel
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "New Post Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.app_logo_foreground,100)
                .setContentTitle("New Post Added")
                .setContentText("Item: " + newItem.getItemName())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Notification ID cannot be 0
        notificationManager.notify(1, builder.build());
    }


    @Override
    public void setSwipeEnabled(boolean enabled) {
        viewPager.setUserInputEnabled(enabled);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabs);

        MainPagerAdapter pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);


        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerViewNotifications.setAdapter(notificationAdapter);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Set up the notification listener
        setupNotificationsListener();
        // Check and request permissions
        checkAndRequestPermissions();



        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(pagerAdapter.getTabTitle(position));
        }).attach();


        FirebaseMessaging.getInstance().subscribeToTopic("allUsers")
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed to allUsers topic";
                    if (!task.isSuccessful()) {
                        msg = "Subscribe to allUsers topic failed";
                    }
                    Log.d("FCM", msg);
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and toast (optional)
                    Log.d(TAG, "FCM Token: " + token);
                    //Toast.makeText(MainActivity.this, "FCM Token: " + token, Toast.LENGTH_SHORT).show();

                    // Call your method to save the token to Firestore
                    saveTokenToFirestore(token);
                });


        }
        private void saveTokenToFirestore(String token) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            // Check if the user is logged in
            if (currentUser != null) {
                // Get the user's ID
                String userId = currentUser.getUid();

                // Create a reference to the user's document in Firestore
                DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId);

                // Create a map to hold the token
                Map<String, Object> tokenMap = new HashMap<>();
                tokenMap.put("fcmToken", token);

                // Update the user's document with the new token
                userDocRef.update(tokenMap)
                        .addOnSuccessListener(aVoid -> Log.d("FCM", "Token successfully written to Firestore"))
                        .addOnFailureListener(e -> Log.w("FCM", "Error writing token", e));
            } else {
                // Handle the case where the user is not logged in
                Log.d("FCM", "User not logged in, unable to save FCM token");
            }
        }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


     @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            Log.d(TAG, "Menu item clicked: " + item.getItemId());

            // Handle notification icon click
            toggleNotificationPanel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void toggleNotificationPanel() {
        Log.d(TAG, "Toggling notification panel");

        /*
        if (recyclerViewNotifications.getVisibility() == View.GONE) {
            recyclerViewNotifications.setVisibility(View.VISIBLE);
            // Optional: Scroll to the top of the list when showing notifications
            recyclerViewNotifications.scrollToPosition(0);
        } else {
            recyclerViewNotifications.setVisibility(View.GONE);
        }

         */

        Button clearButton = findViewById(R.id.clearButton);

        // Toggle visibility
        if (recyclerViewNotifications.getVisibility() == View.GONE) {
            recyclerViewNotifications.setVisibility(View.VISIBLE);
            // Optional: Scroll to the top of the list when showing notifications
            recyclerViewNotifications.scrollToPosition(0);
            clearButton.setVisibility(View.VISIBLE); // Show the clear button when notifications are shown
        } else {
            recyclerViewNotifications.setVisibility(View.GONE);
            clearButton.setVisibility(View.GONE); // Hide the clear button when notifications are hidden
        }
        // Clear notifications when the clear button is clicked
        clearButton.setOnClickListener(v -> {
            // Clear your notifications list and update the RecyclerView
            notificationList.clear();
            notificationAdapter.notifyDataSetChanged();

            // Hide the RecyclerView and the clear button after clearing
            recyclerViewNotifications.setVisibility(View.GONE);
            clearButton.setVisibility(View.GONE);
        });
    }







    private void setupNotificationsListener() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        notificationListenerRegistration = db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<NotificationItem> newNotifications = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots) {
                            NotificationItem notification = doc.toObject(NotificationItem.class);
                            newNotifications.add(notification);
                        }
                        updateNotificationsRecyclerView(newNotifications);
                    }
                });
    }

    private void updateNotificationsRecyclerView(List<NotificationItem> notifications) {
        notificationList.clear(); // Clear the existing notifications
        notificationList.addAll(notifications); // Add all the new notifications
        notificationAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        // Add other permissions as needed

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    // Permission was denied. You can show a message explaining why the permission is needed.
                    return;
                }
            }
            // Permissions have been granted. You can now proceed with your app functionality.
        }
    }



}

