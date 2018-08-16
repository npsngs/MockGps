package com.yhy.mockgps;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by hechengcheng on 2018/8/10
 */
public class MockLocationClient {
    private final double NaN = 0.0d/0.0;
    private double speed = 0.0002d;/* 100m/s */
    private LocationManager lm;
    private Context context;
    private float accuracy = 10f;
    private double altitude = 10d;
    public MockLocationClient(Context context, String cmd) {
        this.context = context;
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        switch (cmd){
            case "ride":
                speed = 0.0002d;        /* 100m/s */
                break;
            case "run":
                speed = 0.00004d;        /* 10m/s */
                break;
            case "walk":
                speed = 0.000002d;        /* 1m/s */
                break;
        }
    }


    public boolean startMock() {
        try {
            //如果未开启模拟位置服务，则添加模拟位置服务
            lm.addTestProvider(LocationManager.GPS_PROVIDER
                    , false
                    , false
                    , false
                    , false
                    , true
                    , true
                    , true
                    , 0
                    , 5);
            lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            start();
        } catch (Exception e) {
            Toast.makeText(context,"请打开‘允许模拟位置’", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public void stopMock() {
        try {
            lm.clearTestProviderEnabled(LocationManager.GPS_PROVIDER);
            lm.removeTestProvider(LocationManager.GPS_PROVIDER);
            handler.getLooper().quit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Handler handler;
    private Location mlocation;
    private double longitude;
    private double latitude;
    private int direction = 0;
    private double maxForward = 0d;
    private double totalForward = 0d;
    private void start() {
        startAsync();
    }

    private void startAsync(){
        if (mlocation == null){
            mlocation = new Location(LocationManager.GPS_PROVIDER);
            longitude = 121.4888119698D;
            latitude = 31.2312568206D;

            mlocation.setBearing(0.0f);
            mlocation.setAltitude(10.0d);
            mlocation.setAccuracy(1.2f);
            mlocation.setSpeed(1.2f);
        }
        maxForward = speed;

        HandlerThread thread = new HandlerThread("MockLocation");
        thread.start();
        handler = new Handler(thread.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goDirection();
                float accuracy = getAccuracy();
                if(accuracy < 6f){
                    mlocation.setLongitude(NaN);
                    mlocation.setLatitude(NaN);
                }else{
                    mlocation.setLongitude(longitude);
                    mlocation.setLatitude(latitude);
                }

                mlocation.setAltitude(getAltitude());
                mlocation.setAccuracy(getAccuracy());


                Log.e("mock", "lnlo:"+longitude+", "+latitude);
                mlocation.setTime(System.currentTimeMillis());
                try {
                    if (android.os.Build.VERSION.SDK_INT >= 17) {
                        mlocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                    }
                    lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, mlocation);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                checkDirection();

                handler.postDelayed(this, 1000);
            }
        }, 1000);
        Toast.makeText(context, "开始模拟GPS信号",Toast.LENGTH_SHORT).show();
    }


    private void goDirection(){
        switch (direction){
            case 0:
                longitude -= speed;
                break;
            case 1:
                latitude += speed;
                break;
            case 2:
                longitude += speed;
                break;
            case 3:
                latitude -= speed;
                break;
        }
    }


    private void checkDirection(){
        totalForward += speed;
        if(totalForward > maxForward){
            maxForward += speed;
            totalForward = 0d;
            direction++;
            direction %= 4;
        }
    }







    /* between v1~ v2 */
    private double randomValue(double v1, double v2){
        double r = Math.random();
        return v1 + (v2-v1)*r;
    }


    public synchronized float getAccuracy() {
        return accuracy;
    }

    public synchronized double getAltitude() {
        return altitude;
    }

    public synchronized void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public synchronized void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
