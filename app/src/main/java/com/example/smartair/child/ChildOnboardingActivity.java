package com.example.smartair.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.smartair.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import utils.ChildOnboardingManager;

public class ChildOnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ChildOnboardingPagerAdapter adapter;
    private Button buttonNext;
    private Button buttonGetStarted;
    private TextView buttonSkip;
    private TabLayout tabLayout;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        buttonNext = findViewById(R.id.buttonNext);
        buttonGetStarted = findViewById(R.id.buttonGetStarted);
        buttonSkip = findViewById(R.id.buttonSkip);
        tabLayout = findViewById(R.id.tabLayoutOnboarding);

        adapter = new ChildOnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
        }).attach();

        updateButtonVisibility();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateButtonVisibility();
            }
        });

        buttonNext.setOnClickListener(v -> {
            if (currentPage < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentPage + 1, true);
            }
        });

        buttonGetStarted.setOnClickListener(v -> {
            completeOnboarding();
        });

        buttonSkip.setOnClickListener(v -> {
            completeOnboarding();
        });
    }

    private void updateButtonVisibility() {
        int lastPage = adapter.getItemCount() - 1;
        if (currentPage == lastPage) {
            buttonNext.setVisibility(View.GONE);
            buttonGetStarted.setVisibility(View.VISIBLE);
            buttonSkip.setVisibility(View.GONE);
        } else {
            buttonNext.setVisibility(View.VISIBLE);
            buttonGetStarted.setVisibility(View.GONE);
            buttonSkip.setVisibility(View.VISIBLE);
        }
    }

    private void completeOnboarding() {
        ChildOnboardingManager.setOnboardingCompleted(this, true);
        Intent intent = new Intent(this, ChildDashboardMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

