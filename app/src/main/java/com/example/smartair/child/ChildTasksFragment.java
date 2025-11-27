package com.example.smartair.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartair.R;
import com.example.smartair.child.emergency.Emergency;
import com.example.smartair.child.inhalertechnique.InhalerTechniqueFirst;
import utils.ChildEmergency;

public class ChildTasksFragment extends Fragment {

    private ImageView Badge;
    private ImageView Streak;
    private Button buttonTechniqueHelper;
    private Button buttonRecordTrigger;
    private Button buttonTriggerHistory;
    private Button buttonRecordSymptom;
    private Button buttonEmergency;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Badge = view.findViewById(R.id.badge);
        Badge.setImageResource(R.drawable.bronze_badge);
        Badge.setVisibility(View.VISIBLE);

        Streak = view.findViewById(R.id.streak);
        Streak.setImageResource(R.drawable.gold_badge);
        Streak.setVisibility(View.VISIBLE);

        buttonTechniqueHelper = view.findViewById(R.id.techniqueHelperButton);
        buttonRecordTrigger = view.findViewById(R.id.recordTriggerButton);
        buttonTriggerHistory = view.findViewById(R.id.historyTriggerButton);
        buttonRecordSymptom = view.findViewById(R.id.symptomRecordButton);
        buttonEmergency = view.findViewById(R.id.emergencyButton);

        buttonTechniqueHelper.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), InhalerTechniqueFirst.class));
        });

        buttonRecordTrigger.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "TODO NOT FUNCTIONAL", Toast.LENGTH_LONG).show();
        });

        buttonTriggerHistory.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "TODO NOT FUNCTIONAL", Toast.LENGTH_LONG).show();
        });

        buttonRecordSymptom.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "TODO NOT FUNCTIONAL", Toast.LENGTH_LONG).show();
        });

        buttonEmergency.setOnClickListener(v -> {
            ChildEmergency.emergencyPrompt(getActivity());
        });
    }
}

