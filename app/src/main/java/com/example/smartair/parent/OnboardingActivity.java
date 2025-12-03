package com.example.smartair.parent;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.smartair.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import utils.OnboardingManager;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingPagerAdapter adapter;
    private Button buttonNext;
    private Button buttonGetStarted;
    private TextView buttonSkip;
    private TabLayout tabLayout;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        buttonNext = findViewById(R.id.buttonNext);
        buttonGetStarted = findViewById(R.id.buttonGetStarted);
        buttonSkip = findViewById(R.id.buttonSkip);
        tabLayout = findViewById(R.id.tabLayoutOnboarding);

        adapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Empty as we just want the indicators
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
            if (currentPage == adapter.getItemCount() - 1) {
                saveNameAndComplete();
            } else {
                completeOnboarding();
            }
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

    private void saveNameAndComplete() {
        if (adapter != null && currentPage == 4) {
            Fragment fragment = adapter.getFragment(currentPage);
            if (fragment instanceof OnboardingNameFragment) {
                OnboardingNameFragment nameFragment = (OnboardingNameFragment) fragment;
                String name = nameFragment.getName();
                
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Please enter your name to continue", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("parentName", name);
                    
                    db.collection("users").document(user.getUid())
                            .update(userData)
                            .addOnSuccessListener(aVoid -> {
                                completeOnboarding();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error saving name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                completeOnboarding();
                            });
                } else {
                    completeOnboarding();
                }
            } else {
                completeOnboarding();
            }
        } else {
            completeOnboarding();
        }
    }

    private void completeOnboarding() {
        OnboardingManager.setOnboardingCompleted(this, true);
        Intent intent = new Intent(this, ParentDashboardWithChildrenActivity.class);
        startActivity(intent);
        finish();
    }
}

