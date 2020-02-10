package com.example.socialconnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialconnection.Adapter.UserAdapter;
import com.example.socialconnection.Dialog.EditChatRoomDialog;
import com.example.socialconnection.Dialog.EditProfileDialog;
import com.example.socialconnection.Model.ChatRoom;
import com.example.socialconnection.Model.Chatlist;
import com.example.socialconnection.Model.User;
import com.google.android.gms.tasks.OnSuccessListener;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomSettingActivity extends AppCompatActivity {
    UserAdapter userAdapter;
    CircleImageView chatroom_image;
    TextView chatroom_name;
    FirebaseUser firebaseUser;
    DatabaseReference reference,UserRe,ChatlistRef;
    Intent intent;
    String roomid,roomname;
    RecyclerView recyclerView;
    Button btn_edit_chatroom;

    List<String> roomUsers;
    List<User> mUsers;

    ChatRoom currentChatRoom = null;


    private final static int REQUEST_CODE = 3; // Or some number you choose
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_setting);

        intent = getIntent();
        roomid = intent.getStringExtra("chatroomid");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat Room Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatRoomSettingActivity.this, RoomMessageActivity.class);
                intent.putExtra("chatroomid",roomid);
                startActivity(intent);
                finish();
            }
        });

        chatroom_image = findViewById(R.id.chatroom_image);
        chatroom_name = findViewById(R.id.chatroom_name);
        btn_edit_chatroom = findViewById(R.id.btn_edit_chatroom);
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(roomid);
        ChatlistRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(roomid);
        UserRe = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                chatroom_name.setText(chatRoom.getChatRoomName());
                roomname = chatRoom.getChatRoomName();
                if(chatRoom.getManager().equals(firebaseUser.getUid())){
                    btn_edit_chatroom.setVisibility(View.VISIBLE);
                    currentChatRoom = chatRoom;
                }else {
                    btn_edit_chatroom.setVisibility(View.GONE);
                }

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

        roomUsers = new ArrayList<>();
        ChatlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomUsers.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    if(!chatlist.getId().equals(firebaseUser.getUid())){
                        roomUsers.add(chatlist.getId());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUsers = new ArrayList<>();
        UserRe.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(roomUsers.contains(user.getId())){
                        mUsers.add(user);
                    }
                }
                userAdapter = new UserAdapter(ChatRoomSettingActivity.this, mUsers, false);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatroom_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatRoomSettingActivity.this, LocalImageActivity.class);
                intent.putExtra("usage","chatroom_image");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        btn_edit_chatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditChatRoomDialog editChatRoomDialog = new EditChatRoomDialog("Edit Chat Room",
                        ChatRoomSettingActivity.this,
                        currentChatRoom);
                editChatRoomDialog.showDialog();
            }
        });

    }

    private void changeChatRoomImage(String mUri){
        reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(roomid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("imageURL",mUri);

        reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ChatRoomSettingActivity.this,"Image Updated!",Toast.LENGTH_SHORT).show();
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
                changeChatRoomImage(mUri);
            }else{
                //Toast.makeText(RoomMessageActivity.this,"Image sending failed.",Toast.LENGTH_SHORT).show();
            }
        }

    }
}
