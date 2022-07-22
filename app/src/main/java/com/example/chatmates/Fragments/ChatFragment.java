package com.example.chatmates.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatmates.FindFriends;
import com.example.chatmates.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class ChatFragment extends Fragment {

    View chatview;
    FloatingActionButton floatingActionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflates the layout for this fragment
        chatview = inflater.inflate(R.layout.fragment_chat, container, false);

        floatingActionButton=chatview.findViewById(R.id.chat_float_btn);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendtoFindFriendsActivity();
            }
        });

        return chatview;
    }

    private void SendtoFindFriendsActivity() {
        Intent intent=new Intent(getActivity(), FindFriends.class);
        startActivity(intent);
    }
}