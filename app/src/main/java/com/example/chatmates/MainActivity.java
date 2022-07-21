package com.example.chatmates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.chatmates.Authentication.LogInActivity;
import com.example.chatmates.helper.TabAccessorAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager mainViewPager;
    TabLayout tabLayout;
    TabAccessorAdapter tabAccessorAdapter;
    FirebaseAuth auth;
    DatabaseReference RootRef;

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

        auth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            auth.signOut();
            Intent intent =new Intent(MainActivity.this, LogInActivity.class);
            startActivity(intent);
        }
    }

    // Below written code is for Logout and Settings options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.logout)
        {
            auth.signOut();
            Intent intent =new Intent(MainActivity.this, LogInActivity.class);
            startActivity(intent);
        }

        if (item.getItemId()==R.id.main_settings)
        {
            Intent intent =new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        return true;
    }

}