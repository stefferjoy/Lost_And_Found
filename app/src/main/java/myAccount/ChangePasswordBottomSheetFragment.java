package myAccount;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ls.lostfound.R;

import org.jetbrains.annotations.Nullable;

public class ChangePasswordBottomSheetFragment extends BottomSheetDialogFragment {

    private EditText editTextOldPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmNewPassword;
    private Button buttonResetPassword;
    private FirebaseUser user;



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_change_password_bottom_sheet, container, false);

        // Initialize Firebase Auth and current user
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize your EditTexts and Button
        editTextOldPassword = v.findViewById(R.id.old_password); // Make sure you have this field in your layout
        editTextNewPassword = v.findViewById(R.id.edit_text_new_password);
        editTextConfirmNewPassword = v.findViewById(R.id.edit_text_confirm_new_password);
        buttonResetPassword = v.findViewById(R.id.button_reset_password);

        // Set up the button click listener to reset the password
        buttonResetPassword.setOnClickListener(view -> {
            String oldPassword = editTextOldPassword.getText().toString();
            String newPassword = editTextNewPassword.getText().toString();
            String confirmNewPassword = editTextConfirmNewPassword.getText().toString();

            if (!newPassword.equals(confirmNewPassword)) {
                // Alert the user that the new passwords do not match
                showToast("New passwords do not match.");
                return;
            }

            // Re-authenticate the user with their old password and proceed with the password update
            reauthenticateAndChangePassword(user, oldPassword, newPassword);
        });

        return v;
    }

    private void reauthenticateAndChangePassword(FirebaseUser user, String oldPassword, String newPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // The user is re-authenticated and can now change the password.
                changeFirebaseUserPassword(newPassword);
            } else {
                // Old password is not correct
                showToast("Old password is incorrect.");
            }
        });
    }

    private void changeFirebaseUserPassword(String newPassword) {
        user.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Password has been updated.");
                // Dismiss the bottom sheet or navigate away
                dismiss();
            } else {
                // Failed to update password
                showToast("Failed to update password.");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
