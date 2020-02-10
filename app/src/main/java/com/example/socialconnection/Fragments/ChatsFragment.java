package com.example.socialconnection.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialconnection.Adapter.ChatRoomAdapter;
import com.example.socialconnection.Adapter.UserAdapter;
import com.example.socialconnection.Model.Chat;
import com.example.socialconnection.Model.ChatRoom;
import com.example.socialconnection.Model.Chatlist;
import com.example.socialconnection.Model.User;
import com.example.socialconnection.Notifications.Token;
import com.example.socialconnection.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

/**

 */
public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView_users,recyclerView_rooms;

    private List<User> mUser;
    private List<ChatRoom> mRoom;

    private UserAdapter userAdapter;
    private ChatRoomAdapter roomAdapter;

    private List<Chatlist> userList,roomList;


    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats,container,false);

        recyclerView_users = view.findViewById(R.id.recycle_view_users);
        recyclerView_users.setHasFixedSize(true);
        recyclerView_users.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    userList.add(chatlist);

                }
                chatList_users();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recyclerView_rooms = view.findViewById(R.id.recycle_view_rooms);
        recyclerView_rooms.setHasFixedSize(true);
        recyclerView_rooms.setLayoutManager(new LinearLayoutManager(getContext()));

        roomList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    roomList.add(chatlist);

                }
                chatList_rooms();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;
    }

    private void chatList_users(){
        mUser = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    for (Chatlist chatlist : userList){
                        if (user.getId().equals(chatlist.getId())){
                            mUser.add(user);
                        }
                    }
                }

                userAdapter = new UserAdapter(getContext(),mUser,true);
                recyclerView_users.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void chatList_rooms(){
        mRoom = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ChatRooms");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRoom.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);

                    for (Chatlist chatlist : roomList){
                        if (chatRoom.getId().equals(chatlist.getId())){
                            mRoom.add(chatRoom);
                        }
                    }
                }

                roomAdapter = new ChatRoomAdapter(getContext(),mRoom,false);
                recyclerView_rooms.setAdapter(roomAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);

    }

}
