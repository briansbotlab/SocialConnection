package com.example.socialconnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialconnection.Adapter.RoomMessageAdapter;
import com.example.socialconnection.Fragments.APIService;
import com.example.socialconnection.Model.ChatRoom;
import com.example.socialconnection.Model.Chatlist;
import com.example.socialconnection.Model.GroupChat;
import com.example.socialconnection.Model.User;
import com.example.socialconnection.Notifications.Client;
import com.example.socialconnection.Notifications.Data;
import com.example.socialconnection.Notifications.MyResponse;
import com.example.socialconnection.Notifications.Sender;
import com.example.socialconnection.Notifications.Token;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomMessageActivity extends AppCompatActivity {

    CircleImageView chatroom_image;
    TextView chatroom_name;
    FirebaseUser firebaseUser;
    DatabaseReference reference,chatRefReceiver,chatRoomReceiverRef,userRef,mUserRef,groupChatSeenListRef,GroupChatsRef;
    Intent intent;

    Button btn_send,btn_image,btn_notification;
    EditText text_send;

    RoomMessageAdapter roomMessageAdapter;
    List<GroupChat> mChat;
    List<String> roomReceivers;

    RecyclerView recyclerView;

    ValueEventListener seenListener,updateSeenMessagelistener,mUserReflistener,GroupChatsReflistener;

    String roomid,roomname;
    String sendername;
    int seennum;



    HashMap<String,String> mImgURL;
    HashMap<String,Boolean> mNotify;

    APIService apiService;
    boolean notify = false;

    StorageReference storageReference;
    private final static int IMG_REQUEST = 1;
    private final static int REQUEST_CODE = 3; // Or some number you choose


    Drawable d_mode_off,d_mode_on;
    boolean receiver_notify_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RoomMessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        chatroom_image = findViewById(R.id.chatroom_image);
        chatroom_name = findViewById(R.id.chatroom_name);
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
        roomid = intent.getStringExtra("chatroomid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(roomid);
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        mUserRef = FirebaseDatabase.getInstance().getReference("Users");
        chatRoomReceiverRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(roomid);
        chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(roomid).child(firebaseUser.getUid());

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    chatroom_name.setText(chatRoom.getChatRoomName());
                    roomname = chatRoom.getChatRoomName();
                    if(chatRoom.getImageURL().equals("default")){
                        chatroom_image.setImageResource(R.mipmap.ic_launcher);
                    }else {
                        Glide.with(getApplicationContext()).load(chatRoom.getImageURL()).into(chatroom_image);
                    }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    sendername = user.getUsername();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        roomReceivers = new ArrayList<>();
        mNotify = new HashMap<>();
        chatRoomReceiverRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomReceivers.clear();
                mNotify.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    if(!chatlist.getId().equals(firebaseUser.getUid())){
                        mNotify.put(chatlist.getId(),chatlist.isNotify());
                        roomReceivers.add(chatlist.getId());
                    }
                }
                chatroom_name.setText(roomname+ " (" +dataSnapshot.getChildrenCount()+")");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mImgURL = new HashMap<>();

            mUserReflistener = mUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mImgURL.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);
                        if( roomReceivers.contains(user.getId()) ){
                            mImgURL.put(user.getId(),user.getImageURL());
                        }
                    }

                    readMessage(roomid,mImgURL);
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
                    sendMessage(firebaseUser.getUid(), roomid, msg, "text");
                }else {
                    Toast.makeText(RoomMessageActivity.this,"You need to input something",Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        //send image message
        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                Intent intent = new Intent(RoomMessageActivity.this, LocalImageActivity.class);
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

        chatroom_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomMessageActivity.this, ChatRoomSettingActivity.class);
                intent.putExtra("chatroomid",roomid);
                startActivity(intent);
            }
        });



    }





    private void sendMessage(String sender, final String receiver, String message, final String type){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GroupChats");
        String id = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",id);
        hashMap.put("sender",sender);
        hashMap.put("sendername",sendername);
        hashMap.put("roomid",roomid);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("senttime",new Date().getTime());
        hashMap.put("type",type);
        hashMap.put("seennum",0);

        reference.child(id).setValue(hashMap);


        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(roomid);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id",roomid);
                    hashMap.put("notify",true);

                    chatRef.setValue(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver  = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(roomid)
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
                for(String receivers :roomReceivers) {
                    if (mNotify.get(receivers)) {
                        if (notify) {
                            if (type.equals("text")) {
                                sendNotifiaction(receivers, user.getUsername(), text_msg, roomname);
                            } else if (type.equals("image")) {
                                sendNotifiaction(receivers, user.getUsername(), image_msg, roomname);
                            }
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


    private void sendNotifiaction(final String receiver, final String sender, final String message, final String chatroom_name){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(
                            firebaseUser.getUid(),
                            android.R.drawable.stat_notify_chat,
                            sender + ": " +message,
                            chatroom_name,
                            receiver,
                            "room",
                            roomid);

                    Sender sender = new Sender(data,token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if(response.code() == 200){
                                if(response.body().success != 1){
                                    Toast.makeText(RoomMessageActivity.this,"Notification Sent Failed !",Toast.LENGTH_SHORT).show();
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

    private void readMessage(final String roomid, final HashMap<String,String> imageurl){
        mChat = new ArrayList<>();
        GroupChatsRef = FirebaseDatabase.getInstance().getReference("GroupChats");

            GroupChatsReflistener = GroupChatsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mChat.clear();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        GroupChat groupChat = snapshot.getValue(GroupChat.class);

                        if(groupChat.getRoomid().equals(roomid)){
                            mChat.add(groupChat);
                        }
                    }

                    if(mChat.size() - 1 >=0){
                        updateSeenMessage(mChat.get(mChat.size() - 1).getId());
                    }

                    roomMessageAdapter = new RoomMessageAdapter(RoomMessageActivity.this,mChat,imageurl);
                    recyclerView.setAdapter(roomMessageAdapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


    }

    private void status(String status){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }

    private void currentRoom(String roomid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();

        editor.putString("currentUser",roomid);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentRoom(roomid);
    }

    @Override
    protected void onPause() {
        super.onPause();



            if(seenListener!=null){
                //seenListener = null;
                //reference = null;
                reference.removeEventListener(seenListener);
            }

        status("offline");
        currentRoom("None");
    }

    private void updateSeenMessage(final String group_chat_id){

        groupChatSeenListRef = FirebaseDatabase.getInstance().getReference("GroupChatSeenList")
                .child(roomid)
                .child(group_chat_id);
            updateSeenMessagelistener = groupChatSeenListRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    seennum = -1;
                    if (!dataSnapshot.child(firebaseUser.getUid()).exists()) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("id", firebaseUser.getUid());
                        hashMap.put("seen", true);
                        groupChatSeenListRef.child(firebaseUser.getUid()).setValue(hashMap);
                    }
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        seennum = seennum + 1;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        seenMessage(group_chat_id);
    }


    private void seenMessage(final String group_chat_id){

        reference = FirebaseDatabase.getInstance().getReference("GroupChats").child(group_chat_id);

            seenListener = reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        GroupChat groupChat = dataSnapshot.getValue(GroupChat.class);
                        if(groupChat.getRoomid() == null){
                            //donothing
                        } else if (groupChat.getRoomid().equals(roomid)) {
                            //Toast.makeText(RoomMessageActivity.this,"seen:" + Integer.toString(seennum),Toast.LENGTH_SHORT).show();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("seennum", seennum);
                            reference.updateChildren(hashMap);

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

        if(requestCode == REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK && data != null){
               // Toast.makeText(RoomMessageActivity.this,"Image sending success.",Toast.LENGTH_SHORT).show();
                String mUri = data.getStringExtra("mUri");
                sendMessage(firebaseUser.getUid(), roomid, mUri, "image");

            }else{
                //Toast.makeText(RoomMessageActivity.this,"Image sending failed.",Toast.LENGTH_SHORT).show();
            }
        }

    }


    private void changeIsNotify(){
        final DatabaseReference chatRefReceiver  = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(roomid).child(firebaseUser.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        receiver_notify_mode = !receiver_notify_mode;
        hashMap.put("notify",receiver_notify_mode);

        chatRefReceiver.updateChildren(hashMap);
    }
}
