package edu.missouri.frame;

import edu.missouri.geom.Point;

import static edu.missouri.drone.Drone.FOV_HEIGHT;
import static edu.missouri.drone.Drone.FOV_WIDTH;

public class Option {

    public static double cruiseAltitude ;
    public static Point startPoint ;
    public static GePoint GPSstartPoint ;
    public static Point endPoint ;
    public static Point[] vertices ;
    public static double overlap;
    public static int energyPercnetRemaining;
    public static int cruiseSpeed;
    public static int toandBackSpeed;
    public static double minCruiseAltitude = 10;
    public static double maxAltitude = 100;
    public static double tiltAngle = 55.0;
    public static double accelaration = 1.0;
    public static double decelaration = 1.0;
    public static double gratitudeAccelaration = 9.8;

    public static double bankedTurningRadias = Math.pow(cruiseSpeed,2)/(gratitudeAccelaration* Math.tan(tiltAngle));

    public static double defaultImageHeight() {
        return 2.0 * cruiseAltitude * Math.tan(FOV_HEIGHT / 2.0);
    }

    public static double defaultImageWidth() {
        return 2.0 * cruiseAltitude * Math.tan(FOV_WIDTH / 2.0);
    }

    public void setParameters(double cruiseAltitude, GePoint GPSstartPoint, Point startPoint, Point endPoint, Point[] vertices, int overlap, int energyPercnetRemaining, int cruiseSpeed, int toandBackSpeed){
        this.cruiseAltitude = cruiseAltitude;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.vertices = vertices;
        this.overlap = overlap/100.0;
        this.energyPercnetRemaining = energyPercnetRemaining;
        this.cruiseSpeed = cruiseSpeed;
        this.toandBackSpeed = toandBackSpeed;
        this.GPSstartPoint = GPSstartPoint;

    }

    public static int distributor = Area.RANDOM;
    public static int numObjects = 1;
    public static double confidenceThreshold = 0.5;
    public static double energyBudget = 100000;
    public static double turningPower = 225;
    public static double angleSpeed = 2.1;
}