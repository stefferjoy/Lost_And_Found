package com.ls.lostfound.myAccount;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ls.lostfound.models.LostAndFoundItem;
import com.ls.lostfound.userdetails.Login;
import com.ls.lostfound.R;
import com.ls.lostfound.userdetails.UserProfile;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class MyProfileFragment extends Fragment implements ProfileUpdateListener {


    private static final String TAG = "MyProfileFragment";
    FirebaseAuth auth;
    TextView textView;
    FirebaseUser user;
    private TextView nameEditText;
    private TextView phoneTextView;
    private TextView addressTextView;
    private ImageView profileImageView;
    private TextView dateOfBirth;
    private String firebaseImageUrl; // Store the Firebase Storage image URL


    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final int REQUEST_PICK_IMAGE = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload user profile data when fragment resumes
        loadUserProfile();
    }

    @Override
    public void onProfileUpdated() {
        loadUserProfile(); // Reloads the user profile
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_profile, container, false);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        textView = rootView.findViewById(R.id.user_details);
        nameEditText = rootView.findViewById(R.id.name_text_view);
        dateOfBirth = rootView.findViewById(R.id.textViewdateOfBirth);
        phoneTextView = rootView.findViewById(R.id.phone_text_view);
        addressTextView = rootView.findViewById(R.id.address_text_view);
        TextView textViewUserName = rootView.findViewById(R.id.textViewUserName);

        // Initialize the profileImageView
        profileImageView = rootView.findViewById(R.id.profile_image);

        ImageButton edit_image_icon = rootView.findViewById(R.id.edit_image_icon);
        edit_image_icon.setOnClickListener(v -> {
            // Check for permissions and then open the image selector
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImageSelector();
            } else {
                requestStoragePermission();
            }
        });


        //uploadImageToFirebase();
        //updateFirebaseUserProfileImage();
        // Load and display the user's profile picture using Firebase Cloud Storage URL
        loadUserProfilePicture();

        loadUserProfile();



        if (user == null) {
            Intent intent = new Intent(requireActivity(), Login.class);
            startActivity(intent);
            requireActivity().finish();
            return rootView; // Return early to avoid executing more code since we're redirecting
        }

        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            textView.setText(email);
            textViewUserName.setText(extractUserNameFromEmail(email));
        } else {
            // Handle the case where email is null or empty
        }

        // Initialize the "Change" button
        Button changePasswordButton = rootView.findViewById(R.id.change_password_button);

        // Set up the click listener for the "Change" button
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here, we create the bottom sheet fragment and show it
                ChangePasswordBottomSheetFragment bottomSheetFragment = new ChangePasswordBottomSheetFragment();
                bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
            }
        });


        // Initialize the ImageButton for editing
        ImageButton editDetailsButton = rootView.findViewById(R.id.edit_details_button);
        TextView nameEditText = rootView.findViewById(R.id.name_text_view);
        TextView phoneTextView = rootView.findViewById(R.id.phone_text_view);
        TextView addressTextView = rootView.findViewById(R.id.address_text_view);
        TextView dateOfBirthTextView = rootView.findViewById(R.id.textViewdateOfBirth);




        editDetailsButton.setOnClickListener(v -> {
            EditDetailsFragment editDetailsFragment = new EditDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("name", nameEditText.getText().toString());
            bundle.putString("phoneNumber", phoneTextView.getText().toString());
            bundle.putString("address", addressTextView.getText().toString());
            editDetailsFragment.setArguments(bundle);
            editDetailsFragment.show(getChildFragmentManager(), editDetailsFragment.getTag());
        });



        return rootView;
    }
    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }


    private void requestStoragePermission() {
        // Request the permission
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageSelector();
            } else {
                Toast.makeText(getContext(), "Permission required to select an image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void loadUserProfilePicture() {
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Construct the path to the user's profile picture in Cloud Storage
        String profilePicturePath = "profile_images/" + userUid + ".jpg"; // Adjust the path as needed

        // Create a reference to the Cloud Storage image
        StorageReference profilePictureRef = FirebaseStorage.getInstance().getReference().child(profilePicturePath);

        // Get the download URL of the profile picture
        profilePictureRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load and display the image using Picasso
            Picasso.get().load(uri).into(profileImageView);
        }).addOnFailureListener(e -> {
            // Handle any errors that may occur while fetching the download URL
            Log.e(TAG, "Error fetching profile picture URL: " + e.getMessage());
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("profile_images/" + userId + ".jpg");

        imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                firebaseImageUrl = uri.toString();
                updateFirebaseUserProfileImage(firebaseImageUrl);
                Picasso.get().load(uri).into(profileImageView);
            });
        }).addOnFailureListener(e -> {
            // Handle failure
            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void updateFirebaseUserProfileImage(String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userProfileRef = db.collection("userProfiles").document(userId);

        // Create a map to update only the avatarUrl field
        Map<String, Object> updates = new HashMap<>();
        updates.put("avatarUrl", imageUrl);

        userProfileRef.update(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update successful
                Log.d(TAG, "User profile image updated.");
            } else {
                // Update failed
                Log.e(TAG, "Error updating user profile image", task.getException());
            }
        });
    }




    private void loadUserProfile() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userProfileRef = db.collection("userProfiles").document(userId);

        userProfileRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    UserProfile userProfile = document.toObject(UserProfile.class);
                    // Update UI with the retrieved data
                    if (userProfile != null) {
                        nameEditText.setText(userProfile.getName());
                        phoneTextView.setText(userProfile.getPhoneNumber());
                        addressTextView.setText(userProfile.getAddress());
                        dateOfBirth.setText(userProfile.getDateOfBirth());
                        // Load profile image
                        if (userProfile.getAvatarUrl() != null) {
                            Picasso.get().load(userProfile.getAvatarUrl()).into(profileImageView);
                        }
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    public String extractUserNameFromEmail(String email) {
        int atIndex = email.indexOf('@');

        if (atIndex != -1) {
            return email.substring(0, atIndex);
        } else {
            // Handle the case where there's no "@" symbol in the email (invalid email)
            return null;
        }
    }



}

