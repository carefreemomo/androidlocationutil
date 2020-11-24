package cn.lixiaoqian.Util;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unity3d.player.UnityPlayer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class LocationHelp extends Activity {

    private String locationProvider;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    private AudioHelper audioHelper;
    private Context context;
    private int targetSdkVersion;
    private boolean isAutoAudio=true;
    private static final String TAG = "Unity";
    private static final int GET_LOCATION = 1011;
    private static String[] PERMISSION_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private List<String> PlayVoiceList = new LinkedList<String>();
    private List<VoiceLocation> VoiceLocationList = new LinkedList<VoiceLocation>();
    private List<VoiceLocation> HadVoiceLocationList = new LinkedList<VoiceLocation>();
    private float minArriveDistance = 15;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void InitData(Context context, LocationManager locationManager, String locationProvider,AudioHelper audioHelper) {
        this.context = context;
        this.locationManager = locationManager;
        this.locationProvider = locationProvider;
        this.audioHelper =  audioHelper;
        this.audioHelper.setListener(new AudioHelper.onListener() {
            @Override
            public void OnStartListener() {
                UnityPlayer.UnitySendMessage("LocationManager", "StartResult", "");
        }
            @Override
            public void OnEndListener() {
                UnityPlayer.UnitySendMessage("LocationManager", "EndResult", "");
                if (PlayVoiceList.size() > 0) {
                    PlayVoiceList.remove(0);
                }
                PlayVoice();
            }

            @Override
            public void OnProgress(float progress) {

            }
        });
    }

    public void OnLocation(Activity activity) {
        if (context == null) return;
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
        //Log.d(TAG, "locationManager：" + ret);
        if (ret) {
            myLocationListener = new MyLocationListener();
            locationManager.requestLocationUpdates(locationProvider, 0, 0, myLocationListener);
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSION_LOCATION, GET_LOCATION);
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
            latLongString = latitude + "|" + longitude;
            //Log.d(TAG, "locationService onLocationChanged: " + latLongString);
            UnityPlayer.UnitySendMessage("LocationManager", "LocResult", latLongString);
            SetPlay(longitude, latitude);
        }

        //当定位状态发生改变的时候调用的方式
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
            Log.d(TAG, "myservice status：" + status);
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

    public void OnMinDistance(float minDistance)
    {
        this.minArriveDistance = minDistance;
    }

    public void OnVoiceLocationInfo(String voiceInfo) {
        Gson gson = new Gson();
        List<VoiceLocation> list = gson.fromJson(voiceInfo, new TypeToken<List<VoiceLocation>>() {
        }.getType());
        this.VoiceLocationList = list;
    }

    VoiceLocation befoerVoiceLocation = null;

    private static double EARTH_RADIUS = 6378.137f;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    //gps距离计算
    public static double GetDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10;
        return s;
    }

    public void SetPlay(Double longitude,Double latitude ) {
//        Log.d(TAG, "VoiceLocationList1: "+VoiceLocationList.size());
        if (!isAutoAudio) {
            return;
        }
        if (VoiceLocationList != null && !VoiceLocationList.isEmpty()) {
            double minDis = 1000000;
            VoiceLocation curVoiceLocation = null;
            for (int i = 0; i < VoiceLocationList.size(); i++) {
                VoiceLocation voiceLocation = VoiceLocationList.get(i);
//                Log.d(TAG, "VoiceLocationList3: " + voiceLocation.gps);
                double log = Double.parseDouble(voiceLocation.gps.split(",")[0]);
                double lat = Double.parseDouble(voiceLocation.gps.split(",")[1]);
                double dis = GetDistance(latitude, longitude, lat, log);
                if (minDis > dis) {
                    minDis = dis;
                    curVoiceLocation = voiceLocation;
                }
            }
            if (befoerVoiceLocation != null && curVoiceLocation != null
                    && befoerVoiceLocation.type == curVoiceLocation.type
                    && befoerVoiceLocation.id == curVoiceLocation.id) {
                return;
            }
            if(curVoiceLocation==null)return;
//            Log.d(TAG, "curVoiceLocation: "+curVoiceLocation.gps);
//            Log.d(TAG, "latitude: "+latitude+"|longitude"+longitude);
            double log = Double.parseDouble(curVoiceLocation.gps.split(",")[0]);
            double lat = Double.parseDouble(curVoiceLocation.gps.split(",")[1]);
            double dis = GetDistance(latitude, longitude, lat, log);
//            Log.d(TAG, "dis: "+dis);
            if (dis < this.minArriveDistance) {
                if (!HadVoiceLocationList.contains(curVoiceLocation)) {
                    if (curVoiceLocation.is_once) {
                        HadVoiceLocationList.add(curVoiceLocation);
                    }
                }
                else
                {
                    return;
                }
                befoerVoiceLocation = curVoiceLocation;
//                Log.d(TAG, "curVoiceLocation2: "+curVoiceLocation.audios);
                SetStop();
                PlayVoiceList.addAll(curVoiceLocation.audios);
                PlayVoice();
            }
        }
    }

    public void SetPause()
    {
        if (PlayVoiceList.size() > 0) {
            audioHelper.Pause();
        }
    }

    public void SetNext()
    {
        if (PlayVoiceList.size() > 1) {
            SetPause();
            PlayVoiceList.remove(0);
            PlayVoice();
        }
        else if(PlayVoiceList.size() > 0)
        {
            SetStop();
        }
    }

    public void SetContinue()
    {
        if(PlayVoiceList.size() > 0)
        {
            audioHelper.Continue();
        }
    }

    public void SetStop()
    {
        audioHelper.Stop();
        PlayVoiceList.clear();
    }

    public void PlayVoice() {
//        Log.d(TAG, "PlayVoiceLength: "+PlayVoiceList.size());
        try {
            if (PlayVoiceList.size() > 0) {
//                Log.d(TAG, "PlayVoice: "+PlayVoiceList.get(0));
                audioHelper.PlayNetWork(0,PlayVoiceList.get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SetOn(boolean isOn)
    {
        isAutoAudio=isOn;
    }

    public void Reset()
    {
        PlayVoiceList.clear();
    }
}