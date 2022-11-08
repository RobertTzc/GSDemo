package com.dji.GSDemo.PathPlanning;

import dji.common.mission.waypoint.WaypointMissionState;

public class DroneStatus {
        public int batteryPercentage = 100;//[0,100] in percentage
        public int batteryPrecentageRemian = 100;

        public double droneLatitude=0;
        public double droneLongtitude=0;
        public double homeLatitude = 0;
        public double homeLongtitude = 0;

        public float droneHeading = 0;
        public float droneHeight=0;// in meters, note this is current height, not the path planning height
        public double droneSpeed=0; //in meter/seconds, note this is current speed, not the path planning speed
        public double droneVerticalSpeed = 0;
        public int plannedSpeed=0; //in meter/seconds, this is the speed set for path coverage
        public int overlapRatio=70; //[0,100] in percentage
        public int prePlannedSpeed = 15;

        public float cameraFOV = 66.0f;
        public int satelliteCount=0;

        public String cameraShutter=null;
        public String cameraAperture=null;
        public String cameraISO=null;
        public String cameraExposureCompensation=null;
        public String cameraExposureMode=null;
        public String cameraWhiteBalance = null;

        public WaypointMissionState droneMissionState = null;
        public boolean isFly = false;
        public int batteryCurrent = 0;
        public int batteryVoltage = 0;

        public float storage = 0.0f;




}
