package cn.lixiaoqian.LocationUtil;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.io.IOException;
import java.util.List;

public class LocationService extends Service {
    private static final String CHANNEL_ID_STRING = "1003";
    private LocationServiceBinder mBinder = new LocationServiceBinder();
    private final static int GRAY_SERVICE_ID = 10003;
    NotificationManager notificationManager;
    String notificationId = "channelId2";
    String notificationName = "channelName2";

    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    private String locationProvider;
    private Context context;
    private int targetSdkVersion;
    private static final String TAG = "Unity";

    private static final int GET_LOCATION = 1011;
    private static String[] PERMISSION_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public void startForegroundService()
    {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        //设置Notification的ChannelID,否则不能正常显示
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(getApplicationContext(),"id1");
            builder.setChannelId(notificationId);
            Notification notification = builder.build();
            startForeground(GRAY_SERVICE_ID,notification);
        }
    }


    //用于Activity和service通讯
    class LocationServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    public void SetContext(Context ctt)
    {
        context = ctt;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startForeground(GRAY_SERVICE_ID, new Notification());
        }
        else if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            startForegroundService();
        }
        acquireWakeLock();
        return START_STICKY;
    }


    PowerManager.WakeLock wakeLock;//锁屏唤醒
    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }
    public LocationService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate:LocationUtil ");
        //1.获取位置的管理者
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //2.获取定位方式
        //2.1获取所有的定位方式，true:表示返回所有可用定位方式
        List<String> providers = locationManager.getProviders(true);
        locationProvider = LocationManager.GPS_PROVIDER;
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            locationProvider = LocationManager.GPS_PROVIDER;
            //Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
        }

    }

    public void OnLocation(Activity activity)
    {
        if(context==null)return;
        targetSdkVersion = 0;
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        boolean ret = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                ret = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            } else {
                ret = PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED;
            }
        } else {
            ret = true;
        }
        Log.d(TAG, "locationManager：" + ret );
        if (ret) {
            myLocationListener = new MyLocationListener();
            locationManager.requestLocationUpdates(locationProvider, 0, 0, myLocationListener);
        }
        else
        {
            ActivityCompat.requestPermissions(activity, PERMISSION_LOCATION,GET_LOCATION);
        }
    }

    private class MyLocationListener implements LocationListener {
        private String latLongString;

        //当定位位置改变的调用的方法
        //Location : 当前的位置
        @Override
        public void onLocationChanged(Location location) {
            float accuracy = location.getAccuracy();//获取精确位置
            double altitude = location.getAltitude();//获取海拔
            final double latitude = location.getLatitude();//获取纬度，平行
            final double longitude = location.getLongitude();//获取经度，垂直
            latLongString=latitude+"|"+longitude;
            Log.d(TAG, "locationService onLocationChanged: "+latLongString);
            UnityPlayer.UnitySendMessage("LocationManager", "LocResult", latLongString);
        }

        //当定位状态发生改变的时候调用的方式
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
            Log.d(TAG, "myservice status："+status);
        }

        //当定位可用的时候调用的方法
        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
            Log.d(TAG, "myservice onProviderEnabled");
        }

        //当定位不可用的时候调用的方法
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
            Log.d(TAG, "myservice onProviderDisabled");
        }
    }

}