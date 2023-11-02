package com.coolweather.android;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }
   private AlarmManager manager;
   private Intent i;
   private PendingIntent pi;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return null;
    }

    @Override
    public void onCreate() {
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        i = new Intent(this, AutoUpdateService.class);
        pi = PendingIntent.getService(this,0,i, PendingIntent.FLAG_IMMUTABLE);
        String ID = "com.coolweather.android";
        String NAME = "LEFTBAR";
        Intent intent = new Intent(AutoUpdateService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notification;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(ID, NAME, manager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
            notification = new NotificationCompat.Builder(this).setChannelId(ID);
        } else {
            notification = new NotificationCompat.Builder(this);
        }
        notification.setContentTitle("正在启动后台自动更新信息服务")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .build();
        Notification notification1 = notification.build();
        startForeground(1,notification1);
        }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        opdateBingpic();
        Message message = new Message();
        message.what = 1;
        WeatherActivity.handler.sendMessage(message);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String time = prefs.getString("time","00:01");
        String[] times = time.split(":");
        int anHour =Integer.parseInt(times[0]) * 60*60*1000 + Integer.parseInt(times[1])*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Log.d("Tag","updata");
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        manager.cancel(pi);
        super.onDestroy();
    }

    private void opdateBingpic() {
        String requestBingPic ="https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String bingPic = response.body().string();
                String url = "https://cn.bing.com" + Utility.handleImageRespones(bingPic);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", url);
                editor.apply();

            }
        });
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +"&key=7b58ced554784401bcac4dd0e7ff16a7";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String reponseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(reponseText);
                    if(weather != null && weather.status.equals("ok")){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",reponseText);
                        editor.apply();
                    }
                }
            });
        }
    }
}