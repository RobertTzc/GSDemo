package edu.missouri.drone.static_height;

import java.util.ArrayList;
import java.util.List;

import edu.missouri.drone.Drone;
import edu.missouri.frame.Area;
import edu.missouri.frame.Option;
import edu.missouri.geom.Angle;
import edu.missouri.geom.Line;
import edu.missouri.geom.Point;
import edu.missouri.geom.Polygon;
import edu.missouri.geom.Util;

import static edu.missouri.frame.Option.cruiseAltitude;

public class JiaoDrone extends Drone {

    List<Polygon> regions;
    Polygon whole;
    private static final boolean VERBOSE = false;


    public JiaoDrone(Area area) {
        super(area);
    }

    public void preplan() {
        List<Polygon> clone = new ArrayList<>(regions);
        regions = reorder(clone, clone.remove(0)).regions;
    }

    private long lastStartTime;
    private long maxTime;

    public void route() {
        whole = getPolygon();
        lastStartTime = System.nanoTime();
        maxTime = 2 * 1000 * 1000 * 1000;
        regions = decompose(whole, 400);
        preplan();

        Point start;
        if(regions.size() < 2) start = getPolygon().rightmost();
        else start = regions.get(1).center();

        Polygon first = regions.get(0);

        Line widthLine = first.widthLine();
        double theta1 = -widthLine.a().bearing(widthLine.b());
        double theta2 = -widthLine.b().bearing(widthLine.a());

        List<Point> option1 = null;
        while(option1 == null || option1.isEmpty()) option1 = PlowDrone.planTheta(first, theta1, false);
        double score1 = option1.get(option1.size()-1).distance(start);
        List<Point> option2 =  PlowDrone.planTheta(first, theta1, true);
        double score2 = option2.get(option2.size()-1).distance(start);
        List<Point> option3 =  PlowDrone.planTheta(first, theta2, false);
        double score3 = option3.get(option3.size()-1).distance(start);
        List<Point> option4 =  PlowDrone.planTheta(first, theta2, true);
        double score4 = option4.get(option4.size()-1).distance(start);

        double min = Util.min(score1, score2, score3, score4);
        List<Point> choice;
        if(min == score1) choice = option1;
        else if(min == score2) choice = option2;
        else if(min == score3) choice = option3;
        else if(min == score4) choice = option4;
        else throw new ArithmeticException();

        List<Point> result = new ArrayList<>(choice);

        start = result.get(result.size()-1);
        for(Polygon p: regions) {
            if(p == regions.get(0)) continue;
            if(VERBOSE) System.out.println(p);

            widthLine = p.widthLine();
            theta1 = -widthLine.a().bearing(widthLine.b());
            theta2 = -widthLine.b().bearing(widthLine.a());

            option1 = PlowDrone.planTheta(p, theta1, false);
            if(option1.isEmpty()) continue;
            score1  = option1.get(0).distance(start);
            option2 = PlowDrone.planTheta(p, theta1, true);
            score2  = option2.get(0).distance(start);
            option3 = PlowDrone.planTheta(p, theta2, false);
            score3  = option3.get(0).distance(start);
            option4 = PlowDrone.planTheta(p, theta2, true);
            score4  = option4.get(0).distance(start);

            min = Util.min(score1, score2, score3, score4);
                 if(min == score1) choice = option1;
            else if(min == score2) choice = option2;
            else if(min == score3) choice = option3;
            else if(min == score4) choice = option4;
            else throw new ArithmeticException();

            result.addAll(choice);
            start = result.get(result.size()-1);
        }

        for(Point p: subdivide(result)) {
            Point q = new Point(p.x(), p.y(), cruiseAltitude);
            moveTo(q);
            scan();
        }
    }

    public List<Polygon> decompose(Polygon polygon, int depth) {

        int n = polygon.numSides();

        Angle[] angles = polygon.toAngles();
        Point[] points = polygon.toPoints();
        Polygon[] bestSubregions = null;

        if(isAcceptable(polygon) || depth <= 0 || (polygon.width() < Option.defaultImageWidth())) {
            List<Polygon> result = new ArrayList<>();
            result.add(polygon);
            return result;
        }

        double bestSumWidth = Double.MAX_VALUE;

        for(int i = 0; i < polygon.numSides(); i++) {
            if(System.nanoTime() > lastStartTime + maxTime) {
                System.err.println("Timeout");
                break; // timeout
            }
            if(! angles[i].isConcave()) {
                if(VERBOSE) System.out.printf("Angle %d (%1.2f rad) of the %dgon isn't concave\n", i, angles[i].measure(), polygon.numSides());
                continue;
            } else if(VERBOSE) System.out.printf(">> Trying angle %d (%1.2f rad)\n", i, angles[i].measure());


            for(int k = 0; k < polygon.toLines().length; k++) {
                if(System.nanoTime() > lastStartTime + maxTime) {
                    System.err.println("Timeout");
                    break; // timeout
                }
                Line l = polygon.toLines()[k];
                if(VERBOSE) System.out.println(">>>> parallel to edge " + k + " on the " + polygon.numSides() + "gon");
                if(l.parallel(angles[i].ab()) || l.parallel(angles[i].bc())) {
                    if(VERBOSE) System.out.println("Angle " + i + " can't use a parallel line " + k + " to split");
                    continue;
                }

                Point guide = new Point(points[i].x() + l.dx(),points[i].y() + l.dy());

                // If the guide is on the wrong side of the concave angle, a backwards ray will be generated.
                // So let's make sure that's not true.
                if(angles[i].containsPoint(guide)) guide = new Point(points[i].x() - l.dx(),points[i].y() - l.dy());

                Line splitLine = new Line(points[i], guide, Line.RAY);
                Point intersection = polygon.intersection(splitLine);

                // There are a few reasons why an arbitrary line can't be used as a splitting line for a polygon.

                // First: The line doesn't intersect with any in the polygon. Although the rules we've already set
                // should prevent this, lines which are parallel are considered non-intersecting immediately, and fuzziness
                // on the definition of "parallel" can cause intersecting lines to be considered "parallel enough".
                if(intersection == null) {
                    if(VERBOSE) System.out.println("Angle " + i + " has no intersection parallel to edge " + k);
                    continue;
                }

                // WARNING: splitIndex indicates the index of the *line* intersected.
                // The index of the intersection, if it is added to the polygon, is one greater than that.
                int splitIndex = polygon.indexOf(intersection);

                if(splitIndex == (i-1+n)%n || splitIndex == i) {
                    if(VERBOSE) System.out.println("Angle " + i + " not allowed to connect to side " + splitIndex + ": too close");
                    continue;
                }

                Polygon clone = polygon.getClone();
                clone.addPoint(intersection, splitIndex+1);

                // WARNING: splitting into the polygon before i means that i now refers to a totally different vertex!

                Polygon[] subregions = clone.split((splitIndex < i)? i+1 : i, splitIndex+1);

                if(VERBOSE) {
                    System.out.println("Angle " + i + " and a point in edge " + splitIndex + " creating a " + subregions[0].numSides() + "gon and a " + subregions[1].numSides() + "gon");
                    System.out.println(subregions[0]);
                    System.out.println(subregions[1]);
                }
                if(subregions[0].numSides() < 3 || subregions[1].numSides() < 3) {
                    if(VERBOSE) System.out.println("Angle " + i + " created an invalid polygon");
                    continue;
                }

                double sumWidth = subregions[0].width() + subregions[1].width();

                if(sumWidth < bestSumWidth) {
                    if(VERBOSE) System.out.println("Split from angle " + i + " to edge " + splitIndex + " parallel to edge " + k + " is the best so far: width sum " + sumWidth);
                    bestSubregions = subregions;
                    bestSumWidth = sumWidth;
                } else if(VERBOSE) System.out.println("Split from angle " + i + " to edge " + splitIndex + " parallel to edge " + k + " is subpar: width sum " + sumWidth);
            }
        }

        if(bestSubregions != null) {
            if(VERBOSE) System.out.printf("Decomposed into a %dgon and a %dgon\n", bestSubregions[0].numSides(), bestSubregions[1].numSides());
            List<Polygon> result = new ArrayList<>();


            result.addAll(decompose(bestSubregions[0], depth-1));
            result.addAll(decompose(bestSubregions[1], depth-1));
            return result;
        }

        //well darn, guess we give up

        List<Polygon> result = new ArrayList<>();
        result.add(polygon);
        return result;
    }
/***
    public void visualize(Graphics g) {

    }
***/
    public boolean isAcceptable(Polygon p) { return ! p.isConcave(); }

    @Override
    public String toString() { return "Jiao - Polygonal"; }
    protected class Reordering {
        List<Polygon> regions = new ArrayList<>();
        double cost = 0.0;
    }

    protected Reordering reorder(List<Polygon> regions, Polygon start) {

        if(regions.size() == 0) {
            Reordering result = new Reordering();
            result.regions.add(start);
            return result;
        }

        Reordering best = null;
        Reordering attempt;
        List<Polygon> cloneList = new ArrayList<>(regions);

        for(Polygon p: regions) {

            cloneList.remove(p);
            attempt = reorder(cloneList, p);

            if(p.sharedPoints(start).size() >= 2) attempt.cost += 1.0; // adjacent!
            else attempt.cost += start.center().distance(p.center()); // not adjacent...

            if(best == null || attempt.cost < best.cost) {
                attempt.regions.add(start);
                best = attempt;
            }
            cloneList.add(p);
        }

        return best;
    }
}
