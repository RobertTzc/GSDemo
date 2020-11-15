package com.dji.GSDemo.PathPlanning;

public class DroneStatus {
        int batteryPercentage = 100;//[0,100] in percentage
        double droneLatitude=0;
        double droneLongtitude=0;
        float droneHeading = 0;
        float droneHeight=0;// in meters, note this is current height, not the path planning height
        double droneSpeed=0; //in meter/seconds, note this is current speed, not the path planning speed
        int plannedSpeed=0; //in meter/seconds, this is the speed set for path coverage
        float overlapRatio=0; //[0,100] in percentage
        int satelliteCount=0;

}
