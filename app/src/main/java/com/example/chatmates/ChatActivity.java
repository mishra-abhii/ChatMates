package com.example.chatmates;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatmates.firebase.FCMSend;
import com.example.chatmates.helper.MessageAdapter;
import com.example.chatmates.helper.Messages;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import yuku.ambilwarna.AmbilWarnaDialog;


public class ChatActivity extends AppCompatActivity {

    ImageButton sendMsg,sendFiles;
    String msgRecId,msRecname,msgRecImg="",messageSenderid, senderName;
    String receiver_fcm_token;
    TextView username , lastSeen;
    FirebaseAuth auth;
    DatabaseReference RootRef;
    Button ConatctSettings, changeBgColor;
    ImageButton btnSendLocation;
    RelativeLayout chatRelativeLayout;
    int defaultColor;
    CircleImageView userImg;
    Uri fileuri;
    EditText message;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    StorageReference storageReference;
    List<Messages> messagesList=new ArrayList<>();
    RecyclerView recyclerView;
    String saveCurrentTime,saveCurrentDate,checker="";
    UploadTask uploadTask;
    String latitude, longitude;

    LocationManager locationManager;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SharedPreferences getSharedPreferences=getSharedPreferences("receiver_data",MODE_PRIVATE);
        senderName = getSharedPreferences.getString("senderName","Your ChatMate");

        auth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
        SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a");
        saveCurrentDate=dateFormat.format(calendar.getTime());
        saveCurrentTime=timeFormat.format(calendar.getTime());

        msgRecId=getIntent().getExtras().get("uid").toString();
        msRecname=getIntent().getExtras().get("name").toString();
        msgRecImg=getIntent().getExtras().get("image").toString();
        message=findViewById(R.id.chat_input_message);
        toolbar=findViewById(R.id.custom_chat_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        changeBgColor =findViewById(R.id.changeBackgroundColor);
        chatRelativeLayout = findViewById(R.id.chatRelativeLayout);

        defaultColor = ContextCompat.getColor(ChatActivity.this, R.color.colorBg);
        changeBgColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               AmbilWarnaDialog coloPicker = new AmbilWarnaDialog(ChatActivity.this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                   @Override
                   public void onCancel(AmbilWarnaDialog dialog) {

                   }
                   @Override
                   public void onOk(AmbilWarnaDialog dialog, int color) {
                       defaultColor = color;
                       chatRelativeLayout.setBackgroundColor(defaultColor);
                       RootRef.child("Users").child(msgRecId).child("chatBgColor").setValue(defaultColor);
                   }
               });
               coloPicker.show();
            }
        });

        ConatctSettings=findViewById(R.id.contactsettings);

        ConatctSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendToProfileActivity();
            }
        });
        username=findViewById(R.id.custom_profile_name);
        lastSeen=findViewById(R.id.custom_user_last_seen);
        userImg=findViewById(R.id.custom_profile_image);
        sendMsg=findViewById(R.id.send_message_chat);
        sendFiles=findViewById(R.id.file_attachment);
        messageSenderid= Objects.requireNonNull(auth.getCurrentUser()).getUid();

        messageAdapter=new MessageAdapter(messagesList,getApplicationContext());
        if (msgRecId!=null && msRecname!=null)
        {
            username.setText(msRecname);
            if (msgRecImg!=null)
            {
                GetImage(msgRecImg,userImg);

            }

        }
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        btnSendLocation = findViewById(R.id.btnSendLocation);
        btnSendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Toast.makeText(ChatActivity.this, "GPS is ON", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChatActivity.this, MapsActivity.class);
                    startActivity(intent);

                    getCurrentLocation();
                }
                else{
                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(10000);
                    locationRequest.setFastestInterval(10000/2);

                    LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();

                    locationSettingsRequestBuilder.addLocationRequest(locationRequest);
                    locationSettingsRequestBuilder.setAlwaysShow(true);

                    SettingsClient settingsClient = LocationServices.getSettingsClient(ChatActivity.this);

                    Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());

                    task.addOnFailureListener(ChatActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(e instanceof ResolvableApiException){

                                try {
                                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                    resolvableApiException.startResolutionForResult(ChatActivity.this,
                                            REQUEST_CHECK_SETTINGS);
                                }
                                catch (IntentSender.SendIntentException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });


        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView=findViewById(R.id.chat_recycler_view);
        DisplayLastSeen();
        progressDialog=new ProgressDialog(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);
        sendFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]=new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"
                        };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0)
                        {
                            checker="image";
                            Intent intent=new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent,"Select Image from here"),
                                    438);
                        }
                        if(i==1)
                        { checker="pdf";
                            Intent intent=new Intent();
                            intent.setType("application/pdf");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent,"Select Pdf from here"),
                                    438);
                        }
                        if(i==2)
                        {   checker="docx";
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                            startActivityForResult(intent, 438);
                        }
                    }
                });
                builder.show();
            }
        });
        setRecyclerView();

    }

    private void getCurrentLocation(){

        RootRef.child("Location").child(messageSenderid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("latitude") && snapshot.hasChild("longitude")){
                    latitude = Objects.requireNonNull(snapshot.child("latitude").getValue()).toString();
                    longitude = Objects.requireNonNull(snapshot.child("longitude").getValue()).toString();

                    message.setText(new StringBuilder().append("Latitude : ").append(latitude).append("\n").append("Longitude : ").append(longitude).toString());


                    RootRef.child("Location").child(messageSenderid).child("latitude").removeValue();
                    RootRef.child("Location").child(messageSenderid).child("longitude").removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//
//        RootRef.child("Location").child(messageSenderid).child("latitude").removeValue();
//        RootRef.child("Location").child(messageSenderid).child("longitude").removeValue();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

    private void updateUserStatus(String status) {
        FirebaseUser currentUser=auth.getCurrentUser();
        String currentUserId= currentUser.getUid();
        RootRef.child("Users").child(currentUserId).child("userState").child("state").setValue(status);
    }


    private void setRecyclerView() {
        RootRef.child("Messages").child(messageSenderid).child(msgRecId)
                .addChildEventListener(new ChildEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Messages messages=snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void DisplayLastSeen() {
        RootRef.child("Users").child(msgRecId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userState").hasChild("state")) {
                    String state = snapshot.child("userState").child("state").getValue().toString();
                    String date = snapshot.child("userState").child("date").getValue().toString();
                    String time = snapshot.child("userState").child("time").getValue().toString();


                    SharedPreferences sharedPreferences=getSharedPreferences("stateData",MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("userState", state);
                    editor.apply();

                    if(snapshot.hasChild("fcm-token")) {
                        receiver_fcm_token = snapshot.child("fcm-token").getValue().toString();
                    }

                    if(snapshot.hasChild("chatBgColor")) {
                        String bgColor = snapshot.child("chatBgColor").getValue().toString();
                        chatRelativeLayout.setBackgroundColor(Integer.parseInt(bgColor));
                    }



                    if (state.equals("online")) {
                        lastSeen.setText("online");

                    } else if (state.equals("offline")) {

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy");
                        String CurrentDate = dateFormat.format(calendar.getTime());
                        if (CurrentDate.equals(date)) {
                            lastSeen.setText(time.toLowerCase(Locale.ROOT));

                        } else {
                            lastSeen.setText(date);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void SendMessage() {

        String messageText=message.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            message.setError("Please enter a message");

        }
        else
        {
            String messageSenderRef="Messages/"+messageSenderid+"/"+msgRecId;
            String messageRecieverRef="Messages/"+msgRecId+"/"+messageSenderid;
            DatabaseReference userMessageRef=RootRef.child("Messages")
                    .child(messageSenderid).child(msgRecId).push();
            String messagePushId=userMessageRef.getKey();
            Map messageTextReady = new HashMap();
            messageTextReady.put("message",messageText);
            messageTextReady.put("type","text");
            messageTextReady.put("from",messageSenderid);
            messageTextReady.put("to",msgRecId);
            messageTextReady.put("messageID",messagePushId);
            messageTextReady.put("time", saveCurrentTime);
            messageTextReady.put("date", saveCurrentDate);
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextReady);
            messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextReady);
            RootRef.updateChildren(messageBodyDetails);
            message.setText("");

            // sending Push Notification
            FCMSend.pushNotification(ChatActivity.this, receiver_fcm_token, senderName, messageText);

        }
    }


    private void SendToProfileActivity() {
        Intent profileIntent=new Intent(ChatActivity.this,ProfileActivity.class);
        profileIntent.putExtra("visited_uid",msgRecId);
        startActivity(profileIntent);
    }

    private void GetImage(String currentUser, CircleImageView imageView) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().
                child("Profile Images/" + currentUser + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(imageView);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==438 && resultCode==RESULT_OK)
        {
            progressDialog.setTitle("Sending File");
            progressDialog.setMessage("Please wait , we are sending the file");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            fileuri=data.getData();
            if (checker.equals("pdf") || checker.equals("docx"))
            {
                StorageReference storageReference=FirebaseStorage.getInstance().getReference().child("Document Files");
                String messageSenderRef="Messages/"+messageSenderid+"/"+msgRecId;
                String messageRecieverRef="Messages/"+msgRecId+"/"+messageSenderid;
                DatabaseReference userMessageRef=RootRef.child("Messages")
                        .child(messageSenderid).child(msgRecId).push();
                String messagePushId=userMessageRef.getKey();
                StorageReference filePath= storageReference.child(messagePushId+"."+checker);
                filePath.putFile(fileuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Map messageTextReady = new HashMap();
                        messageTextReady.put("message",messagePushId);
                        messageTextReady.put("name",fileuri.getLastPathSegment());
                        messageTextReady.put("type",checker);
                        messageTextReady.put("from",messageSenderid);
                        messageTextReady.put("to",msgRecId);
                        messageTextReady.put("messageID",messagePushId);
                        messageTextReady.put("time", saveCurrentTime);
                        messageTextReady.put("date", saveCurrentDate);
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextReady);
                        messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextReady);
                        RootRef.updateChildren(messageBodyDetails);
                        message.setText("");
                        progressDialog.dismiss();

                        FCMSend.pushNotification(ChatActivity.this, receiver_fcm_token, senderName, "Pdf/Docx");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), ""+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                });


            }
            else if (checker.equals("image"))
            {
                StorageReference storageReference=FirebaseStorage.getInstance().getReference().child("Image Files");
                String messageSenderRef="Messages/"+messageSenderid+"/"+msgRecId;
                String messageRecieverRef="Messages/"+msgRecId+"/"+messageSenderid;
                DatabaseReference userMessageRef=RootRef.child("Messages")
                        .child(messageSenderid).child(msgRecId).push();
                String messagePushId=userMessageRef.getKey();
                StorageReference filePath= storageReference.child(messagePushId+".jpg");
                uploadTask=filePath.putFile(fileuri);
                uploadTask.continueWith(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Map messageTextReady = new HashMap();
                        messageTextReady.put("message",messagePushId);
                        messageTextReady.put("name",fileuri.getLastPathSegment());
                        messageTextReady.put("type",checker);
                        messageTextReady.put("from",messageSenderid);
                        messageTextReady.put("to",msgRecId);
                        messageTextReady.put("messageID",messagePushId);
                        messageTextReady.put("time", saveCurrentTime);
                        messageTextReady.put("date", saveCurrentDate);
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextReady);
                        messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextReady);
                        RootRef.updateChildren(messageBodyDetails);
                        message.setText("");
                        progressDialog.dismiss();

                        FCMSend.pushNotification(ChatActivity.this, receiver_fcm_token, senderName, "Image");
                    }
                });

            }
        }
        else
        {
            progressDialog.dismiss();
//            Toast.makeText(getApplicationContext(), "Nothing is Selected", Toast.LENGTH_SHORT).show();
        }
    }

}