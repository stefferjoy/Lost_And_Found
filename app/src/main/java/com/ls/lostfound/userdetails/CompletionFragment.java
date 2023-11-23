package com.ls.lostfound.userdetails;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ls.lostfound.MainActivity;
import com.ls.lostfound.R;
public class CompletionFragment extends Fragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completion, container, false);

        Button letsGoButton = view.findViewById(R.id.buttonDismiss);
        letsGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to start the main activity
                Intent mainActivityIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(mainActivityIntent);
                // Close the entire onboarding activity stack
                getActivity().finish();
            }
        });

        return view;
    }
}
