package com.example.socialconnection.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialconnection.Dialog.RoomMessageOptionsDialog;
import com.example.socialconnection.Model.GroupChat;
import com.example.socialconnection.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class RoomMessageAdapter extends RecyclerView.Adapter<RoomMessageAdapter.ViewHandler> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<GroupChat> mChat;
    private HashMap<String,String> imageurl;

    FirebaseUser firebaseUser;

    public RoomMessageAdapter(Context mContext, List<GroupChat> mChat, HashMap<String,String> imageurl){
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public RoomMessageAdapter.ViewHandler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT){
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);

        }else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new RoomMessageAdapter.ViewHandler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RoomMessageAdapter.ViewHandler holder, final int position) {
       final GroupChat chat = mChat.get(position);
        holder.txt_senttime.setText(DateFormat.format("HH:mm:ss",
                chat.getSenttime()));
        final String username = chat.getSendername();
        final String chat_id = chat.getId();
        final String room_id = chat.getRoomid();
        final String sender_id = chat.getSender();
        final String receiver_id = chat.getReceiver();
        final String chat_message = chat.getMessage();
        final String type = chat.getType();

        if(getItemViewType(position) == MSG_TYPE_LEFT){
            holder.username.setVisibility(View.VISIBLE);
            holder.username.setText(username);
        }else {
            holder.username.setVisibility(View.GONE);
        }

        if (type.equals("text")){
            holder.show_message.setVisibility(View.VISIBLE);
            holder.show_image.setVisibility(View.GONE);
            holder.show_message.setText(chat.getMessage());
        }else if(type.equals("image")){
            holder.show_message.setVisibility(View.GONE);
            holder.show_image.setVisibility(View.VISIBLE);
            Picasso.get().load(chat_message).placeholder(R.drawable.ic_image).into(holder.show_image);
        }


        if(!imageurl.isEmpty()){
            if(imageurl.get(sender_id) == null){
                //Toast.makeText(mContext,"null",Toast.LENGTH_SHORT).show();
            }else if(imageurl.get(sender_id).equals("")){
                //Toast.makeText(mContext,"nothing",Toast.LENGTH_SHORT).show();
            }else if(imageurl.get(sender_id).equals("default")){
                holder.profile_image.setImageResource(R.mipmap.ic_launcher);
            }else {
                Glide.with(mContext).load(imageurl.get(sender_id)).into(holder.profile_image);
            }

        }

        if(position == mChat.size()-1){
            if(chat.getSeennum()>0){
                holder.txt_seen.setText("Seen"+chat.getSeennum());
            }else {
                holder.txt_seen.setText("Delivered");
            }
        }else {
            holder.txt_seen.setVisibility(View.GONE);
        }


        holder.show_message.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                RoomMessageOptionsDialog roomMessageOptionsDialog = new RoomMessageOptionsDialog(chat_id,
                        room_id,
                        chat_message,
                        mContext,
                        getItemViewType(position),
                        type);
                roomMessageOptionsDialog.showMessageOptionsDialog();
                return false;
            }
        });

        holder.show_image.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                RoomMessageOptionsDialog roomMessageOptionsDialog = new RoomMessageOptionsDialog(chat_id,
                        room_id,
                        chat_message,
                        mContext,
                        getItemViewType(position),
                        type);
                roomMessageOptionsDialog.showMessageOptionsDialog();
                return false;
            }
        });



    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHandler extends RecyclerView.ViewHolder{
        public TextView show_message,txt_seen,txt_senttime,username;
        public ImageView profile_image,show_image;

        public ViewHandler(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            txt_senttime = itemView.findViewById(R.id.txt_senttime);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            show_message = itemView.findViewById(R.id.show_message);
            show_image = itemView.findViewById(R.id.show_image);
            profile_image = itemView.findViewById(R.id.profile_image);


            show_message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(mChat.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }


}

