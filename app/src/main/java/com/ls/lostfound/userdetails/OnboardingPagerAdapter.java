
package com.ls.lostfound.userdetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ls.lostfound.MainActivity;
import com.ls.lostfound.R;

public class OnboardingPagerAdapter extends PagerAdapter {
    private static final String TAG = "OnboardingPagerAdapter";
    private Activity activity;

    // Variable to track if an image has been selected
    boolean imageSelected = false;
    private final Context context;

    public OnboardingPagerAdapter(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }




    private static final int[] LAYOUTS = {
            R.layout.fragment_name, // Replace with actual layout resource
            R.layout.fragment_phone_number, // Replace with actual layout resource
            R.layout.fragment_address, // Replace with actual layout resource
            R.layout.fragment_date_of_birth, // Replace with actual layout resource
            R.layout.fragment_avatar, // Replace with actual layout resource
            R.layout.fragment_completion // Replace with actual layout resource

    };
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE = 2;

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View view = inflater.inflate(LAYOUTS[position], container, false);

            switch (position) {
                case 0: // Name step
                    handleNameStep(view);
                    break;
                case 1: // Phone number step
                    handlePhoneNumberStep(view);
                    break;
                case 2: // Address step
                    handleAddressStep(view);
                    break;
                case 3: // DOB step
                    handleDOBStep(view);
                    break;
                case 4: // Avatar step
                    handleAvatarStep(view, container.getContext());
                    break;
                case 5: // Completion step
                    handleCompletionStep(view, container);
                    break;
            }


        // Setup the proceed button for the current page
            setupButtonForCurrentPage(view, position, container);
            // Handle other positions similarly
            container.addView(view);

            return view;
            }

            private void handleNameStep (View view){
                EditText nameEditText = view.findViewById(R.id.editTextFullName);
                Button buttonNameProceed = view.findViewById(R.id.buttonNameProceed);
                buttonNameProceed.setOnClickListener(v -> {
                    String name = nameEditText.getText().toString();
                    if (!name.isEmpty()) {
                        UserProfileData.userProfile.setName(name);
                        // Proceed to next step
                    } else {
                    }
                });
            }
            private void handlePhoneNumberStep (View view){
                EditText phoneEditText = view.findViewById(R.id.editTextPhoneNumber);
                Button buttonPhoneProceed = view.findViewById(R.id.buttonPhoneProceed);
                    buttonPhoneProceed.setOnClickListener(v -> {
                        String phoneNumber = phoneEditText.getText().toString();
                        if(!phoneNumber.isEmpty()){
                            UserProfileData.userProfile.setPhoneNumber(phoneNumber);
                        }else{

                        }


                    });

            }
            private void handleAddressStep (View view){
                EditText editTextAddress = view.findViewById(R.id.editTextAddress);
                Button buttonAddressProceed = view.findViewById(R.id.buttonAddressProceed);

                buttonAddressProceed.setOnClickListener(v -> {
                    String address = editTextAddress.getText().toString();
                    if (!address.isEmpty()) {
                        UserProfileData.userProfile.setAddress(address);
                    }else {

                    }

                });
            }
            private void handleDOBStep (View view){
                EditText editTextDateOfBirth = view.findViewById(R.id.editTextDateOfBirth);
                Button buttonDobProceed = view.findViewById(R.id.buttonDobProceed);

                buttonDobProceed.setOnClickListener(v -> {
                    String dateOfBirth = editTextDateOfBirth.getText().toString();
                    if (!dateOfBirth.isEmpty()) {
                        UserProfileData.userProfile.setDateOfBirth(dateOfBirth);
                    }
                });
            }
    private void handleAvatarStep(View view, final Context context) {
        ImageView imageViewAvatar = view.findViewById(R.id.imageViewAvatar);
        Button buttonAvatarProceed = view.findViewById(R.id.buttonAvatarProceed);

        imageViewAvatar.setOnClickListener(v -> {
            if (context instanceof OnboardingActivity) {
                ((OnboardingActivity) context).selectImage();
            }
        });

        buttonAvatarProceed.setOnClickListener(v -> {
            if (!imageSelected) {
                // If no image is selected, prompt to select image
                imageViewAvatar.performClick();
            } else {
                // If an image is already selected, proceed to the next step
                ViewPager viewPager = ((Activity) context).findViewById(R.id.viewPager);
                if (viewPager != null && viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
                    // Move to the next page of the ViewPager
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                } else {
                    // If this is the last page, handle completion of onboarding
                    if (context instanceof OnboardingActivity) {
                        ((OnboardingActivity) context).handleOnboardingCompletion();
                    }
                }
            }
        });
    }




    private void handleCompletionStep(View view, ViewGroup container) {
        Button buttonDismiss = view.findViewById(R.id.buttonDismiss);
        buttonDismiss.setOnClickListener(v -> {
            if (isUserProfileComplete()) {
                saveUserProfileToFirebase(UserProfileData.userProfile);
                // Navigate to MainActivity or show success message
            } else {
                // Show error message indicating incomplete profile
            }
        });
    }

    private boolean isUserProfileComplete() {
        // Check if all required fields in UserProfile are set
        UserProfile userProfile = UserProfileData.userProfile;
        return userProfile.getName() != null && !userProfile.getName().isEmpty()
                && userProfile.getPhoneNumber() != null && !userProfile.getPhoneNumber().isEmpty()
                // Add checks for other fields like address, dateOfBirth, etc.
                && imageSelected; // Check if an image has been selected
    }




    private void setupButtonForCurrentPage (View view,int position, ViewGroup container)
                {
                    Button proceedButton = null;
                    switch (position) {
                        case 0:
                            proceedButton = view.findViewById(R.id.buttonNameProceed);
                            break;
                        case 1:
                            proceedButton = view.findViewById(R.id.buttonPhoneProceed);
                            break;
                        case 2:
                            proceedButton = view.findViewById(R.id.buttonAddressProceed);
                            break;
                        case 3:
                            proceedButton = view.findViewById(R.id.buttonDobProceed);
                            break;
                        case 4:
                            proceedButton = view.findViewById(R.id.buttonAvatarProceed);
                            break;
                        case 5:
                            proceedButton = view.findViewById(R.id.buttonDismiss);
                            break;
                    }

                    if (proceedButton != null) {
                        proceedButton.setOnClickListener(v -> {
                            if (position < getCount() - 1) {
                                ((ViewPager) container).setCurrentItem(position + 1);
                            } else {
                                handleOnboardingCompletion(container);
                            }
                        });

                    }
                }

                @Override
                public void destroyItem (ViewGroup container,int position, Object object){
                    container.removeView((View) object);
                }

                @Override
                public int getCount () {
                    return LAYOUTS.length;
                }

                @Override
                public boolean isViewFromObject (View view, Object object){
                    return view == object;
                }

                private void handleOnboardingCompletion (ViewGroup container){
                    // Navigate to MainActivity after completing onboarding
                    Intent mainActivityIntent = new Intent(container.getContext(), MainActivity.class);
                    container.getContext().startActivity(mainActivityIntent);
                    ((Activity) container.getContext()).finish();
                }

    public void saveUserProfileToFirebase(UserProfile userProfile) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userProfileRef = db.collection("userProfiles").document(userId);

        userProfileRef.set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile saved successfully");
                    // Navigate to MainActivity
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    // Optionally, if you want to finish the current activity as well:
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user profile: ", e);
                    Toast.makeText(context, "Failed to save user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }







}
