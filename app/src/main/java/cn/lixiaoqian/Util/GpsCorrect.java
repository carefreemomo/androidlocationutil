package cn.lixiaoqian.Util;

import java.util.ArrayList;
import java.util.List;

public class GpsCorrect {
    final static double pi = 3.14159265358979324;
    final static double a = 6378245.0;
    final static double ee = 0.00669342162296594323;

    //region  GCJ-02 to WGS-84
    public static LatLonPoint ToGPSPoint(double latitude, double longitude) {
        LatLonPoint dev = GCJ02_to_WGS84(latitude, longitude);
        return dev;
    }

    // 计算偏差
    private static LatLonPoint WGS84_to_GCJ02(double wgLat, double wgLon) {
        if (isOutOfChina(wgLat, wgLon)) {
            return new LatLonPoint(0, 0);
        }
        double dLat = calLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = calLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        return new LatLonPoint(dLat+wgLat, dLon+wgLon);
    }

    public static LatLonPoint GCJ02_to_WGS84(double gcjLat, double gcjLng)
    {
        LatLonPoint dev = WGS84_to_GCJ02(gcjLat, gcjLng);
        double wgsLng = gcjLng * 2 - dev.getLongitude();
        double wgsLat = gcjLat * 2 - dev.getLatitude();
        dev.lat=wgsLat;
        dev.lng=wgsLng;
        return  dev;
    }

    // 判断坐标是否在国外
    private static boolean isOutOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    // 计算纬度
    private static double calLat(double x, double y)
    {
        double resultLat = -100.0f + 2.0f * x + 3.0f * y + 0.2f * y * y + 0.1f * x * y
                + 0.2f * Math.sqrt(Math.abs(x));
        resultLat += (20.0f * Math.sin(6.0f * x * pi) + 20.0f * Math.sin(2.0f * x * pi)) * 2.0f / 3.0f;
        resultLat += (20.0f * Math.sin(y * pi) + 40.0f * Math.sin(y / 3.0f * pi)) * 2.0f / 3.0f;
        resultLat += (160.0f * Math.sin(y / 12.0f * pi) + 320f * Math.sin(y * pi / 30.0f)) * 2.0f / 3.0f;
        return resultLat;
    }

    // 计算经度
    private static double calLon(double x, double y)
    {
        double resultLon = 300.0f + x + 2.0f * y + 0.1f * x * x + 0.1f * x * y + 0.1f
                * Math.sqrt(Math.abs(x));
        resultLon += (20.0f * Math.sin(6.0f * x * pi) + 20.0f * Math.sin(2.0f * x * pi)) * 2.0f / 3.0f;
        resultLon += (20.0f * Math.sin(x * pi) + 40.0f * Math.sin(x / 3.0f * pi)) * 2.0f / 3.0f;
        resultLon += (150.0f * Math.sin(x / 12.0f * pi) + 300.0f * Math.sin(x / 30.0f
                * pi)) * 2.0f / 3.0f;
        return resultLon;
    }
    //endregion
}