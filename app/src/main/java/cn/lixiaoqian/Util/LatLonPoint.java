package cn.lixiaoqian.Util;

public class LatLonPoint {
    double lat;
    double lng;

    public LatLonPoint(double retLat, double retLon) {
        lat=retLat;
        lng=retLon;
    }

    public double getLatitude() {
        return  lat;
    }

    public double getLongitude() {
        return  lng;
    }

}
