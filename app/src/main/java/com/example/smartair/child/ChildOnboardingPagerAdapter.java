package com.example.smartair.child;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.smartair.R;

import java.util.HashMap;
import java.util.Map;

public class ChildOnboardingPagerAdapter extends FragmentStateAdapter {

    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public ChildOnboardingPagerAdapter(FragmentActivity fragmentActivity) {
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
                fragment = ChildOnboardingFragment.newInstance(
                        R.drawable.ic_child_welcome,
                        "Welcome to SmartAir! 🎉",
                        "Your fun asthma helper! Track your breathing, earn cool badges, and stay healthy!",
                        "#FF6B9D"
                );
                break;
            case 1:
                fragment = ChildOnboardingFragment.newInstance(
                        R.drawable.ic_child_zones,
                        "Color Zones 🎨",
                        "Green = Great! 🟢\nYellow = Be Careful! 🟡\nRed = Get Help! 🔴\n\nYour zone shows how your breathing is today!",
                        "#4ECDC4"
                );
                break;
            case 2:
                fragment = ChildOnboardingFragment.newInstance(
                        R.drawable.ic_child_features,
                        "Cool Features! ⭐",
                        "• Daily Check-In 📝\n• Log Inhaler Use 💨\n• Earn Badges & Streaks 🏆\n• Learn Inhaler Technique 🎓\n• Record Triggers & Symptoms 📊\n• Emergency Button 🆘",
                        "#FFD93D"
                );
                break;
            case 3:
                fragment = ChildOnboardingFragment.newInstance(
                        R.drawable.ic_child_badges,
                        "Badges & Rewards! 🏆",
                        "Complete tasks to earn awesome badges! Keep streaks going to unlock special rewards! Your parent can see your progress too!",
                        "#95E1D3"
                );
                break;
            case 4:
                fragment = ChildOnboardingFragment.newInstance(
                        R.drawable.ic_child_safe,
                        "Stay Safe! 🛡️",
                        "Your parent can see your health info to help keep you safe. Always use the emergency button if you need help right away!",
                        "#F38181"
                );
                break;
            default:
                fragment = ChildOnboardingFragment.newInstance(
                        R.drawable.ic_child_welcome,
                        "Welcome!",
                        "Let's get started!",
                        "#FF6B9D"
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

