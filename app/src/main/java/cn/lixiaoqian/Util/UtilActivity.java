package cn.lixiaoqian.Util;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hiyorin.permission.PermissionPlugin;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class UtilActivity extends UnityPlayerActivity {

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
        context = this;
        int permission = ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSION_CAMERA,
                    GET_CAMERA);
        }
        startAudioService();
        bindAudioService();
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
                    utilService.OnLocation(this);
                } else {
                }
            }
        }
    }

    public void OnFullscreen(final String enable) {
        Log.i(TAG, "OnFullscreen: ()"+enable);
        mUnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enable == "1") {
                    //全屏
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                } else {
                    // 非全屏
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
            }
        });
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



    //region ###音乐后台服务
    private UtilService.UtilServiceBinder utilServiceBinder;
    private UtilService utilService;
    private ServiceConnection utilServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务与活动成功绑定
            Log.d(TAG, "服务与活动成功绑定");
            utilServiceBinder = (UtilService.UtilServiceBinder) iBinder;
            utilService = utilServiceBinder.getService();
            utilService.SetContext(context);
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
        Intent bindIntent = new Intent(context, UtilService.class);
        bindService(bindIntent, utilServiceConnection, BIND_AUTO_CREATE);
    }
    /**
     * 启动服务
     */
    private void startAudioService() {
        Intent intent = new Intent(context, UtilService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android8.0以上通过startForegroundService启动service
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }


    //endregion

    //region 音频方法
    public void OnInitAudio(String url) {
        Log.d(TAG, "播放");
        utilService.OnInitAudio(url);
    }

    public void OnPlayAudio(boolean isNetWork,String name) {
        Log.d(TAG, "播放");
        utilService.OnPlayAudio(isNetWork, name);
    }

    public void OnPauseAudio() {
        Log.d(TAG, "暂停");
        utilService.OnPauseAudio();
    }

    public void OnStopAudio() {
        Log.d(TAG, "结束");
        utilService.OnStopAudio();
    }

    public void OnContinueAudio() {
        Log.d(TAG, "继续");
        utilService.OnContinueAudio();
    }

    public void SetVolume(float value)
    {
        utilService.SetVolume(value);
    }

    public int GetPlayStatus(String url)
    {
        return utilService.GetPlayStatus(url);
    }
    //endregion

    //region 定位方法
    //定位
    public void OnLocation()
    {
        utilService.OnLocation(this);
    }

    //检测距离
    public void OnMinDistance(float minDis)
    {
        utilService.OnMinDistance(minDis);
    }

    //音频触发列表
    public void OnVoiceLocationInfo(String voiceInfo) { utilService.OnVoiceLocationInfo(voiceInfo); }

    public void SetPlay(double lng,double lat)
    {
        utilService.SetPlay(lng,lat);
    }

    public void SetPause()
    {
        utilService.SetPause();
    }

    public void SetContinue()
    {
        utilService.SetContinue();
    }

    public void SetNext()
    {
        utilService.SetNext();
    }

    public void SetStop()
    {
        utilService.SetStop();
    }

    public void SetOn(boolean isOn)
    {
        utilService.SetOn(isOn);
    }

    public void Reset()
    {
        utilService.Reset();
    }
    //endregion
}
