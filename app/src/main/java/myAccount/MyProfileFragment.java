package myAccount;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ls.lostfound.Login;
import com.ls.lostfound.R;

import org.jetbrains.annotations.Nullable;

import static androidx.fragment.app.DialogFragment.STYLE_NORMAL;

public class MyProfileFragment extends Fragment {


    FirebaseAuth auth;
    TextView textView;
    FirebaseUser user;

    private TextView textViewUserName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_profile, container, false);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        textView = rootView.findViewById(R.id.user_details);
        textViewUserName = rootView.findViewById(R.id.textViewUserName);


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


        editDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new instance of the editing fragment
                EditDetailsFragment editDetailsFragment = new EditDetailsFragment();
                editDetailsFragment.show(getChildFragmentManager(), editDetailsFragment.getTag());



                // Optionally, pass data to the editing fragment
                Bundle bundle = new Bundle();
                bundle.putString("name", nameEditText.getText().toString());
                bundle.putString("phone", phoneTextView.getText().toString());
                bundle.putString("address", addressTextView.getText().toString());
                editDetailsFragment.setArguments(bundle);


            }
        });

        return rootView;
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
