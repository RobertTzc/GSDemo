package edu.missouri.drone;

import java.util.List;

import edu.missouri.geom.Angle;
import edu.missouri.geom.Point;

public class WayPointCheck {
    private final List<Point> wayPoints;
    private final int index;

    public WayPointCheck(List<Point> wayPoints, int index){
        this.wayPoints = wayPoints;
        this.index = index;
    }

    public double getAngle(){
        Angle angle = new Angle(wayPoints.get(index+1),wayPoints.get(index),wayPoints.get(index-1));
        Double angleDouble = angle.measure();
        if(angle.measure()> Math.PI && angle.measure()- Math.PI>0.0001){
            angleDouble = angleDouble - Math.PI;
        }
        return angleDouble;
    }

    public boolean isATurn(){
        boolean result = false;
        if (index==0 || index==wayPoints.size()-1){
            return false;
        }
        if(isCloseTo(getAngle(), Math.PI)||isCloseTo(getAngle(),0.0)){
            result = false;
        }
        else {
            result = true;
        }
        return result;
    }


    public boolean isCloseTo(double a, double b){
        boolean result = false;
        if(Math.abs(b-a)<0.001){
            result = true;
        }
        return result;
    }



}
