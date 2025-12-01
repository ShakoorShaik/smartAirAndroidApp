package com.example.smartair.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartair.LoginActivityView;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import utils.ChildEmergency;
import utils.DatabaseManager;

public class ChildSettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView textView = view.findViewById(R.id.WelcomeMsg);
            textView.setText(user.getEmail());
        }

        Button button_logout = view.findViewById(R.id.button_logout_child);
        Button buttonEmergency = view.findViewById(R.id.emergencyButton);
        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager.accountLogout();
                startActivity(new Intent(getActivity(), LoginActivityView.class));
                getActivity().finish();
            }
        });
        buttonEmergency.setOnClickListener(v -> {
            ChildEmergency.emergencyPrompt(getActivity());
        });
    }
}

