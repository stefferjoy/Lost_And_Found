package myAccount;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ls.lostfound.R;

import org.jetbrains.annotations.Nullable;

public class EditDetailsFragment extends BottomSheetDialogFragment{


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_details, container, false);

        // Retrieve data from arguments
        if (getArguments() != null) {
            String name = getArguments().getString("name");
            String phone = getArguments().getString("phone");
            String address = getArguments().getString("address");

            // Set the retrieved data to EditTexts
            EditText nameEditText = view.findViewById(R.id.edit_text_name);
            nameEditText.setText(name);

            EditText phoneEditText = view.findViewById(R.id.edit_text_phone);
            phoneEditText.setText(phone);

            EditText addressEditText = view.findViewById(R.id.edit_text_address);
            addressEditText.setText(address);
        }


        return view;
    }
}
