package com.ls.lostfound;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
class MainPagerAdapter extends FragmentStateAdapter {

    private final String[] tabTitles = {"Discover", "Post", "My Account"};

    public MainPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new DiscoverFragment();
        } else if (position == 1) {
            return new PostFragment();
        } else if (position == 2) {
            return new MyAccountFragment();
        }

        // Handle invalid positions by returning a default fragment or throwing an exception
        throw new IllegalArgumentException("Invalid position: " + position);
    }

    @Override
    public int getItemCount() {
        return tabTitles.length; // Return the number of tabs
    }

    public String getTabTitle(int position) {
        return tabTitles[position];
    }
}
