package com.ls.lostfound;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import androidx.fragment.app.Fragment;

public class MyAccountFragment extends Fragment {

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_account, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        button = rootView.findViewById(R.id.logout);
        textView = rootView.findViewById(R.id.user_details);

        if (user == null) {
            Intent intent = new Intent(requireActivity(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        } else {
            textView.setText(user.getEmail());
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(requireActivity(), Login.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        ImageView accountImageView = rootView.findViewById(R.id.accountImageView);

        // Known Firebase Storage image URL
        String firebaseImageUrl = "https://firebasestorage.googleapis.com/v0/b/lostandfound-51162.appspot.com/o/images%2F1697646777974.jpg?alt=media&token=33cf835d-f1f5-4026-ab47-7c2df4e1519c";

        // Load the Firebase image into the ImageView
        Picasso.get().load(firebaseImageUrl).into(accountImageView);

        return rootView;
    }
}

