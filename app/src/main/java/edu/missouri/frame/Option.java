package edu.missouri.frame;

import edu.missouri.geom.Point;

import static edu.missouri.drone.Drone.FOV_HEIGHT;
import static edu.missouri.drone.Drone.FOV_WIDTH;

public class Option {

    public static double cruiseAltitude ;
    public static GePoint startPoint ;
    public static Point[] vertices ;
    public static double overlap;
    public static double minCruiseAltitude = 10;
    public static double cruiseSpeed = 12;
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

    public void setParameters(double cruiseAltitude, GePoint startPoint, Point[] vertices, double overlap){
        this.cruiseAltitude = cruiseAltitude;
        this.startPoint = startPoint;
        this.vertices = vertices;
        this.overlap = overlap;
    }

    public static int distributor = Area.RANDOM;
    public static int numObjects = 1;
    public static double confidenceThreshold = 0.5;
    public static double energyBudget = 100000;
    public static double turningPower = 225;
    public static double angleSpeed = 2.1;

    public void setCruiseAltitude(double height){
        this.cruiseAltitude = height;
    }
}