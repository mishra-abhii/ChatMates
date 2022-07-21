package com.example.chatmates;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    EditText username,userstatus;
    Button update;
    CircleImageView profileImage;
    FirebaseAuth auth;
    String image;
    DatabaseReference RootRef;
    StorageReference UserProfileImg,storageReference;
    String currentUser;
    ProgressDialog progressDialog;
    String timeUploaded,valid;
    Toolbar toolbar;

    private Uri mImgUri;
    private ActivityResultLauncher<String> ImagePicker;
    private UploadTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth=FirebaseAuth.getInstance();
        currentUser=auth.getCurrentUser().getUid();
        progressDialog=new ProgressDialog(this);

        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImg= FirebaseStorage.getInstance().getReference().child("Profile Images");
        storageReference=FirebaseStorage.getInstance().getReference().child("Profile Images/"+currentUser+".jpg");

        Initialize();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(SettingsActivity.this, "Updating profile pic", Toast.LENGTH_SHORT).show();
                }
                else{
                    SubmitData();
                }
                UpdateSettings();
//
//                if(!TextUtils.isEmpty(username.getText().toString()) &&
//                        !TextUtils.isEmpty(userstatus.getText().toString())) {
////                    SendUserToMainActivity();
//                }

            }
        });

        ImagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        profileImage.setImageURI(result);
                        mImgUri = result;
                    }
                });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!TextUtils.isEmpty(username.getText().toString()) &&
                        !TextUtils.isEmpty(userstatus.getText().toString())) {

                    ImagePicker.launch("image/*");
//                    UpdateSettings();
                }
                else
                {
                    username.setError("please enter user name");
                    userstatus.setError("please enter user status");
                }
            }
        });

//        profileImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(!TextUtils.isEmpty(username.getText().toString()) &&
//                        !TextUtils.isEmpty(userstatus.getText().toString())) {
//                    UpdateSettings();
//                    Intent intent=new Intent();
//                    intent.setType("image/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    startActivityForResult(Intent.createChooser(intent,"Select Image from here"),
//                            Gallery_code);
//                }
//                else
//                {
//                    username.setError("please enter user name");
//                    userstatus.setError("please enter user status");
//                }
//            }
//        });

        RetrieveData();
    }

    private void SubmitData(){

        if(mImgUri != null){

            progressDialog.setTitle("Update Profile");
            progressDialog.setMessage("Please wait , while we are updating your account ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            StorageReference fileReference= UserProfileImg.child(currentUser+".jpg");

            mUploadTask = fileReference.putFile(mImgUri);
            Task<Uri> urlTask = mUploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    progressDialog.cancel();
                    GetImage();
                    RootRef.child("Users").child(currentUser).child("image").setValue(currentUser);
                    Toast.makeText(getApplicationContext(), "Profile Image Uploaded", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void RetrieveData() {
        RootRef.child("Users").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    if (snapshot.hasChild("name")&& snapshot.hasChild("status")&& !snapshot.hasChild("image"))
                    {
                        username.setEnabled(false);
                        String uname=snapshot.child("name").getValue().toString();
                        String ustatus=snapshot.child("status").getValue().toString();
                        username.setText(uname);
                        userstatus.setText(ustatus);
                        if (snapshot.hasChild("timeUploaded") && snapshot.hasChild("valid"))
                        {
                            timeUploaded=snapshot.child("timeUploaded").getValue().toString();
                            valid=snapshot.child("valid").getValue().toString();
                        }
                    }
                    else if (snapshot.hasChild("name")&& snapshot.hasChild("status")&& snapshot.hasChild("image"))
                    {
                        username.setEnabled(false);
                        image=snapshot.child("image").getValue().toString();
                        String uname=snapshot.child("name").getValue().toString();
                        String ustatus=snapshot.child("status").getValue().toString();
                        GetImage();
                        username.setText(uname);
                        userstatus.setText(ustatus);
                        if (snapshot.hasChild("timeUploaded") && snapshot.hasChild("valid"))
                        {
                            timeUploaded=snapshot.child("timeUploaded").getValue().toString();
                            valid=snapshot.child("valid").getValue().toString();
                        }
                    }
                    else
                    {
                        username.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), "Please Update your Profile", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    username.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Please Update your Profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void UpdateSettings() {
        String name=username.getText().toString();
        String status=userstatus.getText().toString();
        if(TextUtils.isEmpty(name))
        {
            username.setError("please enter user name");

        }
        if(TextUtils.isEmpty(status))
        {
            userstatus.setError("please enter user status");

        }
        if(TextUtils.isEmpty(name) && TextUtils.isEmpty(status))
        {
            username.setError("please enter user name");
            userstatus.setError("please enter user status");

        }
        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(status)) {

            HashMap<String , String> profileMap = new HashMap<>();
            profileMap.put("uid",currentUser);
            profileMap.put("name",name);
            profileMap.put("status",status);
            if (image!=null )
            {
                profileMap.put("image",image);
            }
            if(!TextUtils.isEmpty(timeUploaded) && !TextUtils.isEmpty(valid))
            {
                profileMap.put("timeUploaded",timeUploaded);
                profileMap.put("valid",valid);

            }
            RootRef.child("Users").child(currentUser).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(), "Data Updated", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String message=task.getException().getLocalizedMessage();
                        Toast.makeText(getApplicationContext(), "Error : "+message, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void Initialize() {
        username=findViewById(R.id.username_edit_text);
        userstatus=findViewById(R.id.status_edit_text);
        update=findViewById(R.id.update_Settings_btn);
        profileImage=findViewById(R.id.profile_image);
        toolbar=findViewById(R.id.settings_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    // for moving to Home from settings via arrow key at action bar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void GetImage() {
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(profileImage);
            }
        });
    }
}