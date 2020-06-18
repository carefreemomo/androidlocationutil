package cn.lixiaoqian.Util;

class VoiceLocation {
    public int id;

    public String name;

    public ArrivePoiType type ;

    public String pos_in_unity_map;

    public String commentary_audio;

    public String arrive_audio;

    public String next_audio;

    public boolean is_explore ;

    public int explore_id;

    public String content;
}

enum ArrivePoiType {
    PlacePoi,//景点
    LineSite,//站点
    RouteServicePoi,//路线服务点
    ClockPoi,//提示
    RecommendPoi,//推荐
    ARExplorePoi,//AR点
}
