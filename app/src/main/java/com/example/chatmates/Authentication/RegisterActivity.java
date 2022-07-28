package com.example.chatmates.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatmates.MainActivity;
import com.example.chatmates.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    EditText regEmail,regPassword;
    TextView alreadyHaveAccount;
    Button signUp;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regEmail=findViewById(R.id.signup_email);
        regPassword=findViewById(R.id.signup_password);
        alreadyHaveAccount=findViewById(R.id.already_have_acc);
        signUp =findViewById(R.id.signup_btn);
        progressDialog=new ProgressDialog(this);

        auth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount(); // method to create new account
            }
        });

        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(RegisterActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createNewAccount() {

        String userEmail=regEmail.getText().toString();
        String userPass=regPassword.getText().toString();

        if(userPass.trim().length()<6){
            regPassword.setError("Password must contain min 6 letters");
        }
        if(TextUtils.isEmpty(userEmail))
        {
            regEmail.setError("please enter email id");

        }
        if(TextUtils.isEmpty(userPass))
        {
            regPassword.setError("please enter password");

        }
        if(TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPass))
        {
            regEmail.setError("please enter email id");
            regPassword.setError("please enter password");

        }
        if(!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass))
        {
            progressDialog.setTitle("Create New Account");
            progressDialog.setMessage("please wait , while we are creating new account ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            auth.createUserWithEmailAndPassword(userEmail,userPass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
//                                final String[] deviceToken = new String[1];
//                                FirebaseMessaging.getInstance().getToken()
//                                        .addOnCompleteListener(new OnCompleteListener<String>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<String> task) {
//                                                deviceToken[0] = task.getResult();
//                                            }
//                                        });
//
//                                String currentUserId = auth.getCurrentUser().getUid();
//                                RootRef.child("Users").child(currentUserId).setValue("");

                                // (doubt)
                                //this will be used somewhere as creation of "Users" node
                                // is already done by above line of code.
//                                RootRef.child("Users").child(currentUserId).child("device_token").setValue(deviceToken[0]);

                                Intent intent =new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                                Toast.makeText(getApplicationContext(), "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "Error Occurred while creating account", Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.cancel();
                            progressDialog.dismiss();
                        }
                    });

        }
    }
}