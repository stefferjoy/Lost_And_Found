package com.ls.lostfound;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.Manifest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import models.LostAndFoundItem;

public class PostFragment extends Fragment {

    private static final String TAG = "DiscoverFragment";


    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_CAPTURE_IMAGE = 2;
    private String localImagePath; // Store the local file path of the selected image
    private String firebaseImageUrl; // Store the Firebase Storage image URL


    private RadioGroup radioGroupType;
    private EditText editTextName, editTextDescription, editTextLocation, editTextDate;
    private Button buttonUploadImage, buttonPostItem;
    private ImageView imageViewUploaded;

    private boolean isLost = true;

//    private DatabaseReference databaseReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int REQUEST_PERMISSION_CODE = 1; //






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        //databaseReference = FirebaseDatabase.getInstance().getReference("lost_and_found"); // Replace with your Firebase database reference

        // Initialize views
        radioGroupType = view.findViewById(R.id.radioGroupType);
        editTextName = view.findViewById(R.id.editTextName);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextLocation = view.findViewById(R.id.editTextLocation);
        editTextDate = view.findViewById(R.id.editTextDate);
        buttonUploadImage = view.findViewById(R.id.buttonUploadImage);
        buttonPostItem = view.findViewById(R.id.buttonPostItem);
        imageViewUploaded = view.findViewById(R.id.imageViewUploaded);

        // Listen for radio button selection
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonLost) {
                isLost = true;
            } else if (checkedId == R.id.radioButtonFound) {
                isLost = false;
            }
        });

        editTextDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                        // When the user selects a date, update the EditText field
                        String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        editTextDate.setText(selectedDate);
                    },
                    year, month, day
            );



            datePickerDialog.show();
        });





        buttonUploadImage.setOnClickListener(v -> showImageSourceDialog());

        buttonPostItem.setEnabled(false);

// Add TextChangedListeners to the required EditText fields
        editTextName.addTextChangedListener(textWatcher);
        editTextDescription.addTextChangedListener(textWatcher);
        editTextLocation.addTextChangedListener(textWatcher);
        editTextDate.addTextChangedListener(textWatcher);




        // Add a click listener to the button
        buttonPostItem.setOnClickListener(v -> {

            // Disable the button to prevent multiple submissions
            buttonPostItem.setEnabled(false);

            String itemName = editTextName.getText().toString();
            String description = editTextDescription.getText().toString();
            String location = editTextLocation.getText().toString();
            String date = editTextDate.getText().toString();

            // You should retrieve the user's email from your authentication system here
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
                buttonPostItem.setEnabled(true); // Re-enable the button
                return;
            }
            String userEmail = user.getEmail();

            // Validate inputs
            if (itemName.isEmpty() || description.isEmpty() || location.isEmpty() || date.isEmpty() || userEmail == null || localImagePath == null) {
                Toast.makeText(requireContext(), "Please fill in all required fields and upload an image.", Toast.LENGTH_SHORT).show();
                buttonPostItem.setEnabled(true); // Re-enable the button
                return;
            }

            // Create the item without an image URL
            String userId = user.getUid();
            String userName = extractUserNameFromEmail(userEmail);
            LostAndFoundItem item = new LostAndFoundItem(userId, userName, itemName, description, location, date, localImagePath, null);

            // Generate the correct ID based on whether the item is lost or found
            item.generateIdForStatus(isLost);

            // Attempt to upload the image, which will then save the item if successful
            uploadImageToFirebaseStorage(item);
        });




        return view;
    }
    private void checkStoragePermissionAndUpload() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // You have permission, proceed with image selection/upload
            showImageSourceDialog();
        } else {
            // Permission not granted, request it
            requestStoragePermission();
        }
    }
    private void requestStoragePermission() {
        // Request permission
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with image selection/upload
                showImageSourceDialog();
            } else {
                // Permission denied, handle it (e.g., show a message to the user)
                // Optionally, you can show a message to the user explaining why the permission is needed.
                // You can also disable the "Upload" button if permission is denied.
            }
        }
    }

    private void uploadImageToFirebaseStorage(LostAndFoundItem item) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");

        // Start the upload task
        UploadTask uploadTask = imageRef.putFile(Uri.fromFile(new File(item.getLocalImagePath())));

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the download URL
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Set the image URL and save the item to Firestore
                firebaseImageUrl = uri.toString();
                item.setFirebaseImageUrl(firebaseImageUrl);
                // This will generate a lostId or foundId depending on the type of item
                item.generateIdForStatus(isLost);
                saveItemToFirestore(item);
            });
        }).addOnFailureListener(e -> {
            // If the image upload fails, do not save the item and re-enable the button
            Log.e(TAG, "Failed to upload the image: " + e.getMessage());
            Toast.makeText(requireContext(), "Failed to upload the image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            buttonPostItem.setEnabled(true);
        });
    }
    private void saveItemToFirestore(LostAndFoundItem item) {
        // Create a new document reference in your collection
        DocumentReference newDocRef = db.collection("lostAndFoundItems").document();

        // Set the document ID to the item
        item.setDocumentId(newDocRef.getId());

        // Now save the item to Firestore using the set() method
        newDocRef.set(item)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Item posted successfully!", Toast.LENGTH_SHORT).show();
                    buttonPostItem.setEnabled(true); // Re-enable the button after successful post

                    // If you need to do something with the item after it's saved, do it here
                    // For example, you could add it to an adapter or update the UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to post item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    buttonPostItem.setEnabled(true); // Re-enable the button if the post fails
                });
    }




    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Image Source");
        CharSequence[] options = {"Gallery", "Camera"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Pick image from gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_PICK_IMAGE);
            } else if (which == 1) {
                // Capture image using the camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File imageFile = createImageFile();
                if (imageFile != null) {
                    localImagePath = imageFile.getAbsolutePath();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                    startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
                }
            }
        });
        builder.show();
    }
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE) {
            Uri selectedImageUri = data.getData();
            localImagePath = getRealPathFromURI(selectedImageUri);
            imageViewUploaded.setVisibility(View.VISIBLE);
            Picasso.get().load(new File(localImagePath)).into(imageViewUploaded);
        } else if (requestCode == REQUEST_CAPTURE_IMAGE) {
            imageViewUploaded.setVisibility(View.VISIBLE);
            Picasso.get().load(new File(localImagePath)).into(imageViewUploaded);
        }

    }



    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String realImagePath = cursor.getString(column_index);
            cursor.close();
            return realImagePath;
        }
        return contentUri.getPath(); // Fallback
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
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Check if all required fields are filled
            boolean allFieldsFilled = !editTextName.getText().toString().isEmpty()
                    && !editTextDescription.getText().toString().isEmpty()
                    && !editTextLocation.getText().toString().isEmpty()
                    && !editTextDate.getText().toString().isEmpty();

            // Enable the "Post" button if all required fields are filled, otherwise disable it
            buttonPostItem.setEnabled(allFieldsFilled);
        }
    };






}

