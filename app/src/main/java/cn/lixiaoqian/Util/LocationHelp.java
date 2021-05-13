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

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;

public class LocationHelp extends Activity {

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    private String locationProvider;
    private LocationManager locationManager;
//    private MyLocationListener myLocationListener;
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
        InitLocation();
        this.audioHelper.setListener(new AudioHelper.onListener() {
            @Override
            public void OnStartListener(String url) {
                UnityPlayer.UnitySendMessage("LocationManager", "StartResult", url);
        }
            @Override
            public void OnEndListener(String url) {
                UnityPlayer.UnitySendMessage("LocationManager", "EndResult", url);
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



    //region 定位

    /**
     * 初始化定位
     */
    private void InitLocation(){
//        Log.d(TAG, "InitLocation: ");
        //初始化client
        locationClient = new AMapLocationClient(this.context);
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        private String latLongString;
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                if(location.getErrorCode() == 0) {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
//                  Log.d(TAG, "locationListener longitude: "+longitude);
//                  Log.d(TAG, "locationListener latitude: "+latitude);
                    LatLonPoint latLonPoint = GpsCorrect.ToGPSPoint(latitude, longitude);
                    latLongString = latLonPoint.getLatitude() + "|" + latLonPoint.getLongitude();
//                  Log.d(TAG, "locationService getgps: " + latLongString);
                    //latLongString = GpsCorrect.GetGpsCorrect(latitude,longitude);
                    //Log.d(TAG, "locationService getgpscorrect: " + latLongString);
                    UnityPlayer.UnitySendMessage("LocationManager", "LocResult", latLongString);
                    SetPlay(latLonPoint.getLongitude(), latLonPoint.getLatitude());
                }
            }
//                StringBuffer sb = new StringBuffer();
//                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
//                if(location.getErrorCode() == 0){
//                    sb.append("定位成功" + "\n");
//                    sb.append("定位类型: " + location.getLocationType() + "\n");
//                    sb.append("经    度    : " + location.getLongitude() + "\n");
//                    sb.append("纬    度    : " + location.getLatitude() + "\n");
//                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
//                    sb.append("提供者    : " + location.getProvider() + "\n");
//
//                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
//                    sb.append("角    度    : " + location.getBearing() + "\n");
//                    // 获取当前提供定位服务的卫星个数
//                    sb.append("星    数    : " + location.getSatellites() + "\n");
//                    sb.append("国    家    : " + location.getCountry() + "\n");
//                    sb.append("省            : " + location.getProvince() + "\n");
//                    sb.append("市            : " + location.getCity() + "\n");
//                    sb.append("城市编码 : " + location.getCityCode() + "\n");
//                    sb.append("区            : " + location.getDistrict() + "\n");
//                    sb.append("区域 码   : " + location.getAdCode() + "\n");
//                    sb.append("地    址    : " + location.getAddress() + "\n");
//                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
//                } else {
//                    //定位失败
//                    sb.append("定位失败" + "\n");
//                    sb.append("错误码:" + location.getErrorCode() + "\n");
//                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
//                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
//                }
//                sb.append("***定位质量报告***").append("\n");
//                sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启":"关闭").append("\n");
//                sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
//                sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
//                sb.append("* 网络类型：" + location.getLocationQualityReport().getNetworkType()).append("\n");
//                sb.append("* 网络耗时：" + location.getLocationQualityReport().getNetUseTime()).append("\n");
//                sb.append("****************").append("\n");
//            } else {
////                tvResult.setText("定位失败，loc is null");
//            }
        }
    };


    /**
     * 获取GPS状态的字符串
     * @param statusCode GPS状态码
     * @return
     */
    private String getGPSStatusString(int statusCode){
        String str = "";
        switch (statusCode){
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
        }
//        Log.d(TAG, "getGPSStatusString: "+str);
        return str;
    }

    private void StartLocation(){
//        Log.d(TAG, "StartLocation: ");
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    private void StopLocation(){
        // 停止定位
        locationClient.stopLocation();
    }

    private void DestroyLocation(){
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
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
              StartLocation();
//            myLocationListener = new MyLocationListener();
//            locationManager.requestLocationUpdates(locationProvider, 0, 0, myLocationListener);
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSION_LOCATION, GET_LOCATION);
        }
    }

//    private class MyLocationListener implements LocationListener {
//        private String latLongString;
//
//        //当定位位置改变的调用的方法
//        //Location : 当前的位置
//        @Override
//        public void onLocationChanged(Location location) {
//            float accuracy = location.getAccuracy();//获取精确位置
//            double altitude = location.getAltitude();//获取海拔
//            final double latitude = location.getLatitude();//获取纬度，平行
//            final double longitude = location.getLongitude();//获取经度，垂直
//            latLongString = latitude + "|" + longitude;
//            Log.d(TAG, "locationService getgps: " + latLongString);
//            //latLongString = GpsCorrect.GetGpsCorrect(latitude,longitude);
//            Log.d(TAG, "locationService getgpscorrect: " + latLongString);
//            UnityPlayer.UnitySendMessage("LocationManager", "LocResult", latLongString);
//            SetPlay(longitude, latitude);
//        }
//
//        //当定位状态发生改变的时候调用的方式
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            // TODO Auto-generated method stub
//            Log.d(TAG, "myservice status：" + status);
//        }
//
//        //当定位可用的时候调用的方法
//        @Override
//        public void onProviderEnabled(String provider) {
//            // TODO Auto-generated method stub
//            Log.d(TAG, "myservice onProviderEnabled");
//        }
//
//        //当定位不可用的时候调用的方法
//        @Override
//        public void onProviderDisabled(String provider) {
//            // TODO Auto-generated method stub
//            Log.d(TAG, "myservice onProviderDisabled");
//        }
//    }

    //endregion
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
//                if(voiceLocation.gps=="118.0547180176,24.4478721619")
//                {
//                    Log.d(TAG, "dis222: " + dis);
//                    Log.d(TAG, "minDis222: " + minDis);
//                }
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
        Log.d(TAG, "SetOn: "+isAutoAudio);
    }

    public void Reset()
    {
        PlayVoiceList.clear();
    }
}