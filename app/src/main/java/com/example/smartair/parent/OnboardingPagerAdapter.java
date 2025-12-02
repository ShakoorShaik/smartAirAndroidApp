package com.example.smartair.parent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.smartair.R;

import java.util.HashMap;
import java.util.Map;

public class OnboardingPagerAdapter extends FragmentStateAdapter {

    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public OnboardingPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = OnboardingFragment.newInstance(
                        R.drawable.ic_welcome,
                        "Welcome to SmartAir",
                        "Your comprehensive asthma management companion. Monitor your child's respiratory health, track medications, and stay informed about their condition.",
                        "#4CAF50"
                );
                break;
            case 1:
                fragment = OnboardingFragment.newInstance(
                        R.drawable.ic_privacy,
                        "Privacy & Security",
                        "Your family's health data is encrypted and secure. We follow HIPAA-compliant practices. You control what information is shared with healthcare providers.",
                        "#2196F3"
                );
                break;
            case 2:
                fragment = OnboardingFragment.newInstance(
                        R.drawable.ic_features,
                        "Key Features",
                        "• Real-time zone monitoring (Green/Yellow/Red)\n• Medication tracking & reminders\n• Calendar view for health events\n• Emergency alerts & rescue tracking\n• Share data with healthcare providers",
                        "#FF9800"
                );
                break;
            case 3:
                fragment = OnboardingFragment.newInstance(
                        R.drawable.ic_sharing,
                        "Sharing Options",
                        "Connect with healthcare providers to share your child's health data. You decide what to share and when. All sharing requires your explicit consent.",
                        "#9C27B0"
                );
                break;
            case 4:
                fragment = OnboardingNameFragment.newInstance();
                break;
            default:
                fragment = OnboardingFragment.newInstance(
                        R.drawable.ic_welcome,
                        "Welcome",
                        "Get started with SmartAir",
                        "#4CAF50"
                );
                break;
        }
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}

