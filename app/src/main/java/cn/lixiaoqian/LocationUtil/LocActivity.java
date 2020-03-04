package cn.lixiaoqian.LocationUtil;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import android.os.Bundle;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class LocActivity extends UnityPlayerActivity {

    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    private String locationProvider;
    private Context context;
    private int targetSdkVersion;
    private static final String TAG = "Unity";
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:LocationUtil ");
        context = this;
        //1.获取位置的管理者
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
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
        targetSdkVersion = 0;
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void  OnLocation()
    {
        if(context==null)return;
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
            myLocationListener = new MyLocationListener();
            locationManager.requestLocationUpdates(locationProvider, 0, 0, myLocationListener);
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
            Log.d(TAG, "myservice onLocationChanged: "+latLongString);
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

    public void OnFullscreen(String enable) {
        Log.i(TAG, "OnFullscreen: ()"+enable);
        if (enable=="1") { //显示状态栏
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else { //隐藏状态栏
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(lp);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        boolean isScreenOn=false;
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//            isScreenOn = powerManager.isInteractive();
//        } else {
//            isScreenOn = powerManager.isScreenOn();
//        }
//        if (!isScreenOn) {
//            Activity unityActivity = UnityPlayer.currentActivity;
//            unityActivity.moveTaskToBack(false);
//            mUnityPlayer.resume();
//            mUnityPlayer.windowFocusChanged(true);
//        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        ReleaseWakeLock();
    }
}
