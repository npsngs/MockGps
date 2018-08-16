package com.yhy.mockgps;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by hechengcheng on 2018/8/8
 */
public class MockService extends Service {
    private final int NOTIFY_ID = 999;
    private MockLocationClient client;
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String cmd = intent.getStringExtra("cmd");
        switch (cmd){
            case "ride":
            case "run":
            case "walk":
            case "stop":
                if (client != null){
                    client.stopMock();
                    client = null;
                }

                if (!"stop".equals(cmd)){
                    client = new MockLocationClient(this, cmd);
                    boolean isSuccess = client.startMock();
                    if (!isSuccess){
                        stopSelf();
                    }
                }
                break;
            case "accuracy":
                if (client != null){

                    float accuracy = intent.getFloatExtra("accuracy", 10f);
                    Log.e("accuracy", ""+accuracy);
                    client.setAccuracy(accuracy);
                }
                break;
            case "altitude":
                if (client != null) {
                    double altitude = intent.getDoubleExtra("altitude", 10d);
                    client.setAltitude(altitude);
                }
                break;
        }




        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground();
        if (client != null){
            client.stopMock();
        }
    }


    private void startForeground(){
        Notification.Builder mNotifyBuilder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getApplication().getApplicationInfo().nonLocalizedLabel)
                .setContentText("mock location");
        Notification notification = mNotifyBuilder.build();

        startForeground(NOTIFY_ID, notification);

    }

    private void stopForeground(){
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
