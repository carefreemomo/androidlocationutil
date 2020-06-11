package cn.lixiaoqian.LocationUtil;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
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
import java.io.IOException;
import java.util.List;
import android.os.Bundle;

import com.hiyorin.permission.PermissionPlugin;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class LocActivity extends UnityPlayerActivity {

    private Context context;
    private static final String TAG = "Unity";
    private static final int GET_CAMERA = 1010;
    private static String[] PERMISSION_CAMERA = {
            Manifest.permission.CAMERA
    };
    private static final int GET_LOCATION = 1011;
    private static String[] PERMISSION_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:LocationUtil ");
        context = this;

        int permission = ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSION_CAMERA,
                    GET_CAMERA);
        }
        startAudioService();
        bindAudioService();
        startLocationService();
        bindLocationService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        Log.d("unity", "-----------------onRequestPermissionsResult -----------------------");
        PermissionPlugin.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_LOCATION) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    locationService.OnLocation(this);
                } else {
                }
            }
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
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn=false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            isScreenOn = powerManager.isInteractive();
        } else {
            isScreenOn = powerManager.isScreenOn();
        }
        if (!isScreenOn) {
            Activity unityActivity = UnityPlayer.currentActivity;
            unityActivity.moveTaskToBack(false);
            mUnityPlayer.resume();
            mUnityPlayer.windowFocusChanged(true);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    //region ###定位后台服务
    private LocationService.LocationServiceBinder locationServiceBinder;
    private LocationService locationService;
    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务与活动成功绑定
            Log.d(TAG, "服务与活动成功绑定");
            locationServiceBinder = (LocationService.LocationServiceBinder) iBinder;
            locationService = locationServiceBinder.getService();
            locationService.SetContext(context);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //服务与活动断开
            Log.d(TAG, "服务与活动成功断开");
        }
    };
    /**
     * 绑定服务
     */
    private void bindLocationService() {
        Log.d(TAG, "定位后台服务");
        Intent bindIntent = new Intent(context, LocationService.class);
        bindService(bindIntent, locationServiceConnection, BIND_AUTO_CREATE);
    }
    /**
     * 启动服务
     */
    private void startLocationService() {
        Intent intent = new Intent(context, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android8.0以上通过startForegroundService启动service
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    public void OnLocation()
    {
        locationService.OnLocation(this);
    }
    //endregion


    //region ###音乐后台服务
    private AudioService.AudioServiceBinder audioServiceBinder;
    private AudioService AudioService;
    private ServiceConnection audioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务与活动成功绑定
            Log.d(TAG, "服务与活动成功绑定");
            audioServiceBinder = (AudioService.AudioServiceBinder) iBinder;
            AudioService = audioServiceBinder.getService();
            AudioService.SetContext(context);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //服务与活动断开
            Log.d(TAG, "服务与活动成功断开");
        }
    };
    /**
     * 绑定服务
     */
    private void bindAudioService() {
        Log.d(TAG, "音乐后台服务");
        Intent bindIntent = new Intent(context, AudioService.class);
        bindService(bindIntent, audioServiceConnection, BIND_AUTO_CREATE);
    }
    /**
     * 启动服务
     */
    private void startAudioService() {
        Intent intent = new Intent(context, AudioService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android8.0以上通过startForegroundService启动service
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
    public void OnPlayAudio(boolean isNetWork,String name) {
        Log.d(TAG, "播放");
        AudioService.OnPlayAudio(isNetWork, name);
    }

    public void OnPauseAudio() {
        Log.d(TAG, "暂停");
        AudioService.OnPauseAudio();
    }

    public void OnStopAudio() {
        Log.d(TAG, "结束");
        AudioService.OnStopAudio();
    }
    //endregion
}
