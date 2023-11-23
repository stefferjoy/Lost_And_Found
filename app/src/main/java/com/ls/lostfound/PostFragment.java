package com.ls.lostfound;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
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

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ls.lostfound.models.AutocompletePredictionAdapter;
import com.ls.lostfound.models.LostAndFoundItem;

public class PostFragment extends Fragment {

    private static final String TAG = "DiscoverFragment";

    private PostFragment postFragment;


    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_CAPTURE_IMAGE = 2;
    private String localImagePath; // Store the local file path of the selected image
    private String firebaseImageUrl; // Store the Firebase Storage image URL


    private RadioGroup radioGroupType;
    private EditText editTextName, editTextDescription, editTextLocation, editTextDate;
    private String selectedPlaceAddress = ""; // Initialize with empty string

    private Button buttonUploadImage, buttonPostItem;
    private ImageView imageViewUploaded;

    private boolean isLost = true;

    //    private DatabaseReference databaseReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int REQUEST_PERMISSION_CODE = 1;

    private PlacesClient placesClient;

    String apiKey = BuildConfig.PLACES_API_KEY;

    private static final int AUTOCOMPLETE_REQUEST_CODE = 3; // Choose an arbitrary request code value



    private AutocompletePredictionAdapter adapter;
    private RecyclerView recyclerViewLocationSuggestions;

    // This function is called when an item from the autocomplete predictions is clicked
    private void onPredictionClicked(AutocompletePrediction prediction) {
        editTextLocation.setText(prediction.getFullText(null).toString());
        selectedPlaceAddress = prediction.getPrimaryText(null).toString();
        // Clear the predictions and hide the RecyclerView
        adapter.clearPredictions();
        recyclerViewLocationSuggestions.setVisibility(View.GONE);

    }



    @Override
    public void onStop() {
        super.onStop();
        // Call onStop method in your Fragment
        if (postFragment != null) {
            postFragment.onStop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Call onResume method in your Fragment
        if (postFragment != null) {
            postFragment.onResume();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        // Call onPause method in your Fragment
        if (postFragment != null) {
            postFragment.onPause();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Initialize the Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(requireContext().getApplicationContext(), apiKey);
        }
        // Create a new Places client instance
        placesClient = Places.createClient(requireContext());

    }

    private void showPlacesSearchBox() {
        // Set the fields to specify which types of place data to return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

    }

    private void setupButtonPostItem() {
        // Add a click listener to the button
        buttonPostItem.setOnClickListener(v -> {

            // Disable the button to prevent multiple submissions
            buttonPostItem.setEnabled(false);

            String itemName = editTextName.getText().toString();
            String description = editTextDescription.getText().toString();
            // Make sure this is up-to-date with the selected address
            selectedPlaceAddress = editTextLocation.getText().toString();
            String date = editTextDate.getText().toString();

            // You should retrieve the user's email from your authentication system here
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
                buttonPostItem.setEnabled(true); // Re-enable the button
                return;
            }

            String userEmail = user.getEmail();

            // Create the item without an image URL
            String userId = user.getUid();
            String userName = extractUserNameFromEmail(userEmail);

            // Check if the selectedPlaceAddress is set (not empty)
            if (selectedPlaceAddress.isEmpty()) {
                // If not, show a toast, re-enable the button and return
                Toast.makeText(getContext(), "Please select an address", Toast.LENGTH_SHORT).show();
                buttonPostItem.setEnabled(true); // Re-enable the button
                return;
            }
            // Disable the button to prevent multiple submissions
            buttonPostItem.setEnabled(false);




            // Add log statements to check which fields are empty or null
            Log.d(TAG, "Item Name: " + itemName);
            Log.d(TAG, "Description: " + description);
            Log.d(TAG, "Address: " + selectedPlaceAddress);
            Log.d(TAG, "Date: " + date);
            if (user != null) {
                Log.d(TAG, "User Email: " + userEmail);
            } else {
                Log.d(TAG, "User is null");
            }
            Log.d(TAG, "Image Path: " + localImagePath);



            // Validate inputs, including the selected address
            if (itemName.isEmpty() || description.isEmpty() || selectedPlaceAddress == null || selectedPlaceAddress.isEmpty() || date.isEmpty() || userEmail == null || localImagePath == null) {
                Toast.makeText(requireContext(), "Please fill in all required fields and upload an image.", Toast.LENGTH_SHORT).show();
                buttonPostItem.setEnabled(true); // Re-enable the button
                return;
            }



            // Create the item with the selected address
            LostAndFoundItem item = new LostAndFoundItem(userId, userName, itemName, description, selectedPlaceAddress, date, localImagePath, null);

            // Generate the correct ID based on whether the item is lost or found
            item.generateIdForStatus(isLost);

            // Attempt to upload the image, which will then save the item if successful
            uploadImageToFirebaseStorage(item);
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        // Initialize views
        radioGroupType = view.findViewById(R.id.radioGroupType);
        editTextName = view.findViewById(R.id.editTextName);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextLocation = view.findViewById(R.id.editTextLocation);
        editTextDate = view.findViewById(R.id.editTextDate);
        buttonUploadImage = view.findViewById(R.id.buttonUploadImage);
        buttonPostItem = view.findViewById(R.id.buttonPostItem);
        imageViewUploaded = view.findViewById(R.id.imageViewUploaded);

        Context context = getContext();
        List<AutocompletePrediction> predictionList = new ArrayList<>();
        editTextLocation = view.findViewById(R.id.editTextLocation);
        recyclerViewLocationSuggestions = view.findViewById(R.id.recyclerViewLocationSuggestions);
        recyclerViewLocationSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set the adapter with the context, empty prediction list, editTextLocation, and recyclerViewLocationSuggestions
        adapter = new AutocompletePredictionAdapter(
                getContext(),
                new ArrayList<>(),
                editTextLocation,
                recyclerViewLocationSuggestions
        );

        // Set the PredictionClickListener in the adapter
        adapter.setPredictionClickListener(prediction -> {
            Log.d(TAG, "Address selected: " + prediction.getFullText(null).toString());
            editTextLocation.setText(prediction.getFullText(null).toString());
            selectedPlaceAddress = prediction.getFullText(null).toString(); // Update your address variable
            adapter.clearPredictions();
            recyclerViewLocationSuggestions.setVisibility(View.GONE);
        });


        // Set the adapter to the RecyclerView
        recyclerViewLocationSuggestions.setAdapter(adapter);


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
        buttonUploadImage.setOnClickListener(v -> checkStoragePermissionAndUpload());
        buttonPostItem.setOnClickListener(v -> setupButtonPostItem());

        buttonPostItem.setEnabled(false);


        // Add TextChangedListeners to the required EditText fields
        editTextName.addTextChangedListener(textWatcher);
        editTextDescription.addTextChangedListener(textWatcher);
        editTextDate.addTextChangedListener(textWatcher);
        editTextLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) { // Consider a delay here to avoid too many requests
                    fetchPlaces(s.toString());
                } else {
                    // If the text is cleared or less than 3 characters, hide the suggestions
                    recyclerViewLocationSuggestions.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
                startCameraIntent();

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
            // Handle camera image
            imageViewUploaded.setVisibility(View.VISIBLE);
            Picasso.get().load(new File(localImagePath)).into(imageViewUploaded);
        }

        Context appContext = getActivity().getApplicationContext();
        PlacesClient placesClient = Places.createClient(appContext);

        // Handle the autocomplete result
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                selectedPlaceAddress = place.getAddress();
                editTextLocation.setText(place.getAddress());

                LostAndFoundItem item = new LostAndFoundItem();
                item.setAddress(place.getAddress());
                if (place.getLatLng() != null) {
                    item.setLatitude(place.getLatLng().latitude);
                    item.setLongitude(place.getLatLng().longitude);
                }
                selectedPlaceAddress = place.getAddress();

                // TODO: Save this item to your database
                // This could be by passing the item to a method that performs the save operation,
                // or by setting these values on an existing item object that will be saved later.
                saveItemToFirestore(item);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e(TAG, "Error: Status = " + status);
            }
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

    private void fetchPlaces(String query) {

        // Make sure you include the necessary filters or restrictions for your autocomplete request
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setTypeFilter(TypeFilter.ADDRESS) // or any other filter you want
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
            // Update the adapter with the new predictions and make the RecyclerView visible
            adapter.updatePredictions(predictions);
            adapter.notifyDataSetChanged();
            recyclerViewLocationSuggestions.setVisibility(predictions.isEmpty() ? View.GONE : View.VISIBLE);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                // Handle the error
            }
        });
    }

    private void startCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = createImageFile();
            if (photoFile != null) {
                localImagePath = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.ls.lostfound.fileprovider", // Adjust with your file provider authority
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMAGE);
            }
        }
    }



}

