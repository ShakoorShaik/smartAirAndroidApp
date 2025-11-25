package com.example.smartair.parent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.smartair.R;

import utils.ParentEmergency;

public class ParentHomeFragment extends Fragment {

    public ParentHomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ParentEmergency.listenEmergency(this);

        return inflater.inflate(R.layout.fragment_parent_home, container, false);
    }
}

