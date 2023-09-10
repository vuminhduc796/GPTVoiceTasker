package com.research.activityinvoker.ListAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.research.activityinvoker.ActivityViewerActivity;
import com.research.activityinvoker.model.PackageDataObject;
import com.research.activityinvoker.R;
import com.research.activityinvoker.ViewActivity.DeepLinkViewerActivity;
import com.research.activityinvoker.ViewActivity.XmlViewerActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyPackageAdapter extends RecyclerView.Adapter<MyPackageAdapter.ViewHolder> {
    private List<PackageDataObject> mData;
    private LayoutInflater mInflater;
    private Context context;

    // data is passed into the constructor
    public MyPackageAdapter(Context context, List<PackageDataObject> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.package_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = mData.get(position).name;
        holder.myTextView.setText(title);
        holder.deeplinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> data = mData.get(position).deepLinks;
                String[] dataArr = new String[data.size()];
                dataArr = data.toArray(dataArr);


                Intent i = new Intent(context, DeepLinkViewerActivity.class);
                i.putExtra("deeplinks", dataArr);
                context.startActivity(i);
            }
        });
        holder.manifestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = mData.get(position).xmlContent;
                Intent i = new Intent(context, XmlViewerActivity.class);
                i.putExtra("xmlContent", data);
                context.startActivity(i);
            }
        });
        holder.intentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, ArrayList<String>> data = mData.get(position).intentsByActivity;
                String packageName = mData.get(position).packageName;
//                String[] dataArr = new String[data.size()];
//                dataArr = data.toArray(dataArr);


                Intent i = new Intent(context, ActivityViewerActivity.class);
                i.putExtra("intents", data);
                i.putExtra("packageName",packageName );
                context.startActivity(i);
            }
        });
    }
    public void setData(List<PackageDataObject> packageDataObjects){
        mData = packageDataObjects;
    }
    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;
        Button manifestBtn;
        Button deeplinkBtn;
        Button intentBtn;
        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.itemTitle);
            manifestBtn = itemView.findViewById(R.id.viewManifestBtn);
            deeplinkBtn = itemView.findViewById(R.id.viewDeepLinkUrl);
            intentBtn = itemView.findViewById(R.id.viewIntent);
        }

    }

    // convenience method for getting data at click position
}
