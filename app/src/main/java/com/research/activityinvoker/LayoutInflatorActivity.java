package com.research.activityinvoker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class LayoutInflatorActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.inflater);
        LinearLayout v = findViewById(R.id.maxView);
        Context c = null;
        String youtubePackage = "com.google.android.youtube";
        String thisPackage = "com.example.com.example.com.research.activityinvoker";
        try {
            c = getApplicationContext().createPackageContext(thisPackage, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        assert c != null;

            Log.d("test",
                    c.getDataDir()+"");



        int id = c.getResources().getIdentifier("manifest_main", "layout",thisPackage );
        LayoutInflater inflater = LayoutInflater.from(c);

        if(v != null){
            View window = inflater.inflate(id, null);
            v.addView(window);
        }
//
//        ClassLoader loader = c.getClassLoader();
//
//        try {
//
//            Class util = loader.loadClass("com.google.android.apps.youtube.app.application.Shell$HomeActivity");
//            Log.d("Test", Arrays.toString(util.getDeclaredMethods()) + " 1");
//            Method getDeviceId = util.getDeclaredMethod(
//                    "onCreate", util);
//            getDeviceId.invoke(null, this);
//
//        } catch (ClassNotFoundException | NoSuchMethodException e) {
//            Log.d("Not successful", "Testtt");
//            e.printStackTrace();
//        } catch (IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
//        }

    }
}