package com.research.activityinvoker.ListAdapter;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.research.activityinvoker.R;

import java.util.HashMap;
import java.util.List;

public class MainAdapter extends BaseExpandableListAdapter {
    Context context;
    List<String> packagesName;
    HashMap<String,List<String>> activitiesName;
    List<PackageInfo> packages;
    HashMap<String,List<ActivityInfo>> activities;


    public MainAdapter(Context context, List<String> packagesName, HashMap<String, List<String>> activitiesName, List<PackageInfo> packages, HashMap<String, List<ActivityInfo>> activities) {
        this.context = context;
        this.packagesName = packagesName;
        this.activitiesName = activitiesName;
        this.packages = packages;
        this.activities = activities;
    }

    @Override
    public int getGroupCount() {
        return this.packagesName.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.activitiesName.get(this.packagesName.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.packagesName.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.activitiesName.get(this.packagesName.get(groupPosition)).get(childPosition);
    }

    public String getChildData(int groupPosition, int childPosition) {
        ActivityInfo activityInfo = this.activities.get(this.packagesName.get(groupPosition)).get(childPosition);
        return activityInfo.exported + "";
      //  return "Permission: "  + activityInfo.permission + "\n" + "Target Activity: " + activityInfo.targetActivity + "\n" + "Split name: " + activityInfo.splitName;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String group = (String) getGroup(groupPosition);
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listgroup,null);

        }
        TextView textView = convertView.findViewById(R.id.list_parent);
        textView.setText(group);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String child = (String) getChild(groupPosition,childPosition);
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listitem,null);

        }
        TextView textView = convertView.findViewById(R.id.list_child);
        textView.setText(child);
        Button mButton=(Button)convertView.findViewById(R.id.list_child_click);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.android.youtube.intent.action.CREATE_LIVE_STREAM");

                intent.setComponent(new ComponentName((String) getGroup(groupPosition), (String)getChild(groupPosition,childPosition)));
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Define what your app should do if no activity can handle the intent.

                    Log.d("Invocation Errors", e.getMessage());
                }

            }
        });
        Button mButton2 =(Button)convertView.findViewById(R.id.info_child_click);
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TEST", getChildData(groupPosition,childPosition));
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
