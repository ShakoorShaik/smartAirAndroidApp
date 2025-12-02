package com.example.smartair.parent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartair.R;

public class OnboardingNameFragment extends Fragment {

    private EditText editTextName;
    private OnboardingNameListener listener;

    public interface OnboardingNameListener {
        void onNameEntered(String name);
    }

    public static OnboardingNameFragment newInstance() {
        return new OnboardingNameFragment();
    }

    public void setOnboardingNameListener(OnboardingNameListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_name, container, false);

        editTextName = view.findViewById(R.id.editTextParentName);
        TextView textTitle = view.findViewById(R.id.textOnboardingTitle);
        TextView textDescription = view.findViewById(R.id.textOnboardingDescription);

        textTitle.setText("Tell Us Your Name");
        textDescription.setText("We'd like to personalize your experience. Please enter your full name.");

        return view;
    }

    public String getName() {
        if (editTextName != null) {
            return editTextName.getText().toString().trim();
        }
        return "";
    }
}

