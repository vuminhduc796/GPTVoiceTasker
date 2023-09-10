package com.research.activityinvoker.ViewActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.research.activityinvoker.R;

public class XmlViewerActivity extends AppCompatActivity {

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xmlviewer);
        Intent intent = getIntent();
        String xmlContent = intent.getExtras().getString("xmlContent");
        textView = findViewById(R.id.xml_data);
        Log.d("Manifest",xmlContent);
        textView.setText(xmlContent);
    }
}
