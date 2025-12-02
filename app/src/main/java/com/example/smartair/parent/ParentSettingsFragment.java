package com.example.smartair.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.smartair.LoginActivityView;
import com.example.smartair.R;
import com.example.smartair.parent.sharewithprovider.ParentConfigureProviderVisibility;
import com.example.smartair.parent.sharewithprovider.ParentProviderViewables;
import com.example.smartair.parent.sharewithprovider.ParentViewableDataWriting;
import com.google.firebase.auth.FirebaseAuth;

import utils.DatabaseManager;
import utils.ParentEmergency;
import utils.ParentRescue;

public class ParentSettingsFragment extends Fragment {


    public ParentSettingsFragment() {
    }

    @Override
    public void onStart(){
        super.onStart();
        ParentEmergency.listenEmergency(this);
        ParentRescue.listenRescue(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_settings, container, false);

        Button buttonLogout = view.findViewById(R.id.buttonLogout);
        Button buttonThresholds = view.findViewById(R.id.buttonThresholds);
        Button buttonRescue = view.findViewById(R.id.buttonRescue);
        Button childPrivacy = view.findViewById(R.id.ChildPrivacy);

        childPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ParentConfigureProviderVisibility.class);
                startActivity(intent);
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivityView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });
        buttonThresholds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SetChildThresholds.class);
                startActivity(intent);
            }
        });
        buttonRescue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), setRescueThreshold.class);
                startActivity(intent);
            }
        });

        Button buttonExportHistory = view.findViewById(R.id.buttonExportHistory);
        buttonExportHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ParentExportHistoryActivity.class);
                startActivity(intent);
            }
        });

        Button buttonAdherenceSchedule = view.findViewById(R.id.buttonAdherenceSchedule);
        buttonAdherenceSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ParentAdherenceScheduleActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}

