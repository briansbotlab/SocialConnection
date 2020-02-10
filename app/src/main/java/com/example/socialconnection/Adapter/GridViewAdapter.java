package com.example.socialconnection.Adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.socialconnection.LocalImageActivity;
import com.example.socialconnection.Model.Images;
import com.example.socialconnection.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GridViewAdapter extends ArrayAdapter<Images> {
    Context context;
    ViewHolder viewHolder;
    ArrayList<Images> al_menu = new ArrayList<>();
    int int_position;
    String usage;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    StorageReference storageReference;

    private StorageTask uploadTask;

    public GridViewAdapter(Context context, ArrayList<Images> al_menu,int int_position, String usage) {
        super(context, R.layout.image_folder, al_menu);
        this.al_menu = al_menu;
        this.context = context;
        this.int_position = int_position;
        this.usage = usage;

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
    }


    @Override
    public int getCount() {
        //Log.e("ADAPTER LIST SIZE", al_menu.get(int_position).getAl_imagepath().size() + "");
        return al_menu.get(int_position).getAl_imagepath().size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        if (al_menu.get(int_position).getAl_imagepath().size() > 0) {
            return al_menu.get(int_position).getAl_imagepath().size();
        } else {
         return 1;
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            viewHolder = new ViewHolder() {};
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.image_folder, parent, false);
            viewHolder.tv_foldern = (TextView) convertView.findViewById(R.id.tv_folder);
            viewHolder.tv_foldersize = (TextView) convertView.findViewById(R.id.tv_folder2);
            viewHolder.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);


            convertView.setTag(viewHolder);
        } else {
        viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_foldern.setVisibility(View.GONE);
        viewHolder.tv_foldersize.setVisibility(View.GONE);


        Glide.with(context).load("file://"+ al_menu.get(int_position).getAl_imagepath().get(position))
        .apply(new RequestOptions().placeholder(R.drawable.ic_image))
        .into(viewHolder.iv_image);

        viewHolder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri imageUri = Uri.parse("file://"+ al_menu.get(int_position).getAl_imagepath().get(position));
                //Toast.makeText(getContext(),"file://"+ al_menu.get(int_position).getAl_imagepath().get(position),Toast.LENGTH_SHORT).show();
                if(uploadTask!=null && uploadTask.isInProgress()){
                    Toast.makeText(getContext(),"Upload in progress",Toast.LENGTH_SHORT).show();
                }else {
                    if(usage.equals("profile_image")){
                        uploadImage(imageUri);
                    }else if(usage.equals("message_image")){
                        uploadImage_withProgressTxt(imageUri,"Sending ...");
                    }else if(usage.equals("chatroom_image")){
                        uploadImage_withProgressTxt(imageUri,"Uploading ...");
                    }

                }

            }
        });

        return convertView;

    }



    public static class ViewHolder {
        public TextView tv_foldern, tv_foldersize;
        public ImageView iv_image;
        ViewHolder(){}

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(Uri imageUri){
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading ...");
        progressDialog.show();

        if(imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri>then(Task<UploadTask.TaskSnapshot> task)throws Exception{
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("imageURL",mUri);
                        reference.updateChildren(hashMap);
                        progressDialog.dismiss();

                        goBack_Profile();
                    }else {
                        Toast.makeText(getContext(),"Failed!",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }else {
            Toast.makeText(getContext(),"No Image selected!",Toast.LENGTH_SHORT).show();
        }
    }



    private void uploadImage_withProgressTxt(Uri imageUri,String progressDialog_message){
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(progressDialog_message);
        progressDialog.show();

        if(imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri>then(Task<UploadTask.TaskSnapshot> task)throws Exception{
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();
                        progressDialog.dismiss();

                        goBack_WithData(mUri);
                    }else {
                        Toast.makeText(getContext(),"Failed!",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }else {
            Toast.makeText(getContext(),"No Image selected!",Toast.LENGTH_SHORT).show();
        }
    }


    private void goBack_Profile(){
        Intent intent = new Intent();
        ((Activity)getContext()).setResult(Activity.RESULT_OK, intent);
        ((Activity)getContext()).finish();
    }

    private void goBack_WithData(String mUri){
        Intent intent = new Intent();
        intent.putExtra("mUri",mUri);
        ((Activity)getContext()).setResult(Activity.RESULT_OK, intent);
        ((Activity)getContext()).finish();
    }


}