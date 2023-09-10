package com.research.activityinvoker.ViewActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.research.activityinvoker.R;

import java.util.Arrays;

public class DeepLinkViewerActivity extends AppCompatActivity {

    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deeplinkviewer_layout);
        Intent intent = getIntent();
        String[] xmlContent = intent.getExtras().getStringArray("deeplinks");
        Log.d("deeplink", Arrays.toString(xmlContent) +"");
        listView = findViewById(R.id.deeplinkListview);
        listView.setAdapter(new ArrayAdapter< String >(this, android.R.layout.simple_list_item_1, xmlContent));
        listView.setOnItemClickListener(new ExpandableListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent (Intent.ACTION_VIEW);
                intent.setData (Uri.parse( xmlContent[position]));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Define what your app should do if no activity can handle the intent.

                    Log.d("Invocation Errors", e.getMessage());
                }
            }
        });
    }}
