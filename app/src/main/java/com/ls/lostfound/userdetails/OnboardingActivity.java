package com.ls.lostfound.userdetails;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ls.lostfound.MainActivity;
import com.ls.lostfound.R;
import com.squareup.picasso.Picasso;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class OnboardingActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final String TAG = "OnboardingActivity";
    private static final int REQUEST_CODE_PICK_IMAGE = 1 ;
    private ViewPager viewPager;
    private ProgressBar progressBar;
    private com.google.firebase.auth.FirebaseAuth FirebaseAuth;
    private boolean imageSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.setupProgressBar);

        // Use the OnboardingPagerAdapter that handles layouts
        OnboardingPagerAdapter pagerAdapter = new OnboardingPagerAdapter(this,this);
        viewPager.setAdapter(pagerAdapter);

        // Add a page change listener to update the progress bar
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                progressBar.setProgress(position + 1);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Optional: Handle page scrolling if needed
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Optional: Handle changes in scroll state if necessary
            }
        });
    }

    // In your OnboardingActivity
    void handleOnboardingCompletion() {
        UserProfile user = UserProfileData.userProfile;
        saveUserProfileToFirebase(user);
    }
    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE);
    }



    private void completeOnboarding() {
        UserProfile userProfile = UserProfileManager.getUserProfile();

        // Upload data to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userProfileRef = db.collection("userProfiles").document(userId);

        userProfileRef.set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    // Handle successful save
                    // Navigate to the next activity or update UI accordingly
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    // Log the error or show a message to the user
                    Log.e(TAG, "Error saving user profile: ", e);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                uploadImageToFirebase(selectedImageUri);

                // Update the ImageView with the selected image
                ImageView imageViewAvatar = findViewById(R.id.imageViewAvatar);
                if (imageViewAvatar != null) {
                    Picasso.get().load(selectedImageUri).into(imageViewAvatar);
                }

                imageSelected = true;
            }
        }
    }


    // Method to be called from the adapter
    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // Check if image is selected
    public boolean isImageSelected() {
        return imageSelected;
    }
    private void uploadImageToFirebase(Uri imageUri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("profile_images/" + userId + ".jpg");

        imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                UserProfile userProfile = UserProfileManager.getUserProfile();
                userProfile.setAvatarUrl(uri.toString());
                // Update the image view with the selected image if needed
            });
        }).addOnFailureListener(e -> {
            // Handle failure
        });
    }
    public void saveUserProfileToFirebase(UserProfile userProfile) {
        // Firestore save logic
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userProfileRef = db.collection("userProfiles").document(userId);

        userProfileRef.set(userProfile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Handle successful save, navigate to MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Handle failure
                // Optionally, log the error or show a message to the user
                Log.e(TAG, "Error saving user profile: ", task.getException());
            }
        });
    }


}
