package com.example.smartair.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.smartair.R;
import com.example.smartair.child.ChildDashboardHome;
import com.example.smartair.child.ChildHistoryActivity;

public class ParentHomeFragment extends Fragment {

    public ParentHomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_home, container, false);

        Button historyButton = view.findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ParentHistoryActivity.class));
                getActivity().finish();
            }
        });

        return view;

    }
}

