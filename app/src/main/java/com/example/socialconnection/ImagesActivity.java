package com.example.socialconnection;


import android.os.Bundle;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.socialconnection.Adapter.GridViewAdapter;

public class ImagesActivity  extends AppCompatActivity {
    int int_position;
    String toolbar_title;
    private GridView gridView;
    GridViewAdapter adapter;
    String usage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_image);

        gridView = (GridView)findViewById(R.id.gv_folder);
        int_position = getIntent().getIntExtra("value", 0);
        toolbar_title = getIntent().getStringExtra("folder_name");
        usage = getIntent().getStringExtra("usage");
        adapter = new GridViewAdapter(this,LocalImageActivity.al_images,int_position,usage);
        gridView.setAdapter(adapter);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(toolbar_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

}
