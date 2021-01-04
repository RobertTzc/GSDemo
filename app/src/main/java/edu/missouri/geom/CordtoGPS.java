package edu.missouri.geom;

public class CordtoGPS {

    private double standardLatitude;//y
    private double standardLongitude;//x
    private double xDistance;
    private double yDistance;
    private double latitude;
    private double longitude;
    private double altitude;
    private int heading;
    private double curvesize;
    private boolean isTurning;

    public CordtoGPS(double standardLongitude, double standardLatitude, double xDistance, double yDistance, double altitude, int heading, double curvesize,boolean isTurning){
        this.standardLatitude = standardLatitude;
        this.standardLongitude = standardLongitude;
        this.xDistance = xDistance;
        this.yDistance = yDistance;
        this.altitude = altitude;
        this.heading = heading;
        this.curvesize = curvesize;
        this.isTurning = isTurning;
        latitude = standardLatitude - doLatDegress(yDistance);
        longitude = standardLongitude + doLngDegress(xDistance,standardLatitude);

    }


    private static Double doLngDegress(double distance, Double latitude) {
        double lngDegree = 2 * Math.asin(Math.sin((double)distance/12742000)/ Math.cos(latitude* Math.PI / 180));
        // 转换弧度
        lngDegree = lngDegree * (180/ Math.PI);
        return lngDegree;
    }

    private static Double doLatDegress(double distance) {
        double latDegrees = (double)distance/6371000;
        // 转换弧度
        latDegrees = latDegrees * (180/ Math.PI);
        return latDegrees;
    }


    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }
    public double getAltitude(){
        return altitude;
    }
    public int getHeading(){
        return heading;
    }
    public double getCurvesize(){
        return curvesize;
    }

    public boolean isTurning() {
        return isTurning;
    }
}
