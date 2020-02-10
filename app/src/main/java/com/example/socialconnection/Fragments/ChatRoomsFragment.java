package com.example.socialconnection.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialconnection.Adapter.ChatRoomAdapter;
import com.example.socialconnection.Adapter.UserAdapter;
import com.example.socialconnection.Dialog.CreateChatRoomDialog;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

/**

 */
public class ChatRoomsFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<ChatRoom> mChatRoom;
    private ChatRoomAdapter chatRoomAdapter;
    private List<Chatlist> mChatList;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    EditText search_chatrooms;
    Button btn_add_chatroom;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatrooms,container,false);

        search_chatrooms = view.findViewById(R.id.search_chatrooms);
        btn_add_chatroom = view.findViewById(R.id.btn_add_chatroom);

        recyclerView = view.findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mChatList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mChatRoom = new ArrayList<>();
        chatList();


        btn_add_chatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateChatRoomDialog createChatRoomDialog = new CreateChatRoomDialog("Create a new Chat Room",getContext());
                createChatRoomDialog.showDialog();
            }
        });

        search_chatrooms.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchChatRooms(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




        return view;
    }

    private void chatList(){

        reference = FirebaseDatabase.getInstance().getReference("ChatRooms");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatRoom.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);
                    mChatRoom.add(chatRoom);
                }

                chatRoomAdapter = new ChatRoomAdapter(getContext(),mChatRoom,false);
                recyclerView.setAdapter(chatRoomAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void searchChatRooms(String s){

        Query query = FirebaseDatabase.getInstance().getReference("ChatRooms").orderByChild("search")
                .startAt(s).endAt(s + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatRoom.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);


                    assert chatRoom != null;

                    mChatRoom.add(chatRoom);

                }

                chatRoomAdapter = new ChatRoomAdapter(getContext(),mChatRoom,true);
                recyclerView.setAdapter(chatRoomAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




}
