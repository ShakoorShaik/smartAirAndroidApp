package com.example.smartair.parent;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.smartair.LoginActivityView;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import utils.ParentEmergency;
import utils.ParentRescue;

public class ParentSettingsFragment extends Fragment {

    private TextView textParentName;
    private TextView textParentInitials;
    private ImageButton buttonEditName;
    private String currentParentName = "";

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

        textParentName = view.findViewById(R.id.textParentName);
        textParentInitials = view.findViewById(R.id.textParentInitials);
        buttonEditName = view.findViewById(R.id.buttonEditName);
        
        loadParentName();
        
        buttonEditName.setOnClickListener(v -> showEditNameDialog());

        Button buttonLogout = view.findViewById(R.id.buttonLogout);
        Button buttonThresholds = view.findViewById(R.id.buttonThresholds);
        Button buttonRescue = view.findViewById(R.id.buttonRescue);
        Button childPrivacy = view.findViewById(R.id.ChildPrivacy);

        childPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), com.example.smartair.parent.sharewithprovider.ParentConfigureProviderVisibility.class);
                startActivity(intent);
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getActivity(), LoginActivityView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

    private void loadParentName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || textParentName == null || textParentInitials == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String parentName = documentSnapshot.getString("parentName");
                        currentParentName = parentName != null ? parentName : "";
                        if (parentName != null && !parentName.isEmpty()) {
                            updateNameDisplay(parentName);
                        } else {
                            textParentName.setText("Parent");
                            textParentInitials.setText("P");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (textParentName != null) {
                        textParentName.setText("Parent");
                    }
                    if (textParentInitials != null) {
                        textParentInitials.setText("P");
                    }
                });
    }

    private void updateNameDisplay(String parentName) {
        if (textParentName == null || textParentInitials == null) return;
        
        textParentName.setText(parentName);

        String[] nameParts = parentName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        if (nameParts.length > 0) {
            initials.append(nameParts[0].charAt(0));
        }
        if (nameParts.length > 1) {
            initials.append(nameParts[nameParts.length - 1].charAt(0));
        }
        textParentInitials.setText(initials.toString().toUpperCase());

        int color = generateColorFromString(parentName);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        textParentInitials.setBackground(drawable);
    }

    private void showEditNameDialog() {
        if (getContext() == null) return;
        
        EditText editText = new EditText(getContext());
        editText.setHint("Full Name");
        editText.setText(currentParentName);
        editText.setPadding(50, 20, 50, 20);
        editText.setSingleLine(true);
        
        new AlertDialog.Builder(getContext())
                .setTitle("Edit Name")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(newName)) {
                        Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveParentName(newName);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveParentName(String newName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || getContext() == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid())
                .update("parentName", newName)
                .addOnSuccessListener(aVoid -> {
                    currentParentName = newName;
                    updateNameDisplay(newName);
                    Toast.makeText(getContext(), "Name updated successfully", Toast.LENGTH_SHORT).show();

                    if (getActivity() != null) {
                        androidx.fragment.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                        Fragment homeFragment = fm.findFragmentById(R.id.fragmentContainer);
                        if (homeFragment instanceof ParentHomeFragment) {
                            ((ParentHomeFragment) homeFragment).refreshParentName();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private int generateColorFromString(String name) {
        int hash = name.hashCode();
        int r = Math.abs(hash % 100) + 100;
        int g = Math.abs((hash / 100) % 100) + 100;
        int b = Math.abs((hash / 10000) % 100) + 100;

        r = Math.min(200, Math.max(100, r));
        g = Math.min(200, Math.max(100, g));
        b = Math.min(200, Math.max(100, b));
        
        return Color.rgb(r, g, b);
    }
}
