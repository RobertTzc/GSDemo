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
import edu.missouri.geom.Point;
import edu.missouri.geom.Polygon;

@SuppressWarnings("unused")
public class LayerDrone extends Drone {


    Queue<Polygon> regions = new LinkedList<>();

    public LayerDrone(Area area) {
        super(area);
        regions.addAll(new JiaoDrone(area).decompose(getPolygon(), 500));
    }

    public void route() {
        List<Detectable> toDo = new ArrayList<>();
        List<Detectable> done = new ArrayList<>();

        // Assumption: We'll never take more than half the budget to go up and down checking
        // individual points.


        double overviewAltitude = Option.cruiseAltitude;
        setHeading(getPolygon().widthLine().measure() + Math.PI/2.0);

        Queue<Point> currentPoints = new LinkedList<>(Drone.subdivide(PlowDrone.plan(getPolygon())));

        while(! currentPoints.isEmpty()) {
            Point p = currentPoints.remove();
            p = new Point(p.x(), p.y(), overviewAltitude);
            moveTo(p);
            toDo.addAll(scan().detectables);
        }
        for(Detectable d: toDo) {
            if(done.contains(d)) continue;
            done.add(d);
            Point p2 = new Point(d.x(), d.y(), 10);
            moveTo(p2);
            scan();
        }
    }

 //   @Override
  //  public void visualize(Graphics g) {

   // }
}
