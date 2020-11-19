package cn.lixiaoqian.Util;
import java.util.List;

class VoiceLocation {
    public int id;
    public int type;
    public String gps ;
    public Boolean is_explore;
    public Boolean is_once;
    public List<String> audios;
}

enum ArrivePoiType {
    PlacePoi,//景点
    LineSite,//站点
    RouteServicePoi,//路线服务点
    ClockPoi,//提示
    RecommendPoi,//推荐
    ARExplorePoi,//AR点
}