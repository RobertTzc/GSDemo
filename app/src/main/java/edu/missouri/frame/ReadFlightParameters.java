package edu.missouri.frame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.missouri.drone.Drone;
import edu.missouri.drone.variable_height.ImprovedDirectDrone;
import edu.missouri.geom.CordtoGPS;
import edu.missouri.geom.Point;
import edu.missouri.geom.SortVertise;
import edu.missouri.geom.csvCreate;

public class ReadFlightParameters {


    public List<GePoint> wayPoints;
    public List<Double> altitudes;
    public List<Boolean> isTurnings;

    public Area area;
    public Drone drone;

    public ReadFlightParameters() {
    }

    public void UpdateBounds(List<GePoint> GPSvertices, GePoint GPSstartPoint, double height, double overlap) {

        int verticesNum = GPSvertices.size();
        List<GePoint> GPSverticesSorted = new SortVertise(GPSvertices).getCounterClockwiseVertices();
        Point[] verticesTmp = new Point[verticesNum];
        for (int i = 0; i < verticesNum; i++) {
            List<Point> points = new ArrayList<Point>();
            verticesTmp[i] = GPSToCord(GPSverticesSorted.get(i), GPSstartPoint);
        }
        new Option().setParameters(height, GPSstartPoint, verticesTmp, overlap);
        planPath();
    }


    public void planPath() {
        area = Area.readPolygonFromCSV();
        drone = new ImprovedDirectDrone(area);
        Map<Point, Boolean> maps = ((ImprovedDirectDrone) drone).routes();
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
        this.altitudes = heights;
        this.isTurnings = isTurnings;
        this.wayPoints = wayPoints;

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
