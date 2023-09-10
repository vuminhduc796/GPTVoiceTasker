package com.research.activityinvoker.ViewActivity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.research.activityinvoker.R;

public class IntentViewerActivity  extends AppCompatActivity {

    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.deeplinkviewer_layout);
        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        String[] intents =intent.getExtras().getStringArray("intents");
        String packageName = intent.getExtras().getString("packageName");
        String activityName = intent.getExtras().getString("activityName");
        listView = findViewById(R.id.deeplinkListview);
        listView.setAdapter(new ArrayAdapter< String >(this, android.R.layout.simple_list_item_1, intents));
        listView.setOnItemClickListener(new ExpandableListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String intentName = intents[position];
                Log.d("testt", intentName+ " / " + packageName +" / " + activityName);
                Intent intent = new Intent (intentName);
                intent.setComponent(new ComponentName(packageName,activityName));

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
    }

}