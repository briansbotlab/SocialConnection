package com.example.socialconnection.Dialog;

import android.content.Context;
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
import com.example.socialconnection.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditChatRoomDialog {
    private String dialogTitle;
    private Context context;
    private ChatRoom chatRoom;


    public EditChatRoomDialog(String dialogTitle, Context context, ChatRoom chatRoom) {
        this.dialogTitle = dialogTitle;
        this.context = context;
        this.chatRoom = chatRoom;
    }


    public String getDialogTitle() {
        return dialogTitle;
    }
    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void showDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_chatroom_template, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.txt_dialog_chatroom_name);
        final Switch switchSecret = (Switch)dialogView.findViewById(R.id.switch_dialog_chatroom_issecret);
        final EditText editTextPassword = (EditText) dialogView.findViewById(R.id.txt_dialog_chatroom_password);
        final Button buttonEdit = (Button) dialogView.findViewById(R.id.btn_dialog_chatroom_left);
        final Button buttonCancel = (Button) dialogView.findViewById(R.id.btn_dialog_chatroom_right);

        buttonEdit.setText("Edit");
        buttonCancel.setText("Cancel");

        dialogBuilder.setTitle(dialogTitle);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        editTextName.setText(chatRoom.getChatRoomName());

        if(chatRoom.getSecret_status().equals("secret")){
            switchSecret.setChecked(true);
            editTextPassword.setVisibility(View.VISIBLE);
            editTextPassword.setText(chatRoom.getPassword());
        }else if(chatRoom.getSecret_status().equals("not_secret")){
            switchSecret.setChecked(false);
            editTextPassword.setVisibility(View.GONE);
        }


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


        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                boolean secret =  switchSecret.isChecked();
                String pass = editTextPassword.getText().toString().trim();
                try {
                    if(! TextUtils.isEmpty(name) ) {

                        if(secret && !TextUtils.isEmpty(pass)){
                            updateChatRoom(name,"secret",pass);
                        }else if(!secret){
                            updateChatRoom(name,"not_secret",pass);
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

    private void updateChatRoom(String roomName, String roomSecret, String roomPass){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoom.getId());


        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("chatRoomName",roomName);
        hashMap.put("password",roomPass);
        hashMap.put("status","offline");
        hashMap.put("search",roomName.toLowerCase());
        hashMap.put("secret_status",roomSecret);

        reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,"Chat Room Updated!",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
