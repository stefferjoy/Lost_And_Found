package myAccount;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ls.lostfound.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.fragment.app.Fragment;
import models.LostAndFoundItem;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;


public class EditPostFragment extends BottomSheetDialogFragment {

    private EditText editTextItemName, editTextItemDescription, editTextLocation, editTextItemDate;
    private Button buttonSave;
    private ProgressBar progressbar;
    private String itemId;
    private FirebaseFirestore db;

    public EditPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            // Set behavior such as state and peek height here
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_post, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize your views
        editTextItemName = view.findViewById(R.id.editTextItemName);
        editTextItemDescription = view.findViewById(R.id.editTextItemDescription);
        editTextLocation = view.findViewById(R.id.editTextItemLocation);
        editTextItemDate = view.findViewById(R.id.editTextItemDate);
        editTextItemDate.setOnClickListener(v -> showDatePickerDialog());
        buttonSave = view.findViewById(R.id.buttonSave);
        progressbar = view.findViewById(R.id.progressbar);



        // Retrieve the item ID from arguments
        if (getArguments() != null && getArguments().containsKey("ITEM_ID")) {
            itemId = getArguments().getString("ITEM_ID");

            // Fetch the item details from Firestore using the itemId
            db.collection("lostAndFoundItems").document(itemId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        LostAndFoundItem item = documentSnapshot.toObject(LostAndFoundItem.class);
                        if (item != null) {
                            // Populate the fields with the data
                            editTextItemName.setText(item.getItemName());
                            editTextItemDescription.setText(item.getDescription());
                            editTextLocation.setText(item.getLocation()); // Ensure this method exists in your model
                            editTextItemDate.setText(item.getDate()); // Ensure this method exists in your model
                        } else {
                            Toast.makeText(getContext(), "Item not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading item", Toast.LENGTH_SHORT).show());
        }

        buttonSave.setOnClickListener(v -> savePost());

        return view;
    }

    private void showDatePickerDialog() {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Set the date on the TextView
                    String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    editTextItemDate.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void savePost() {
        // Collect the updated data from the user
        String updatedName = editTextItemName.getText().toString();
        String updatedDescription = editTextItemDescription.getText().toString();
        String updatedLocation = editTextLocation.getText().toString();
        String updatedDate = editTextItemDate.getText().toString();

        // Validate input
        if (updatedName.isEmpty() || updatedDescription.isEmpty() || updatedLocation.isEmpty() || updatedDate.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show the loading indicator
        progressbar.setVisibility(View.VISIBLE);

        // Create a map with the updated data
        Map<String, Object> updates = new HashMap<>();
        updates.put("itemName", updatedName);
        updates.put("description", updatedDescription);
        updates.put("location", updatedLocation);
        updates.put("date", updatedDate);
        // Continue adding other fields that need to be updated

        // Update the Firestore document with the new data
        db.collection("lostAndFoundItems").document(itemId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Hide the loading indicator
                    progressbar.setVisibility(View.GONE);

                    // Show a success message
                    Toast.makeText(getContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();

                    // Close the fragment and return to the previous screen
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    // Hide the loading indicator
                    progressbar.setVisibility(View.GONE);

                    // Show an error message
                    Toast.makeText(getContext(), "Error updating item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating item: ", e);
                });
    }
}

