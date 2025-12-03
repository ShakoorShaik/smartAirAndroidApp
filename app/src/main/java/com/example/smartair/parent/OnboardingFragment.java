package com.example.smartair.parent;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartair.R;

public class OnboardingFragment extends Fragment {

    private static final String ARG_ICON = "icon";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_COLOR = "color";

    public static OnboardingFragment newInstance(int iconRes, String title, String description, String color) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ICON, iconRes);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_COLOR, color);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);

        ImageView imageIcon = view.findViewById(R.id.imageOnboardingIcon);
        TextView textTitle = view.findViewById(R.id.textOnboardingTitle);
        TextView textDescription = view.findViewById(R.id.textOnboardingDescription);
        View colorAccent = view.findViewById(R.id.viewColorAccent);
        View layoutRescueControllerDiagram = view.findViewById(R.id.layoutRescueControllerDiagram);

        Bundle args = getArguments();
        if (args != null) {
            int iconRes = args.getInt(ARG_ICON, R.drawable.ic_health_app);
            String title = args.getString(ARG_TITLE, "");
            String description = args.getString(ARG_DESCRIPTION, "");
            String colorStr = args.getString(ARG_COLOR, "#4CAF50");

            imageIcon.setImageResource(iconRes);
            
            textTitle.setText(title);
            textDescription.setText(description);
            
            // Show diagram only for Key Features page
            if ("Key Features".equals(title)) {
                layoutRescueControllerDiagram.setVisibility(View.VISIBLE);
            } else {
                layoutRescueControllerDiagram.setVisibility(View.GONE);
            }
            
            try {
                int color = Color.parseColor(colorStr);
                colorAccent.setBackgroundColor(color);
            } catch (IllegalArgumentException e) {
                colorAccent.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
        }

        return view;
    }
}

