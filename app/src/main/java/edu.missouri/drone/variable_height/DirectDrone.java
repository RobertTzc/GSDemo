package edu.missouri.drone.variable_height;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.missouri.drone.Drone;
import edu.missouri.drone.static_height.JiaoDrone;
import edu.missouri.drone.static_height.PlowDrone;
import edu.missouri.frame.Area;
import edu.missouri.frame.Detectable;
import edu.missouri.frame.Option;
import edu.missouri.geom.Line;
import edu.missouri.geom.Point;
import edu.missouri.geom.Polygon;

public class DirectDrone extends Drone {


    Queue<Polygon> regions = new LinkedList<>();
    double thoroughness = 1.0;

    public DirectDrone(Area area) {
        super(area);
        regions.addAll(new JiaoDrone(area).decompose(getPolygon(), 500));
    }

    public void route() {
        List<Point> result = new ArrayList<>();
        List<Point> done = new ArrayList<>();

        // Assumption: We'll never take more than half the budget to go up and down checking
        // individual points.


        double overviewAltitude = Option.cruiseAltitude;
        setHeading(getPolygon().widthLine().measure() + Math.PI/2.0);

        Queue<Point> currentPoints = new LinkedList<>(Drone.subdivide(PlowDrone.plan(getPolygon(), getLocation())));

        while(! currentPoints.isEmpty()) {
            Point p = currentPoints.remove();
            p = new Point(p.x(), p.y(), overviewAltitude);
            moveTo(p);
            Capture c = scan();
            result.add(p);

            for(Detectable d: c.detectables) {
                if(done.contains(d)) continue;
                done.add(d);
                double k = altitudeNeeded(d);
                if(k >= overviewAltitude) continue;
                Point p2 = new Point(d.x(), d.y(), k);
                moveTo(p2);
                scan();
            }
        }
    }

    private double altitudeNeeded(Detectable d) {
        double k = d.detectedFrom() * Math.abs(d.confidence() - Option.confidenceThreshold) * (1 / thoroughness);
        return Math.max(k, 10);
    }

   // @Override
//    public void visualize(Graphics g) {

 //   }

    public void reroute(double energy) {
        thoroughness = 1.0;
        reroute();
//        while(energyUsed() > energy && Option.cruiseAltitude < Option.maxAltitude) {
//            Option.cruiseAltitude += 1;
//            thoroughness -= 1.0/Option.maxAltitude;
//            reroute();
//        }
    }

    public void reroute(Area area, double energyBudget) {
        thoroughness = 1.0;
        reroute(area);
//        double e = energyUsed();
//        while(e > energyBudget && Option.cruiseAltitude < Option.maxAltitude) {
//            Option.cruiseAltitude += 1;
//            thoroughness -= 1.0/Option.maxAltitude;
//            reroute(area);
//            e = energyUsed();
//        }
    }

    private double pathLength(List<Point> points) {
        double sum = 0;
        for(Line l: Line.arrayFromPoints(points.toArray(new Point[0]))) {
            sum += l.length();
        }
        return sum;
    }
}
