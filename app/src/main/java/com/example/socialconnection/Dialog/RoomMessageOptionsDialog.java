package com.example.socialconnection.Dialog;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.example.socialconnection.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RoomMessageOptionsDialog {
    private String id,roomid;
    private String message;
    private String messageType;
    private int viewType;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    private Context context;


    public RoomMessageOptionsDialog(String id, String roomid, String message, Context context, int viewType, String messageType) {
        this.id = id;
        this.roomid = roomid;
        this.message = message;
        this.context = context;
        this.viewType = viewType;
        this.messageType = messageType;
    }

    public void showMessageOptionsDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_message_options, null);
        dialogBuilder.setView(dialogView);

        final Button buttonCopy = (Button) dialogView.findViewById(R.id.btn_dialog_message_copy);
        final Button buttonDownload = (Button) dialogView.findViewById(R.id.btn_dialog_message_download);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.btn_dialog_message_delete);

        if(viewType == MSG_TYPE_RIGHT){
            buttonDelete.setVisibility(View.VISIBLE);
        }else if(viewType == MSG_TYPE_LEFT){
            buttonDelete.setVisibility(View.GONE);
        }

        if (messageType.equals("text")){
            buttonDownload.setVisibility(View.GONE);
        }else if(messageType.equals("image")){
            buttonDownload.setVisibility(View.VISIBLE);
        }

        String dialogTitle = "Options";
        dialogBuilder.setTitle(dialogTitle);
        final AlertDialog b = dialogBuilder.create();
        b.show();


        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setClipboard(context,message);
                b.dismiss();
            }
        });

        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadFile(message);
                b.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMessage(id,roomid);
                b.dismiss();
            }
        });

    }

    private void deleteMessage(String id,String roomid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GroupChats").child(id);
        reference.removeValue();
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("GroupChatSeenList").child(roomid).child(id);
        reference2.removeValue();
    }

    private void setClipboard(Context context, String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    private void downloadFile(String url) {

        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context,"/download",System.currentTimeMillis()+"."+getFileExtension(uri));

        downloadmanager.enqueue(request);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


}
