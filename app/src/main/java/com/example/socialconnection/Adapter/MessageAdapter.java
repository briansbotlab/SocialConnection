package com.example.socialconnection.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialconnection.Dialog.MessageOptionsDialog;
import com.example.socialconnection.MessageActivity;
import com.example.socialconnection.Model.Chat;
import com.example.socialconnection.Model.User;
import com.example.socialconnection.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHandler> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> mChat,  String imageurl){
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHandler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT){
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);

        }else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new MessageAdapter.ViewHandler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHandler holder, final int position) {
       final Chat chat = mChat.get(position);
        holder.txt_senttime.setText(DateFormat.format("HH:mm:ss",
                chat.getSenttime()));
        final String chat_id = chat.getId();
        final String chat_message = chat.getMessage();
        final String type = chat.getType();
        if (type.equals("text")){
            holder.show_message.setVisibility(View.VISIBLE);
            holder.show_image.setVisibility(View.GONE);
            holder.show_message.setText(chat.getMessage());
        }else if(type.equals("image")){
            holder.show_message.setVisibility(View.GONE);
            holder.show_image.setVisibility(View.VISIBLE);
            Picasso.get().load(chat.getMessage()).placeholder(R.drawable.ic_image).into(holder.show_image);
        }


        if(imageurl.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(mContext).load(imageurl).into(holder.profile_image);
        }

        if(position == mChat.size()-1){
            if(chat.isIsseen()){
                holder.txt_seen.setText("Seen");
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
                MessageOptionsDialog messageOptionsDialog = new MessageOptionsDialog(chat_id,
                        chat_message,
                        mContext,
                        getItemViewType(position),
                        type);
                messageOptionsDialog.showMessageOptionsDialog();
                return false;
            }
        });

        holder.show_image.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                MessageOptionsDialog messageOptionsDialog = new MessageOptionsDialog(chat_id,
                        chat_message,
                        mContext,
                        getItemViewType(position),
                        type);
                messageOptionsDialog.showMessageOptionsDialog();
                return false;
            }
        });



    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHandler extends RecyclerView.ViewHolder{
        public TextView show_message,txt_seen,txt_senttime;
        public ImageView profile_image,show_image;

        public ViewHandler(@NonNull View itemView) {
            super(itemView);
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

