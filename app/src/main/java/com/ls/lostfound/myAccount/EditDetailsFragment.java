package com.ls.lostfound.myAccount;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.ls.lostfound.R;

import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditDetailsFragment extends BottomSheetDialogFragment {


    private static final String TAG = "EditDetailsFragment";
    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText addressEditText;
    private EditText editDateOfBirth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_details, container, false);

        nameEditText = view.findViewById(R.id.edit_text_name);
        phoneEditText = view.findViewById(R.id.edit_text_phone);
        addressEditText = view.findViewById(R.id.edit_text_address);
        editDateOfBirth = view.findViewById(R.id.edit_date_of_birth);

        editDateOfBirth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                        // When the user selects a date, update the EditText field
                        String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        editDateOfBirth.setText(selectedDate);
                    },
                    year, month, day
            );


            datePickerDialog.show();
        });


        // Retrieve data from arguments and set it to EditTexts
        if (getArguments() != null) {
            String name = getArguments().getString("name");
            String phone = getArguments().getString("phoneNumber");
            String address = getArguments().getString("address");
            String dateOfBirth = getArguments().getString("dateOfBirth");


            nameEditText.setText(name != null ? name : "");
            phoneEditText.setText(phone != null ? phone : "");
            addressEditText.setText(address != null ? address : "");
            editDateOfBirth.setText(address != null ? address : "");

        }

        Button btnSave = view.findViewById(R.id.button_make_changes);
        btnSave.setOnClickListener(v -> {
            updateUserProfileInFirebase(() -> {
                if (getActivity() instanceof ProfileUpdateListener) {
                    ((ProfileUpdateListener) getActivity()).onProfileUpdated();
                }
                dismiss();
            });
        });

        return view;
    }

    private void updateUserProfileInFirebase(Runnable onSuccessCallback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Use FirebaseFirestore instead of FirebaseDatabase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userProfileRef = db.collection("userProfiles").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", nameEditText.getText().toString());
        updates.put("phoneNumber", phoneEditText.getText().toString());
        updates.put("address", addressEditText.getText().toString());
        updates.put("dateOfBirth", editDateOfBirth.getText().toString());

        // Use the set() method for creating/updating the document with the new data
        userProfileRef.set(updates, SetOptions.merge()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }
            } else {
                        // Handle failure, e.g., show an error message
                        Log.e(TAG, "Update failed: " + task.getException().getMessage());
                    }
                });
    }

}
