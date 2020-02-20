package cn.lixiaoqian.LocationUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.util.List;


public class MyJobService extends JobIntentService {
    private static Context mContext;
    private static MyJobService.MyLocationListener  myLocationListener;
    private static final String TAG = "Unity";

    private static final int JOB_ID = 1000;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "MyJobService onCreate");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
    }

    public MyJobService() {
    }

    public static void enqueueWork(Context context, Intent work) {

        enqueueWork(context, MyJobService.class, JOB_ID, work);
        mContext=context;
        myLocationListener = new MyLocationListener();
        InitLocation();

    }
    public static void InitLocation()
    {
        Context context = mContext;
        Log.d(TAG, "OnLocation:LocationUtil ");
        //1.获取位置的管理者
        LocationManager  locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        //2.获取定位方式
        //2.1获取所有的定位方式，true:表示返回所有可用定位方式
        List<String> providers = locationManager.getProviders(true);
        String  locationProvider = LocationManager.GPS_PROVIDER;
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
        int  targetSdkVersion = 0;
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
        if (ret) {
            locationManager.requestLocationUpdates(locationProvider, 0, 0, myLocationListener);
        }
    }


    private static class MyLocationListener implements LocationListener {
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
            Log.d(TAG, "onLocationChanged: "+latLongString);
            UnityPlayer.UnitySendMessage("LocationManager", "LocResult", latLongString);
        }

        //当定位状态发生改变的时候调用的方式
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        //当定位可用的时候调用的方法
        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        //当定位不可用的时候调用的方法
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "MyService onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}