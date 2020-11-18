package edu.missouri.drone.static_height;

import java.util.ArrayList;
import java.util.List;

import edu.missouri.frame.Area;
import edu.missouri.geom.Angle;
import edu.missouri.geom.Point;
import edu.missouri.geom.Polygon;
import edu.missouri.geom.Util;

import static edu.missouri.frame.Option.cruiseAltitude;

public class BoustrophedonDrone extends JiaoDrone {

    private boolean VERBOSE = false;


    public BoustrophedonDrone(Area area) {
        super(area);
    }

    @Override
    public List<Polygon> decompose(Polygon polygon, int depth) {
        List<Polygon> result = new ArrayList<>();


        for(Angle a: polygon.toAngles()) {
            if(depth <= 0) break;
            if(! a.isConcave()) continue;
            if(Util.within(a.a().x(), a.c().x(), a.b().x(), 1)) continue;
            if(Util.approx(a.a().x(), a.b().x(), 0.1)) continue;
            if(Util.approx(a.c().x(), a.b().x(), 0.1)) continue;

            Point splitPoint = polygon.above(a.b());
            if(splitPoint.equals(a.b())) {
                System.err.println("!!!");
                splitPoint = polygon.below(a.b());
            }

            if(polygon.top(a.b().x()).equals(polygon.bottom(a.b().x()))) continue;



            if(splitPoint == null) {
                System.err.println("Bad upsplit");
                splitPoint = polygon.bottom(a.b().x());
            }

            Polygon clone = polygon.getClone();
            clone.addPoint(splitPoint, polygon.indexOf(splitPoint)+1);

            Polygon[] subregions = clone.split(clone.indexOf(a.b()), clone.indexOf(splitPoint));

            result.addAll(decompose(subregions[0], depth-1));
            result.addAll(decompose(subregions[1], depth-1));
            return result;



        }
        // Nothing more to do!
        result.add(polygon);
        return result;
    }

    public void route() {
        whole = getPolygon();
        regions = decompose(whole, 400);
        List<Point> result = new ArrayList<>();
        List<List<Point>> missions = new ArrayList<>();

        for(Polygon p: regions) {
            if(VERBOSE) System.out.println(p);
            List<Point> subPlan = PlowDrone.planTheta(p, 0.0, false);
            if(subPlan != null && subPlan.size() > 0) {
                missions.add(subPlan);
            }
        }

        for(List<Point> mission: missions) result.addAll(mission);
        result = subdivide(result);
        setHeading(Math.PI/2); // this in a single line of code is my least favorite thing about this algorithm
        for(Point p: result) {
            moveTo(new Point(p.x(), p.y(), cruiseAltitude));
            scan();
        }
    }


    @Override
    public String toString() {return "Choset - Boustrophedon"; }
}
