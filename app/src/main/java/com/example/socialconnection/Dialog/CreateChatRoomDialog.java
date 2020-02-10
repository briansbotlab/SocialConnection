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

import com.example.socialconnection.Adapter.UserAdapter;
import com.example.socialconnection.LocalImageActivity;
import com.example.socialconnection.Model.ChatRoom;
import com.example.socialconnection.Model.Chatlist;
import com.example.socialconnection.Model.User;
import com.example.socialconnection.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateChatRoomDialog {
    private String dialogTitle;
    private Context context;



    public CreateChatRoomDialog( String dialogTitle, Context context) {
        this.dialogTitle = dialogTitle;
        this.context = context;

    }


    public String getDialogTitle() {
        return dialogTitle;
    }
    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }


    public void showDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_chatroom_template, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.txt_dialog_chatroom_name);
        final Switch switchSecret = (Switch)dialogView.findViewById(R.id.switch_dialog_chatroom_issecret);
        final EditText editTextPassword = (EditText) dialogView.findViewById(R.id.txt_dialog_chatroom_password);
        final Button buttonCreate = (Button) dialogView.findViewById(R.id.btn_dialog_chatroom_left);
        final Button buttonCancel = (Button) dialogView.findViewById(R.id.btn_dialog_chatroom_right);

        buttonCreate.setText("Create");
        buttonCancel.setText("Cancel");

        dialogBuilder.setTitle(dialogTitle);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        switchSecret.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked){
                    editTextPassword.setVisibility(View.VISIBLE);
                }else{
                    editTextPassword.setVisibility(View.GONE);
                    editTextPassword.setText("");
                }
            }
        });


        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                boolean secret =  switchSecret.isChecked();
                String pass = editTextPassword.getText().toString().trim();
                try {
                    if(! TextUtils.isEmpty(name) ) {

                        if(secret && !TextUtils.isEmpty(pass)){
                            addChatRoom(name,"secret",pass);
                        }else if(!secret){
                            addChatRoom(name,"not_secret",pass);
                        }else if(secret){
                            Toast.makeText(context,"Operation deny. Please check your input data.", Toast.LENGTH_SHORT).show();
                        }

                        b.dismiss();
                    }
                }catch(Exception e){
                    Toast.makeText(context,"Operation deny. Please check your input data.", Toast.LENGTH_SHORT).show();
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

    private void addChatRoom(String roomName, String roomSecret, String roomPass)
    {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms");

        String id = reference.push().getKey();
        String manager = FirebaseAuth.getInstance().getCurrentUser().getUid();


        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",id);
        hashMap.put("chatRoomName",roomName);
        hashMap.put("manager",manager);
        hashMap.put("password",roomPass);
        hashMap.put("imageURL","default");
        hashMap.put("status","offline");
        hashMap.put("search",roomName.toLowerCase());
        hashMap.put("secret_status",roomSecret);

        reference.child(id).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(context,"Chat Room creating task Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addtoChatlist(manager,id);

    }

    private void addtoChatlist(final String manager, final String chatRoomId){
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(manager)
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
                .child(manager);
        chatRefReceiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id",manager);
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
