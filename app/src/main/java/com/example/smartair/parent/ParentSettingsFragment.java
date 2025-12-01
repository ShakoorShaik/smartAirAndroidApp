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

import com.example.smartair.Login;
import com.example.smartair.R;
import com.example.smartair.parent.sharewithprovider.ParentProviderViewables;
import com.example.smartair.parent.sharewithprovider.ParentViewableDataWriting;
import com.google.firebase.auth.FirebaseAuth;

import utils.DatabaseManager;
import utils.ParentEmergency;
import utils.ParentRescue;

public class ParentSettingsFragment extends Fragment {

    private Switch rescueLogs;
    private Switch controllerSummary;
    private Switch symptomLog;
    private Switch triggerLog;
    private Switch peakFlow;
    private Switch triageIncident;
    private Switch summaryChart;

    private Button saveSettings;

    private ParentProviderViewables settingData = new ParentProviderViewables();

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
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), Login.class);
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
        //-------------------------------------------------
        // Share provider switches
        //-------------------------------------------------
        rescueLogs = view.findViewById(R.id.RescueLog);
        rescueLogs.setOnCheckedChangeListener((rescueLogs, isChecked) -> {
            if (isChecked) {
                settingData.SetRescues(true);
            } else {
                settingData.SetRescues(false);
            }
        });


        controllerSummary = view.findViewById(R.id.ControllerAdherenceSummary);
        controllerSummary.setOnCheckedChangeListener((controllerSummary,isChecked) ->{
            if (isChecked) {
                settingData.SetSummary(true);
            } else {
                settingData.SetSummary(false);
            }
        });


        symptomLog = view.findViewById(R.id.SymptompLog);
        symptomLog.setOnCheckedChangeListener((symptomLog, isChecked) ->{
            if (isChecked) {
                settingData.SetSymptom(true);
            } else {
                settingData.SetSymptom(false);
            }
        });


        triggerLog = view.findViewById(R.id.TriggerLog);
        triggerLog.setOnCheckedChangeListener((triggerLog,isChecked)-> {
            if (isChecked) {
                settingData.SetTrigger(true);
            } else {
                settingData.SetTrigger(false);
            }
        });


        peakFlow = view.findViewById(R.id.PeakFlow);
        peakFlow.setOnCheckedChangeListener((peakFlow,isChecked) -> {
            if (isChecked) {
                settingData.SetPeakFlow(true);
            } else {
                settingData.SetPeakFlow(false);
            }
        });

        triageIncident = view.findViewById(R.id.TriageIncident);
        triageIncident.setOnCheckedChangeListener((triageIncident,isChecked)-> {
            if (isChecked) {
                settingData.SetTriage(true);
            } else {
                settingData.SetTriage(false);
            }
        });


        summaryChart = view.findViewById(R.id.SummaryChart);
        summaryChart.setOnCheckedChangeListener((summaryChart,isChecked) -> {
            if (isChecked) {
                settingData.SetSummary(true);
            } else {
                settingData.SetSummary(false);
            }
        });

        //-------------------------------------------------
        // Save Button
        //-------------------------------------------------

        saveSettings = view.findViewById(R.id.button8);
        saveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParentViewableDataWriting.SaveSetting(settingData, new DatabaseManager.SuccessFailCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Settings Saved Successfully", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Settings Failed to Save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }
}

