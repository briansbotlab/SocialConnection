package com.example.socialconnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialconnection.Adapter.MessageAdapter;
import com.example.socialconnection.Fragments.APIService;
import com.example.socialconnection.Model.Chat;
import com.example.socialconnection.Model.Chatlist;
import com.example.socialconnection.Model.User;
import com.example.socialconnection.Notifications.Client;
import com.example.socialconnection.Notifications.Data;
import com.example.socialconnection.Notifications.MyResponse;
import com.example.socialconnection.Notifications.Sender;
import com.example.socialconnection.Notifications.Token;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    FirebaseUser firebaseUser;
    DatabaseReference reference,chatRef,chatRefReceiver;
    Intent intent;

    Button btn_send,btn_image,btn_notification;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerView;

    ValueEventListener seenListener;

    String userid;

    APIService apiService;
    boolean notify = false;

    StorageReference storageReference;
    private final static int IMG_REQUEST = 1;
    private final static int REQUEST_CODE = 2; // Or some number you choose
    private Uri imageUri;
    private StorageTask uploadTask;

    Drawable d_mode_off,d_mode_on;
    boolean notify_mode,receiver_notify_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(MessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        btn_image = findViewById(R.id.btn_image);
        btn_notification = findViewById(R.id.btn_notification);
        text_send = findViewById(R.id.text_send);
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //these are about btn_notify background
        int icon_off = R.drawable.ic_bell_off;
        int icon_on = R.drawable.ic_bell_on;
        d_mode_off = getResources().getDrawable(icon_off);
        d_mode_on = getResources().getDrawable(icon_on);

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        chatRefReceiver  = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid).child(firebaseUser.getUid());
        chatRef  = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid()).child(userid);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }

                readMessage(firebaseUser.getUid(),userid,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        chatRefReceiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Chatlist chatlist = dataSnapshot.getValue(Chatlist.class);
                    receiver_notify_mode = chatlist.isNotify();

                    if(chatlist.isNotify()){
                        btn_notification.setBackground(d_mode_on);
                    }else {
                        btn_notification.setBackground(d_mode_off);
                    }
                }else{
                    receiver_notify_mode = true;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Chatlist chatlist = dataSnapshot.getValue(Chatlist.class);
                    notify_mode = chatlist.isNotify();
                }else{
                    notify_mode = true;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //set notify mode on/off
        btn_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!receiver_notify_mode){
                    btn_notification.setBackground(d_mode_on);
                    changeIsNotify();
                }else{
                    btn_notification.setBackground(d_mode_off);
                    changeIsNotify();
                }
            }
        });


        btn_send.setEnabled(false);
        btn_send.setVisibility(View.INVISIBLE);

        //send text message
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = text_send.getText().toString();
                if(!msg.equals("")){
                    sendMessage(firebaseUser.getUid(), userid, msg, "text");
                }else {
                    Toast.makeText(MessageActivity.this,"You need to input somethig",Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        //send image message
        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                Intent intent = new Intent(MessageActivity.this, LocalImageActivity.class);
                intent.putExtra("usage","message_image");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });


        text_send.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0){
                    btn_send.setEnabled(true);
                    btn_send.setVisibility(View.VISIBLE);
                }else{
                    btn_send.setEnabled(false);
                    btn_send.setVisibility(View.INVISIBLE);
                }

            }
        });


        seenMessage(userid);

    }

    private void sendMessage(String sender, final String receiver, String message, final String type){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        String id = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",id);
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);
        hashMap.put("senttime",new Date().getTime());
        hashMap.put("type",type);


        reference.child(id).setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(receiver);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id",receiver);
                    hashMap.put("notify",true);

                    chatRef.setValue(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver  = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(receiver)
                .child(firebaseUser.getUid());
        chatRefReceiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id",firebaseUser.getUid());
                    hashMap.put("notify",true);

                    chatRefReceiver.setValue(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final String text_msg = message;
        final String image_msg = "Sent a photo.";
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(notify_mode){
                    if(notify){
                        if(type.equals("text")){
                            sendNotifiaction(receiver,user.getUsername(),text_msg);
                        }else if(type.equals("image")){
                            sendNotifiaction(receiver,user.getUsername(),image_msg);
                        }
                    }
                }

                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void sendNotifiaction(final String receiver, final String username, final String message){
        DatabaseReference tokens  = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(
                            firebaseUser.getUid(),
                            android.R.drawable.stat_notify_chat,
                            username + ": " +message,
                            "New Message",
                            receiver,
                            "user",
                            "");

                    Sender sender = new Sender(data,token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if(response.code() == 200){
                                if(response.body().success != 1){
                                    Toast.makeText(MessageActivity.this,"Notification Sent Failed !",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readMessage(final String myid, final String userid, final String imageurl){
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid)
                        || chat.getReceiver().equals(userid) && chat.getSender().equals(myid)
                    ){
                        mChat.add(chat);
                    }
                }

                MessageAdapter userAdapter = new MessageAdapter(MessageActivity.this,mChat,imageurl);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentUser",userid);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("None");
    }

    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)){

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*
        if(requestCode == IMG_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() !=null){
            imageUri = data.getData();

            if(uploadTask!=null && uploadTask.isInProgress()){
                Toast.makeText(MessageActivity.this,"Upload in progress",Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }
        }

 */
        if(requestCode == REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK && data != null){
                //Toast.makeText(MessageActivity.this,"Image sending success.",Toast.LENGTH_SHORT).show();
                String mUri = data.getStringExtra("mUri");
                sendMessage(firebaseUser.getUid(), userid, mUri, "image");
            }else{
                //Toast.makeText(MessageActivity.this,"Image sending failed.",Toast.LENGTH_SHORT).show();
            }
        }

    }


    private void changeIsNotify(){
        final DatabaseReference chatRefReceiver  = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid).child(firebaseUser.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        receiver_notify_mode = !receiver_notify_mode;
        hashMap.put("notify",receiver_notify_mode);

        chatRefReceiver.updateChildren(hashMap);
    }


}
