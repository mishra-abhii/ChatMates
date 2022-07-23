package com.example.chatmates.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatmates.R;
import com.example.chatmates.StatusActivity;
import com.example.chatmates.helper.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


public class StatusFragment extends Fragment {

    View statusView;
    RecyclerView recyclerView;
    FirebaseAuth auth;
    StorageReference storageReference;
    String currentUser;
    DatabaseReference databaseReference;
    FloatingActionButton floatingActionButton;
    DatabaseReference UserRef;

    private Uri FilePath;
    private ActivityResultLauncher<String> ImagePicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        statusView = inflater.inflate(R.layout.fragment_status, container, false);

        auth=FirebaseAuth.getInstance();
        currentUser=auth.getCurrentUser().getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Status");
        storageReference= FirebaseStorage.getInstance().getReference().child("Status");
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView=statusView.findViewById(R.id.status_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        floatingActionButton=statusView.findViewById(R.id.status_float_btn);


        ImagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        FilePath = result;

                        if (FilePath!=null) {

                            final ProgressDialog progressDialog=new ProgressDialog(getContext());
                            progressDialog.setTitle("Uploading");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();

                            StorageReference filePath = storageReference.child(currentUser + ".jpg");
                            filePath.putFile(FilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressDialog.cancel();
                                    Calendar c = Calendar.getInstance();
                                    c.add(Calendar.DATE, 1);
                                    Date validate = c.getTime();
                                    Date date = Calendar.getInstance().getTime();
                                    DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");
                                    String today = dateFormat.format(date);
                                    String valid = dateFormat.format(validate);
                                    UserRef.child(currentUser).child("timeUploaded").setValue(today);
                                    UserRef.child(currentUser).child("valid").setValue(valid);
                                    Toast.makeText(getContext(), "Story has been posted", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.cancel();
                                    Toast.makeText(getContext(), "Story can't be posted", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                                }
                            });
                        }
                    }
                });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.launch("image/*");
            }
        });

        LoadRecyclerViewForStatus();
        return statusView;
    }


    private void LoadRecyclerViewForStatus() {
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UserRef,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,StatusViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, StatusViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull StatusViewHolder holder, int position, @NonNull Contacts model) {
                holder.hide.setVisibility(View.GONE);
                holder.unHide.setVisibility(View.GONE);
                if (model.getTimeUploaded()!=null && model.getValid()!=null)
                {
                    Calendar c = Calendar.getInstance();
                    Date date = c.getTime();
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");
                    String today = dateFormat.format(date);
                    Date val =null,now=null;
                    try{
                        val= new SimpleDateFormat("hh:mm:ss dd-MM-yyyy").parse(model.getValid());
                        now= new SimpleDateFormat("hh:mm:ss dd-MM-yyyy").parse(today);

                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                    if (val.getTime()>now.getTime())
                    {
                        holder.hide.setVisibility(View.GONE);
                        holder.unHide.setVisibility(View.VISIBLE);
                        holder.userName.setText(model.getName());
                        holder.userTime.setText(model.getStatus());
                        try {
                            GetImage(holder.profileImage,model.getUid());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        holder.profileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(getActivity(), StatusActivity.class);
                                intent.putExtra("uid",model.getUid());
                                intent.putExtra("time",model.getTimeUploaded());
                                intent.putExtra("username",model.getName());
                                startActivity(intent);
                            }
                        });


                    }
                    else
                    {
                        holder.hide.setVisibility(View.VISIBLE);
                        holder.unHide.setVisibility(View.GONE);
                    }

                }
                else
                {
                    holder.hide.setVisibility(View.VISIBLE);
                    holder.unHide.setVisibility(View.GONE);
                }
            }

            @NonNull
            @Override
            public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.status_layout,parent,false);
                StatusViewHolder viewHolder=new StatusViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void GetImage(CircleImageView profileImage, String uid) throws IOException {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().
                child("Status/" + uid + ".jpg");
        File localFile=File.createTempFile(uid,".jpg");
        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bitmap= BitmapFactory.decodeFile(localFile.getAbsolutePath());
                profileImage.setImageBitmap(bitmap);
            }
        });


    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userTime;
        CircleImageView profileImage;
        LinearLayout hide, unHide;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.user_display_name);
            userTime=itemView.findViewById(R.id.user_upload_time);
            profileImage=itemView.findViewById(R.id.status_image);
            hide=itemView.findViewById(R.id.hide);
            unHide =itemView.findViewById(R.id.unhide);
            hide.setVisibility(View.GONE);
            unHide.setVisibility(View.GONE);

        }
    }



}
