package edu.missouri.drone.static_height;


import edu.missouri.frame.Area;
import edu.missouri.geom.Point;

@SuppressWarnings("unused")
public class DistanceTransformDrone extends PathTransformDrone {

    public DistanceTransformDrone(Area area) {
        super(area);
    }

    @Override
    protected int boundaryDistance(Point p) {
        return 0;
    }

    @Override
    public String toString() {
        return "Zelinsky - Distance Transform";
    }
}