package edu.missouri.frame;

public class GePoint {
    public double latitude; // 纬度坐标
    public double longtitude; // 经度坐标

    public GePoint(double latitude, double longtitude) {
        this.latitude = latitude;
        this.longtitude = longtitude;
    }
    public double getLatitude() {
        return 2 * latitude * Math.PI / 360 ;
    }
    public double getLongtitude() {
        return 2 * longtitude * Math.PI / 360;
    }

}