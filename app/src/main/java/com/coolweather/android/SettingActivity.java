package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.coolweather.android.R;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView back;
    private Switch aSwitch;
    private TextView time;
    private TimePicker timePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        back = (ImageView) findViewById(R.id.back_button);
        aSwitch = (Switch) findViewById(R.id.open_server);
        time = (TextView)findViewById(R.id.time);
        back.setOnClickListener(this);
        aSwitch.setOnClickListener(this);
        time.setOnClickListener(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        aSwitch.setChecked(prefs.getBoolean("isopen",false));
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if(vId == R.id.back_button){
            finish();
        }else if(vId == R.id.open_server){
            Intent intent = new Intent(this,AutoUpdateService.class);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            if(aSwitch.isChecked()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
                Toast.makeText(SettingActivity.this,"后台自动更新服务已开启",Toast.LENGTH_SHORT).show();
                time.setEnabled(true);
                time.setTextColor(Color.BLACK);
            }else {
                stopService(intent);
                Toast.makeText(SettingActivity.this,"后台自动更新服务已关闭",Toast.LENGTH_SHORT).show();
                time.setEnabled(false);
                time.setTextColor(Color.GRAY);
            }
            editor.putBoolean("isopen",aSwitch.isChecked());
            editor.apply();
        } else if (vId == R.id.time) {
            new TimePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT,new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    if (minute < 10){
                        time.setText(hourOfDay+":"+"0"+minute);
                    }else {
                       time.setText(hourOfDay+":"+minute);
                    }
                }
            }, 0, 1, true).show();
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit();
            editor.putString("time",time.getText().toString());
            editor.apply();
        }
    }
}