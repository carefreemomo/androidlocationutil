package cn.lixiaoqian.LocationUtil;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class AudioService extends Service {
    private static final String CHANNEL_ID_STRING = "1002";
    public AudioHelper audioHelper;
    public LocationHelp locationHelp;
    private AudioServiceBinder mBinder = new AudioServiceBinder();
    private final static int GRAY_SERVICE_ID = 10002;
    NotificationManager notificationManager;
    String notificationId = "channelId1";
    String notificationName = "channelName1";
    private  Context context;
    private static final String TAG = "Unity";
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
    class AudioServiceBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    public void SetContext(Context ctt)
    {
        context = ctt;
    }

    private String locationProvider;
    private LocationManager locationManager;
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

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT <android.os.Build.VERSION_CODES.O) {
            startForeground(GRAY_SERVICE_ID, new Notification());
        }
        else if(Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.O)
        {
            startForegroundService();
        }
        acquireWakeLock();
        Log.d("Unity", "定时播放音乐");
        handler.postDelayed(runnable, 1000*10);//延时多长时间启动定时器
        return START_STICKY;
    }

    Handler  handler = new Handler();
    Runnable  runnable = new Runnable() {
        @Override
        public void run() {
            // handler自带方法实现定时器
            Log.d("Unity", "后台开始播放音乐");
            //OnPlayAudio(false,"Mapbackground.mp3");
            //OnPlayAudio(true,"http://cdn.lixiaoqian.com/gulangyu/audio/640894fb683511cfb486364dddd9620d.mp3");
            handler.postDelayed(this, 1000*10);//每隔3s执行
        }
    };

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
    public AudioService() {
        audioHelper = new AudioHelper();
        locationHelp = new LocationHelp();
    }
    public void OnInitAudio(String url) {
        audioHelper.InitUrl(url);
    }

    public void OnPlayAudio(boolean isNetWork,String name){
        try {
            if (!isNetWork) {
                audioHelper.Play(context,name);
            } else {
                audioHelper.PlayNetWork(name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void OnPauseAudio() {
        audioHelper.Pause();
    }

    public void OnStopAudio() {
        audioHelper.Stop();
    }

    public void OnLocation(Activity activity){
        locationHelp.InitData(context,locationManager,locationProvider,audioHelper);
        locationHelp.OnLocation(activity);
    }

    public void OnMinDistance(float minDis) {
        locationHelp.OnMinDistance(minDis);
    }

    public void SetPlay(double lng, double lat) {
        locationHelp.SetPlay(lng,lat);
    }

    public void OnVoiceLocationInfo(String voiceInfo) {
        locationHelp.OnVoiceLocationInfo(voiceInfo);
    }
}