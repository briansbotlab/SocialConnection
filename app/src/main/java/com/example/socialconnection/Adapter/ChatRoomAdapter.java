package com.example.socialconnection.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialconnection.Dialog.EnterChatRoomDialog;
import com.example.socialconnection.Model.ChatRoom;
import com.example.socialconnection.Model.GroupChat;
import com.example.socialconnection.R;
import com.example.socialconnection.RoomMessageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHandler> {
    private Context mContext;
    private List<ChatRoom> mChatRooms;
    private boolean ischat;

    String theLastMessage;

    public ChatRoomAdapter(Context mContext, List<ChatRoom> mChatRooms, boolean ischat){
        this.mContext = mContext;
        this.mChatRooms = mChatRooms;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHandler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_chatroom, parent, false);
        return new ChatRoomAdapter.ViewHandler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHandler holder, int position) {
        final ChatRoom chatRoom = mChatRooms.get(position);
        holder.chatroom_name.setText(chatRoom.getChatRoomName());

        if(chatRoom.getImageURL().equals("default")){
            holder.chatroom_image.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(mContext).load(chatRoom.getImageURL()).into(holder.chatroom_image);
        }


        if(chatRoom.getSecret_status().equals("secret")){
            holder.chatroom_lock.setVisibility(View.VISIBLE);
        }else if(chatRoom.getSecret_status().equals("not_secret")){
            holder.chatroom_lock.setVisibility(View.GONE);
        }


        if (ischat){
            lastMessage(chatRoom.getId(), holder.last_msg);
        } else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (ischat){
            if (chatRoom.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterChatRoomDialog enterChatRoomDialog = new EnterChatRoomDialog(chatRoom.getChatRoomName(),mContext,chatRoom);
                enterChatRoomDialog.showDialog();



            }
        });


    }

    @Override
    public int getItemCount() {
        if(mChatRooms != null){
            return mChatRooms.size();
        }else {
            return 0;
        }

    }

    public class ViewHandler extends RecyclerView.ViewHolder{
        public TextView chatroom_name,last_msg;
        public ImageView chatroom_image;
        private ImageView chatroom_lock;
        private ImageView img_on;
        private ImageView img_off;


        public ViewHandler(@NonNull View itemView) {
            super(itemView);

            chatroom_name = itemView.findViewById(R.id.chatroom_name);
            chatroom_image = itemView.findViewById(R.id.chatroom_image);
            chatroom_lock = itemView.findViewById(R.id.chatroom_lock);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);

        }

    }

    private void lastMessage(final String roomid, final TextView last_msg){

        theLastMessage = "default";

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GroupChats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    GroupChat groupChat = snapshot.getValue(GroupChat.class);

                    if(groupChat.getRoomid().equals(roomid)){
                        if(groupChat.getType().equals("text")){
                            theLastMessage = groupChat.getMessage();
                        }else if(groupChat.getType().equals("image")){
                            theLastMessage = "Sent a photo.";
                        }

                    }
                }



                switch(theLastMessage){
                    case "default":
                        last_msg.setText("No message!");
                    break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";

            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
