package com.example.socialconnection.Dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.socialconnection.R;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditProfileDialog {
    private String userid;
    private String dialogTitle;
    private String dialogContent;
    private Context context;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }

    public EditProfileDialog(String userid, String dialogTitle, String dialogContent, Context context) {
        this.userid = userid;
        this.dialogTitle = dialogTitle;
        this.dialogContent = dialogContent;
        this.context = context;
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public String getDialogContent() {
        return dialogContent;
    }

    public void setDialogContent(String dialogContent) {
        this.dialogContent = dialogContent;
    }

    public void showUpdateDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_profile_edit, null);
        dialogBuilder.setView(dialogView);


        final EditText editTextContent = (EditText) dialogView.findViewById(R.id.txt_dialog_edit_content);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.btn_dialog_edit_update);
        final Button buttonCancel = (Button) dialogView.findViewById(R.id.btn_dialog_edit_cancel);
        editTextContent.setText(dialogContent);
        dialogBuilder.setTitle(dialogTitle);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        final String title = dialogTitle;
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = editTextContent.getText().toString().trim();
                try {
                    //checking if the value is provided
                    if(! TextUtils.isEmpty(content)) {


                        updateProfile(userid, content);
                        b.dismiss();
                    }
                }catch(Exception e){
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


    private void updateProfile(String userId, String username) {
        //getting the specified user reference
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        //updating
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("username",username);


        reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,"Profile Updated!",Toast.LENGTH_SHORT).show();
            }
        });

    }


}
