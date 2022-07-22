package com.example.chatmates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.chatmates.Authentication.LogInActivity;
import com.example.chatmates.helper.TabAccessorAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager mainViewPager;
    TabLayout tabLayout;
    TabAccessorAdapter tabAccessorAdapter;
    FirebaseAuth auth;
    DatabaseReference RootRef;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser=auth.getCurrentUser();

        if (currentUser!=null) {
            setContentView(R.layout.activity_main);

            toolbar = findViewById(R.id.main_activity_toolbar);
            mainViewPager = findViewById(R.id.main_tab_viewPager);
            tabLayout = findViewById(R.id.main_tabs);

            tabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());
            mainViewPager.setAdapter(tabAccessorAdapter);
            setSupportActionBar(toolbar);
            tabLayout.setupWithViewPager(mainViewPager);
            getSupportActionBar().setTitle("Whatsapp");

            RootRef = FirebaseDatabase.getInstance().getReference();
        }
        else
        {
            auth.signOut();
            Intent intent =new Intent(MainActivity.this, LogInActivity.class);
            startActivity(intent);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=auth.getCurrentUser();
        if (currentUser==null)
        {
            auth.signOut();
            Intent intent =new Intent(MainActivity.this, LogInActivity.class);
            startActivity(intent);

        }
        else
        {
            progressDialog=new ProgressDialog(this);
            progressDialog.setTitle("Loading Chats");
            progressDialog.setMessage("Please wait while we are loading your chats");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            updateUserStatusStartActivity("online");
            VerifyExistenceUser();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser=auth.getCurrentUser();
        if (currentUser!=null)
        {
            updateUserStatusActivity("offline");
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        FirebaseUser currentUser=auth.getCurrentUser();
//        if (currentUser!=null)
//        {
//            updateUserStatusActivity("offline");
//        }
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FirebaseUser currentUser=auth.getCurrentUser();
        if (currentUser!=null)
        {
            updateUserStatusActivity("online");
        }
    }

    private void updateUserStatusActivity(String status) {

        String currentUserId= auth.getCurrentUser().getUid();
        String currentTime,currentDate;

        Calendar calendar= Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
        currentDate=dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a");
        currentTime=timeFormat.format(calendar.getTime());

        HashMap<String ,Object> userStateMap = new HashMap<>();
        userStateMap.put("time",currentTime);
        userStateMap.put("date",currentDate);
        userStateMap.put("state",status);

        RootRef.child("Users").child(currentUserId).child("userState").updateChildren(userStateMap);
    }

    private void updateUserStatusStartActivity(String status) {

        String currentUserId= auth.getCurrentUser().getUid();
        String currentTime,currentDate;

        Calendar calendar= Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
        currentDate=dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a");
        currentTime=timeFormat.format(calendar.getTime());

        HashMap<String ,Object> userStateMap = new HashMap<>();
        userStateMap.put("time",currentTime);
        userStateMap.put("date",currentDate);
        userStateMap.put("state",status);

        RootRef.child("Users").child(currentUserId).child("userState").updateChildren(userStateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.cancel();
            }
        });
    }

    // to check if user has once updated settings info or not
    private void VerifyExistenceUser() {
        String currentUserId= auth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!(snapshot.child("name")).exists())
                {
                    progressDialog.dismiss();
                    Intent intent =new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(intent);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
            FirebaseUser currentUser=auth.getCurrentUser();
            if (currentUser!=null)
            {
                updateUserStatusActivity("offline");
            }

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