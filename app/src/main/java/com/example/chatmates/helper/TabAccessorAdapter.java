package com.example.chatmates.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.chatmates.Fragments.ChatFragment;
import com.example.chatmates.Fragments.RequestFragment;
import com.example.chatmates.Fragments.StatusFragment;

        // See alternative for this deprecation.
public class TabAccessorAdapter extends FragmentPagerAdapter {

    // corresponding constructor
    public TabAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    //creating instances of fragments to return as a respective tab.
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                ChatFragment chatFragment= new ChatFragment();
                return chatFragment;
            case 1:
                StatusFragment statusFragment = new StatusFragment();
                return statusFragment;
            case 2:
                RequestFragment requestFragment= new RequestFragment();
                return requestFragment;
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    // for giving title to different tabs
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Status";
            case 2:
                return "Requests";
            default:
                return null;
        }
    }
}
