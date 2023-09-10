package com.research.activityinvoker.ViewActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.research.activityinvoker.R;

public class SettingsActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener{

    int tooltipColor = 0;
    int tooltipOpacity = 0;
    int tooltipSize = 0;
    int buttonOpacity = 0;
    int buttonRecordTxt = 0;
    int buttonAlgoTxt = 0;

    public static final String[] tooltipColorSpinnerItems = new String[]{"blue","black","red"};
    public static final String[] tooltipOpacitySpinnerItems = new String[]{"100%","75%", "50%","25%"};
    public static final String[] tooltipSizeSpinnerItems = new String[]{"small","medium", "large"};
    public static final String[] buttonOpacitySpinnerItems = new String[]{"100%","75%", "50%","25%"};
    public static final String[] buttonRecordItems = new String[]{"hide","show"};
    public static final String[] buttonAlgoItems = new String[]{"Custom A-T Match", "AutoML"};


    SharedPreferences sharedPreferences;
    public static final String FILE_NAME = "voicify";
    public static final String BUTTON_OPACITY = "btn_opacity";
    public static final String BUTTON_RECORD = "btn_record";
    public static final String TOOLTIP_COLOR = "tooltip_color";
    public static final String TOOLTIP_SIZE = "tooltip_size";
    public static final String TOOLTIP_OPACITY = "tooltip_opacity";
    public static final String BUTTON_ALGO = "btn_algo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences(FILE_NAME,0);

        tooltipColor = sharedPreferences.getInt(TOOLTIP_COLOR,0);
        tooltipOpacity = sharedPreferences.getInt(TOOLTIP_OPACITY,0);
        tooltipSize = sharedPreferences.getInt(TOOLTIP_SIZE,0);
        buttonOpacity = sharedPreferences.getInt(BUTTON_OPACITY,0);
        buttonRecordTxt = sharedPreferences.getInt(BUTTON_RECORD,0);
        buttonAlgoTxt = sharedPreferences.getInt(BUTTON_ALGO,0);

        SharedPreferences.Editor editor = sharedPreferences.edit();         // call an editor to modify SF

        Spinner tooltipColorSpinner = findViewById(R.id.tooltip_color);
        ArrayAdapter<String> tooltipColorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tooltipColorSpinnerItems);
        tooltipColorSpinner.setAdapter(tooltipColorAdapter);
        tooltipColorSpinner.setOnItemSelectedListener(this);
        tooltipColorSpinner.setSelection(tooltipColor);

        Spinner tooltipOpacitySpinner = findViewById(R.id.tooltip_opacity);
        ArrayAdapter<String> tooltipOpacityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tooltipOpacitySpinnerItems);
        tooltipOpacitySpinner.setAdapter(tooltipOpacityAdapter);
        tooltipOpacitySpinner.setOnItemSelectedListener(this);
        tooltipOpacitySpinner.setSelection(tooltipOpacity);

        Spinner tooltipSizeSpinner = findViewById(R.id.tooltip_size);
        ArrayAdapter<String> tooltipSizeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tooltipSizeSpinnerItems);
        tooltipSizeSpinner.setAdapter(tooltipSizeAdapter);
        tooltipSizeSpinner.setOnItemSelectedListener(this);
        tooltipSizeSpinner.setSelection(tooltipSize);

        Spinner buttonOpacitySpinner = findViewById(R.id.button_opacity);
        ArrayAdapter<String> buttonOpacityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, buttonOpacitySpinnerItems);
        buttonOpacitySpinner.setAdapter(buttonOpacityAdapter);
        buttonOpacitySpinner.setOnItemSelectedListener(this);
        buttonOpacitySpinner.setSelection(buttonOpacity);

        Spinner buttonRecord = findViewById(R.id.button_record);
        ArrayAdapter<String> buttonRecordAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, buttonRecordItems);
        buttonRecord.setAdapter(buttonRecordAdapter);
        buttonRecord.setOnItemSelectedListener(this);
        buttonRecord.setSelection(buttonRecordTxt);

        Spinner buttonAlgo = findViewById(R.id.button_algo);
        ArrayAdapter<String> buttonAlgoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, buttonAlgoItems);
        buttonAlgo.setAdapter(buttonAlgoAdapter);
        buttonAlgo.setOnItemSelectedListener(this);
        buttonAlgo.setSelection(buttonAlgoTxt);

        Button saveBtn = findViewById(R.id.button3);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt(BUTTON_OPACITY,buttonOpacity);
                editor.putInt(BUTTON_RECORD,buttonRecordTxt);
                editor.putInt(BUTTON_ALGO,buttonAlgoTxt);
                editor.putInt(TOOLTIP_COLOR,tooltipColor);
                editor.putInt(TOOLTIP_SIZE,tooltipSize);
                editor.putInt(TOOLTIP_OPACITY,tooltipOpacity);
                editor.apply();
                Intent myIntent = new Intent(SettingsActivity.this, ReadManifest.class);
                startActivity(myIntent);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.tooltip_color)
        {
            tooltipColor = position;
        }
        else if(spinner.getId() == R.id.tooltip_opacity)
        {
            tooltipOpacity =  position;
        }
        else if(spinner.getId() == R.id.tooltip_size)
        {
            tooltipSize =  position;
        }
        else if(spinner.getId() == R.id.button_opacity)
        {
            buttonOpacity =  position;
        }
        else if(spinner.getId() == R.id.button_record)
        {
            buttonRecordTxt =  position;
        }
        else if(spinner.getId() == R.id.button_algo)
        {
            buttonAlgoTxt =  position;
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.tooltip_color)
        {
            tooltipColor = 0;
        }
        else if(spinner.getId() == R.id.tooltip_opacity)
        {
            tooltipOpacity =  0;
        }
        else if(spinner.getId() == R.id.tooltip_size)
        {
            tooltipSize =  0;
        }
        else if(spinner.getId() == R.id.button_opacity)
        {
            buttonOpacity =  0;
        }
        else if(spinner.getId() == R.id.button_record)
        {
            buttonRecordTxt = 0;
        }
        else if(spinner.getId() == R.id.button_algo)
        {
            buttonAlgoTxt =  0;
        }
    }
}
