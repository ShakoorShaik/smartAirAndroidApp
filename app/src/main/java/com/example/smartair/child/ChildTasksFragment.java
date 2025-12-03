package com.example.smartair.child;

import android.app.Activity;
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
import com.example.smartair.child.inhalertechnique.InhalerTechniqueFirst;
import com.example.smartair.child.logtriggerandsymptoms.LogSymptomActivity;
import com.example.smartair.child.logtriggerandsymptoms.LogTriggerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import utils.BadgeStreakManager;

import utils.ChildEmergency;
import utils.ChildIdManager;

public class ChildTasksFragment extends Fragment {

    private ImageView BadgeInhalerTechnique;

    private ImageView BadgeControllerConsequtively;
    private ImageView BadgeRescueMonth;
    private ImageView StreakTechnique;
    private ImageView StreakController;
    private Button buttonTechniqueHelper;
    private Button buttonRecordTrigger;
    private Button buttonRecordSymptom;
    private Button buttonEmergency;
    String userID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Activity curActivity = this.getActivity();
        if (curActivity != null) {
            ChildIdManager manager = new ChildIdManager(curActivity);
            String curr_child_id = manager.getChildId();
            if (!curr_child_id.equals("NA")) {
                userID = curr_child_id;
            } else {
                userID = user.getUid();
            }
        }

        BadgeInhalerTechnique = view.findViewById(R.id.badge);
        initialiseInhalerTechniqueBadge();

        BadgeControllerConsequtively = view.findViewById(R.id.badge1);
        initialiseControllerNumberBadge();

        BadgeRescueMonth = view.findViewById(R.id.badge2);
        initialiseRescueNumberBadge();

        StreakTechnique = view.findViewById(R.id.streak);
        initialiseTechniqueStreak();

        StreakController  = view.findViewById(R.id.streak1);
        initialiseControllerStreak();

        buttonTechniqueHelper = view.findViewById(R.id.techniqueHelperButton);
        buttonRecordTrigger = view.findViewById(R.id.recordTriggerButton);
        buttonRecordSymptom = view.findViewById(R.id.symptomRecordButton);
        buttonEmergency = view.findViewById(R.id.emergencyButton);


        buttonTechniqueHelper.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), InhalerTechniqueFirst.class));
        });

        buttonRecordTrigger.setOnClickListener(v -> {
            startActivity((new Intent(getActivity(), LogTriggerActivity.class)));
        });

        buttonRecordSymptom.setOnClickListener(v -> {
            startActivity((new Intent(getActivity(), LogSymptomActivity.class)));
        });

        buttonEmergency.setOnClickListener(v -> {
            ChildEmergency.emergencyPrompt(getActivity());
        });
    }
    private void initialiseInhalerTechniqueBadge() {
        BadgeInhalerTechnique.setImageResource(R.drawable.bronze_badge);
        final int[] inhalerTechniqueNumber = new int[1];
        final int[] inhalerTechniqueThreshold = new int[1];
        BadgeStreakManager.getInhalerTechniqueNumber(userID, new BadgeStreakManager.BSMCallback() {
            @Override
            public void onSuccess(Integer count) {
                inhalerTechniqueNumber[0] = count;
                BadgeStreakManager.getParentInhalerTechniqueThreshold(userID, new BadgeStreakManager.BSMCallback() {
                    @Override
                    public void onSuccess(Integer count) {
                        inhalerTechniqueThreshold[0] = count;
                        if (inhalerTechniqueNumber[0] > inhalerTechniqueThreshold[0]){
                            BadgeInhalerTechnique.setVisibility(View.VISIBLE);
                            BadgeInhalerTechnique.setOnClickListener((v -> {
                                Toast.makeText(getActivity(), "Badge for completing " + inhalerTechniqueNumber[0] + " inhaler technique sessions!", Toast.LENGTH_LONG).show();
                            }));
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
    private void initialiseControllerNumberBadge() {
        BadgeControllerConsequtively.setImageResource(R.drawable.gold_badge);
        final int[] controllerNumber = new int[1];
        final int[] controllerNumberThreshold = new int[1];
        BadgeStreakManager.getLongestControllerStreak(userID, new BadgeStreakManager.BSMCallback() {
            @Override
            public void onSuccess(Integer count) {
                controllerNumber[0] = count;
                BadgeStreakManager.getParentControllerNumberThreshold(userID, new BadgeStreakManager.BSMCallback() {
                    @Override
                    public void onSuccess(Integer count) {
                        controllerNumberThreshold[0] = count;
                        if (controllerNumber[0] >= controllerNumberThreshold[0]){
                            BadgeControllerConsequtively.setVisibility(View.VISIBLE);
                            BadgeControllerConsequtively.setOnClickListener((v -> {
                                Toast.makeText(getActivity(), "Badge for having " + controllerNumber[0] + " perfect consecutive controller uses, longer than " + controllerNumberThreshold[0] + " days!", Toast.LENGTH_LONG).show();
                            }));
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
    private void initialiseRescueNumberBadge() {
        BadgeRescueMonth.setImageResource(R.drawable.silver_badge);
        final int[] rescueNumber = new int[1];
        final int[] rescueNumberThreshold = new int[1];
        BadgeStreakManager.getRescueNumberPastThirtyDays(userID, new BadgeStreakManager.BSMCallback() {
            @Override
            public void onSuccess(Integer count) {
                rescueNumber[0] = count;
                BadgeStreakManager.getParentRescueNumberThreshold(userID, new BadgeStreakManager.BSMCallback() {
                    @Override
                    public void onSuccess(Integer count) {
                        rescueNumberThreshold[0] = count;
                        if (rescueNumber[0] <= rescueNumberThreshold[0]){
                            BadgeRescueMonth.setVisibility(View.VISIBLE);
                            BadgeRescueMonth.setOnClickListener((v -> {
                                Toast.makeText(getActivity(), "Badge for having " + rescueNumber[0] + " rescues, less than " + rescueNumberThreshold[0] + " in a month!", Toast.LENGTH_LONG).show();
                            }));
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
    private void initialiseTechniqueStreak() {
        StreakTechnique.setImageResource(R.drawable.fire);
        StreakTechnique.setVisibility(View.VISIBLE);
        BadgeStreakManager.getCurrentInhalerTechniqueStreak(userID, new BadgeStreakManager.BSMCallback() {
            @Override
            public void onSuccess(Integer count) {
                StreakTechnique.setOnClickListener((v -> {
                    Toast.makeText(getActivity(), "Your current correct technique practice streak is " + count + "!", Toast.LENGTH_LONG).show();
                }));
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
    private void initialiseControllerStreak() {
        StreakController.setImageResource(R.drawable.fire);
        StreakController.setVisibility(View.VISIBLE);
        BadgeStreakManager.getCurrentControllerStreak(userID, new BadgeStreakManager.BSMCallback() {
            @Override
            public void onSuccess(Integer count) {
                StreakController.setOnClickListener((v -> {
                    Toast.makeText(getActivity(), "Your current controller use streak is " + count + "!", Toast.LENGTH_LONG).show();
                }));
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

}

