package com.example.smartair.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartair.R;
import utils.ChildEmergency;

public class ChildHomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonEmergency = view.findViewById(R.id.emergencyButton);
        Button logInhalerButton = view.findViewById(R.id.button5);
        logInhalerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LogUsageActivity.class));
            }
        });
        buttonEmergency.setOnClickListener(v -> {
            ChildEmergency.emergencyPrompt(getActivity());
        });

        Button zoneButton = view.findViewById(R.id.zoneButton);
        Button historyButton = view.findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChildHistoryActivity.class));
            }
        });
    }
}

