package com.research.activityinvoker.ViewActivity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.research.activityinvoker.R;
import com.research.activityinvoker.services.ActionFulfilment;

public class ActionChooserActivity extends AppCompatActivity {
    Button btn;
    ListView deeplinkListView;
    ListView intentListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_selection_layout);
        Intent intent = getIntent();
        String[] deeplinks = intent.getExtras().getStringArray("deeplinks");
        String[] intents = intent.getExtras().getStringArray("intents");
        String packageName = intent.getExtras().getString("packageName");
        String manifest = intent.getExtras().getString("manifest");
        btn = findViewById(R.id.serviceBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = manifest;
                Intent i = new Intent(getApplicationContext(), XmlViewerActivity.class);
                i.putExtra("xmlContent", data);

                startActivity(i);
            }
        });

        deeplinkListView = findViewById(R.id.deeplinkChooser);
        deeplinkListView.setAdapter(new ArrayAdapter< String >(this, android.R.layout.simple_list_item_1, deeplinks));
        deeplinkListView.setOnItemClickListener(new ExpandableListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent (Intent.ACTION_VIEW);
                intent.setData (Uri.parse( deeplinks[position]));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Define what your app should do if no activity can handle the intent.

                    Log.d("Invocation Errors", e.getMessage());
                }
            }
        });

        intentListView = findViewById(R.id.intentChooser);
        intentListView.setAdapter(new ArrayAdapter< String >(this, android.R.layout.simple_list_item_1, intents));
        intentListView.setOnItemClickListener(new ExpandableListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String intentName = intents[position];
                String[] intents = intentName.split("  ");
                Log.d("testt", intentName+ " / " + packageName +" / " + intents[1]);
                Intent intent = new Intent (intents[0]);
                intent.setComponent(new ComponentName(packageName,intents[1]));

                if(intentName.equals("com.google.android.gms.actions.SEARCH_ACTION") ){
                    intent.putExtra("query","testing string");
                }
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Define what your app should do if no activity can handle the intent
                    Log.d("Invocation Errors", e.getMessage());
                }


            }
        });
    }}