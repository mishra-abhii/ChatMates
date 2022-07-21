package com.example.chatmates.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.chatmates.R;

public class LogInActivity extends AppCompatActivity {

    TextView needNewAcc;
    Button phoneLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        needNewAcc = findViewById(R.id.needanewAccount);
        phoneLogin = findViewById(R.id.phone_number_login);

        needNewAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivity.this , RegisterActivity.class);
                startActivity(intent);
            }
        });

        phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivity.this , PhoneLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}