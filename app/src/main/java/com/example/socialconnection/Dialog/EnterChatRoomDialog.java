package com.example.socialconnection.Dialog;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;


import com.example.socialconnection.Model.ChatRoom;
import com.example.socialconnection.Model.Chatlist;
import com.example.socialconnection.R;
import com.example.socialconnection.RoomMessageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;

public class EnterChatRoomDialog {
    private String dialogTitle;
    private Context context;
    private ChatRoom room;



    public EnterChatRoomDialog( String dialogTitle, Context context, ChatRoom room) {
        this.dialogTitle = dialogTitle;
        this.context = context;
        this.room = room;
    }


    public String getDialogTitle() {
        return dialogTitle;
    }
    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public ChatRoom getRoom() {
        return room;
    }

    public void setRoom(ChatRoom room) {
        this.room = room;
    }

    public void showDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_enter_chatroom, null);
        dialogBuilder.setView(dialogView);


        final EditText editTextPassword = (EditText) dialogView.findViewById(R.id.txt_dialog_enter_chatroom_password);
        final Button buttonEnter = (Button) dialogView.findViewById(R.id.btn_dialog_enter_chatroom_enter);
        final Button buttonCancel = (Button) dialogView.findViewById(R.id.btn_dialog_enter_chatroom_cancel);

        dialogBuilder.setTitle("Enter "+dialogTitle);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        if(room.getSecret_status().equals("secret")){
            editTextPassword.setVisibility(View.VISIBLE);
        }else if(room.getSecret_status().equals("not_secret")){
            editTextPassword.setVisibility(View.GONE);
        }



        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pass = editTextPassword.getText().toString().trim();
                if(room.getSecret_status().equals("secret") && room.getPassword().equals(pass)){
                    enterChatRoom();
                }else if(room.getSecret_status().equals("not_secret")){
                    enterChatRoom();
                }else{
                    Toast.makeText(context,"Please check your input data.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.dismiss();
            }
        });
    }


    private void enterChatRoom(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference chatRoomReceiverRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(room.getId());
        chatRoomReceiverRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(firebaseUser.getUid())){
                    addtoChatlist(firebaseUser.getUid(),room.getId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Intent intent = new Intent(context, RoomMessageActivity.class);
        intent.putExtra("chatroomid",room.getId());
        context.startActivity(intent);
    }

    private void addtoChatlist(final String userid, final String chatRoomId){
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(chatRoomId);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id",chatRoomId);
                    hashMap.put("notify",true);

                    chatRef.setValue(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver  = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(chatRoomId)
                .child(userid);
        chatRefReceiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id",userid);
                    hashMap.put("notify",true);

                    chatRefReceiver.setValue(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



}
