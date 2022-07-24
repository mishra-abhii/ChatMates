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
import android.widget.Toast;

import com.example.chatmates.MainActivity;
import com.example.chatmates.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    EditText etPhoneNumber, etEnterOTP;
    Button sendVerificationCode, btnVerify;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callback;
    String verificationId;
    PhoneAuthProvider.ForceResendingToken token;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        etPhoneNumber = findViewById(R.id.phone_number_edit);
        etEnterOTP = findViewById(R.id.phone_number_verify_code);
        sendVerificationCode = findViewById(R.id.send_verify_code_btn);
        btnVerify = findViewById(R.id.verify_btn);

        etPhoneNumber.setVisibility(View.VISIBLE);
        sendVerificationCode.setVisibility(View.VISIBLE);
        etEnterOTP.setVisibility(View.INVISIBLE);
        btnVerify.setVisibility(View.INVISIBLE);

        auth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);

        sendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String PhoneNumber = etPhoneNumber.getText().toString();
                if(TextUtils.isEmpty(PhoneNumber))
                {
                    etPhoneNumber.setError("please enter phone number");

                }
                else
                {
                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("please wait , while we are authenticating your phone ...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthOptions options= PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber("+91"+ PhoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(PhoneLoginActivity.this)
                            .setCallbacks(callback)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                    auth.setLanguageCode("en");
                }
            }
        });

        callback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                progressDialog.cancel();
                etEnterOTP.setText(phoneAuthCredential.getSmsCode());
                signInWithPhoneCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.cancel();
                Toast.makeText(getApplicationContext(), "Invalid Phone Number , please enter correct phone number", Toast.LENGTH_SHORT).show();
                btnVerify.setVisibility(View.INVISIBLE);
                etEnterOTP.setVisibility(View.INVISIBLE);
                sendVerificationCode.setVisibility(View.VISIBLE);
                etPhoneNumber.setVisibility(View.VISIBLE);

                // Through below code we handle exceptions
                if(e instanceof FirebaseAuthInvalidCredentialsException)
                { Toast.makeText(getApplicationContext(), "Invalid Request : " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();


                }
                if(e instanceof FirebaseTooManyRequestsException)
                {
                    Toast.makeText(getApplicationContext(), " Your sms limit has been expired ", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken Token) {
                progressDialog.cancel();
                verificationId=s;
                token=Token;
                Toast.makeText(getApplicationContext(), "OTP Sent", Toast.LENGTH_SHORT).show();
                btnVerify.setVisibility(View.VISIBLE);
                etEnterOTP.setVisibility(View.VISIBLE);
                sendVerificationCode.setVisibility(View.INVISIBLE);
                etPhoneNumber.setVisibility(View.INVISIBLE);
            }

        };

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etPhoneNumber.setVisibility(View.INVISIBLE);
                sendVerificationCode.setVisibility(View.INVISIBLE);
                String code=etEnterOTP.getText().toString();
                if(TextUtils.isEmpty(code))
                {
                    etEnterOTP.setError("please enter verification code");
                }
                else
                {
                    progressDialog.setTitle("Verification Code");
                    progressDialog.setMessage("Please wait , while we are verifying you code ....");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId,code);
                    signInWithPhoneCredentials(credential);
                }
            }
        });
    }

    public void signInWithPhoneCredentials(PhoneAuthCredential phoneAuthCredential) {
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
//                    String deviceToken= FirebaseInstanceId.getInstance().getToken();
//                    String currentUserId=auth.getCurrentUser().getUid();
//                    DatabaseReference UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
//                    UserRef.child(currentUserId).child("device_token").setValue(deviceToken);
                    Toast.makeText(getApplicationContext(), "You are Successfully Logged In", Toast.LENGTH_SHORT).show();

                    Intent intent =new Intent(PhoneLoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    String message =task.getException().getMessage();
                    Toast.makeText(getApplicationContext(), "Error : " +  message, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
                progressDialog.cancel();

            }
        });
    }
}