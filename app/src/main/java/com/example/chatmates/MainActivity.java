package com.example.chatmates;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.chatmates.helper.TabAccessorAdapter;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager mainViewPager;
    TabLayout tabLayout;
    TabAccessorAdapter tabAccessorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_activity_toolbar);
        mainViewPager = findViewById(R.id.main_tab_viewPager);
        tabLayout = findViewById(R.id.main_tabs);

        // Below code is used to set the TabAccessorAdapter to the viewPager
        tabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());
        mainViewPager.setAdapter(tabAccessorAdapter);
        setSupportActionBar(toolbar);
        tabLayout.setupWithViewPager(mainViewPager);
    }
}