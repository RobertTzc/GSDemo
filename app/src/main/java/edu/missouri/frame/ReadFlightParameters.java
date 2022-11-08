package edu.missouri.frame;

import com.dji.GSDemo.PathPlanning.DroneStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.missouri.drone.variable_height.ImprovedDirectDrone;
import edu.missouri.geom.CordtoGPS;
import edu.missouri.geom.Point;
import edu.missouri.geom.SortVertise;
import edu.missouri.geom.csvCreate;

public class ReadFlightParameters {
    public int plannedSpeed,prePlannedSpeed;
    public int overlap;
    public double height;
    public int energyPercnetRemaining;
    public List<GePoint> GPSvertices;
    public GePoint GPSstartPoint;
    public GePoint GPSendPoint;

    public List<GePoint> wayPoints;
    public List<Double> altitudes;
    public List<Boolean> isTurnings;
    public double horizonGap;
    public double verticalGap;
    public int energyPercentRemainingAfterPlan;
    public double reconmendSpeed;
    public boolean ifEnergyEnough;

    public Area area;
    public ImprovedDirectDrone drone;

    public ReadFlightParameters() {
    }
    public void UpdateBounds(List<GePoint> GPSvertices, double height, DroneStatus droneStatus)
    {
        this.GPSstartPoint = new GePoint(droneStatus.droneLatitude,droneStatus.droneLongtitude);
        this.GPSendPoint = new GePoint(droneStatus.homeLatitude,droneStatus.homeLongtitude);
        this.GPSvertices = GPSvertices;
        this.height = height;
        this.overlap = droneStatus.overlapRatio;
        this.energyPercnetRemaining = droneStatus.batteryPercentage;
        this.plannedSpeed = droneStatus.plannedSpeed;
        this.prePlannedSpeed = droneStatus.prePlannedSpeed;
        /***
         * need to be done:
         * planned speed is the speed for coverage
         * prePlannedSpeed is used to reach the first point of the coverage area, its preset as 15m/s could be modified, stored in dronestatus, should be counted in the energy model
         *
         */

        int verticesNum = GPSvertices.size();
//        List<GePoint> TMPGPSvertices;
//        try{
//            TMPGPSvertices = GPSvertices;
//        }catch (Exception e){
//            TMPGPSvertices = new SortVertise(GPSvertices).reverseVertices();
//        }
        List<GePoint> GPSverticesSorted = new SortVertise(GPSvertices).getCounterClockwiseVertices();
        Point[] verticesTmp = new Point[verticesNum];
        for (int i = 0; i < verticesNum; i++) {
            verticesTmp[i] = GPSToCord(GPSverticesSorted.get(i), GPSstartPoint);
        }
        Point startPoint =  new Point(GPSToCord(GPSstartPoint, GPSstartPoint),droneStatus.droneHeight);
        Point endPoint = new Point(GPSToCord(GPSendPoint, GPSstartPoint),0);
//        if (GPSstartPoint.latitude == GPSendPoint.latitude && GPSstartPoint.longtitude==GPSendPoint.longtitude){
//            endPoint = GPSToCord(GPSstartPoint, GPSstartPoint);
//        }

        new Option().setParameters(height,GPSstartPoint,startPoint,endPoint, verticesTmp, overlap,energyPercnetRemaining,plannedSpeed,prePlannedSpeed);
        planPath();
    }



    public void planPath() {
        area = Area.readPolygonFromCSV();
        drone = new ImprovedDirectDrone(area);
        Map<Point, Boolean> maps = drone.routes();
        double energyBudget = drone.TOTAL_ENERGY * (Option.energyPercnetRemaining)/100.0;//20% alarm
        double energyUse = drone.energyUsed(plannedSpeed);
        Iterator<Map.Entry<Point, Boolean>> entries = maps.entrySet().iterator();
        List<Point> waypoints = new ArrayList<>();
        List<Point> turningPoints = new ArrayList<>();
        while (entries.hasNext()) {
            Map.Entry<Point, Boolean> entry = entries.next();
            if (entry.getValue() == true) {
                turningPoints.add(entry.getKey());
            }
            waypoints.add(entry.getKey());
        }

        List<CordtoGPS> coordinates = new csvCreate(waypoints, turningPoints).getCoordinates();
        List heights = new ArrayList<Double>();
        List wayPoints = new ArrayList<Point>();
        List isTurnings = new ArrayList<Boolean>();
        // return parameters

        for (CordtoGPS coordinate : coordinates) {
            heights.add(coordinate.getAltitude());
            wayPoints.add(new GePoint(coordinate.getLatitude(), coordinate.getLongitude()));
            isTurnings.add(coordinate.isTurning());

        }
        if (energyUse>energyBudget){
            this.energyPercentRemainingAfterPlan = 0;
            this.ifEnergyEnough = false;
            this.reconmendSpeed = drone.getOptimalSpeed(plannedSpeed,energyBudget);
        }
        else {
            this.energyPercentRemainingAfterPlan = (int) Math.round(100*(0.01*Option.energyPercnetRemaining*drone.TOTAL_ENERGY-energyUse)/drone.TOTAL_ENERGY);
            this.ifEnergyEnough = true;
            this.reconmendSpeed = plannedSpeed;
        }
        this.altitudes = heights;
        this.isTurnings = isTurnings;
        this.wayPoints = wayPoints;
        this.horizonGap = (1-Option.overlap)*Option.defaultImageHeight();
        this.verticalGap = (1-Option.overlap)*Option.defaultImageWidth();
    }

    public Point GPSToCord(GePoint target, GePoint standard) {
        double[] tmpPoint = GPStoCord.getCord(target, standard);
        return new Point(tmpPoint[1], tmpPoint[0]);
    }

    public List<GePoint> getWaypoints() {
        return wayPoints;
    }
    public List<Double> getAltitudes() {
        return altitudes;
    }
    public List<Boolean> getIsTurning() {
        return isTurnings;
    }
    public int getEnergyPercentRemainingAfterPlan(){return energyPercentRemainingAfterPlan;}
    public double getReconmendSpeed() {return reconmendSpeed;}
    public boolean getIfEnergyEnough() {return ifEnergyEnough;}


    public static List<GePoint> splitPointString(String pointsString) {
        List<GePoint> GPSPoints = new ArrayList<GePoint>();
        String[] pointStringArr = pointsString.split(" ");
        for (String pointString : pointStringArr) {
            double latitude = Double.parseDouble(pointString.split(",")[0]);
            double longtitude = Double.parseDouble(pointString.split(",")[1]);
            GPSPoints.add(new GePoint(latitude, longtitude));
        }
        return GPSPoints;
    }


    public static void main(String[] args) {

        ArrayList<Double> latitude_list = new ArrayList<Double>(Arrays.asList(38.9129228409671, 38.9113696239793, 38.9113237082811, 38.9128514328361));
        ArrayList<Double> longtitude_list = new ArrayList<>(Arrays.asList(-92.2959491063508, -92.2960270901189, -92.2939188738332, -92.2940476198659));
        double height = 100;
        double overlap = 20;
        ArrayList<Double> startPoint = new ArrayList<>(Arrays.asList(38.9129228409671, -92.2959491063508));
        PathPlanning path = new PathPlanning(latitude_list, longtitude_list, startPoint, height, overlap);
        System.out.println(path.getWaypoints());
    }
}