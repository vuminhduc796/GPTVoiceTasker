package com.research.activityinvoker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.research.activityinvoker.ViewActivity.IntentViewerActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ActivityViewerActivity extends AppCompatActivity {

    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.deeplinkviewer_layout);
        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        HashMap<String, ArrayList<String>> intents = (HashMap<String, ArrayList<String>>) intent.getExtras().getSerializable("intents");
        String packageName = intent.getExtras().getString("packageName");
        listView = findViewById(R.id.deeplinkListview);
        String[] activityNames = convert(intents.keySet());
        listView.setAdapter(new ArrayAdapter< String >(this, android.R.layout.simple_list_item_1, activityNames));
        listView.setOnItemClickListener(new ExpandableListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Intent i = new Intent(getApplicationContext(), IntentViewerActivity.class);
                String keyActivityName = activityNames[position];
                ArrayList<String> dataArrayList = intents.get(keyActivityName);
                assert dataArrayList != null;
                if(!dataArrayList.contains("android.intent.action.MAIN")){
                    dataArrayList.add("android.intent.action.MAIN");
                }
                if(!dataArrayList.contains("android.intent.action.VIEW")){
                    dataArrayList.add("android.intent.action.VIEW");
                }

                String[] dataArr = new String[dataArrayList.size()];
                dataArr =  dataArrayList.toArray(dataArr);
                i.putExtra("intents",dataArr );
                i.putExtra("packageName",packageName);
                i.putExtra("activityName", keyActivityName);
                Log.d("Duccc", dataArr + "/ "+ packageName + "/ " + keyActivityName);
                startActivity(i);
            }
        });
    }
    public static String[] convert(Set<String> setOfString)
    {

        // Create String[] of size of setOfString
        String[] arrayOfString = new String[setOfString.size()];

        // Copy elements from set to string array
        // using advanced for loop
        int index = 0;
        for (String str : setOfString)
            arrayOfString[index++] = str;

        // return the formed String[]
        return arrayOfString;
    }

}