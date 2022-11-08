package edu.missouri.drone.static_height;

import java.util.ArrayList;
import java.util.List;

import edu.missouri.drone.Drone;
import edu.missouri.frame.Area;
import edu.missouri.frame.Option;
import edu.missouri.geom.Line;
import edu.missouri.geom.Point;
import edu.missouri.geom.Polygon;
import edu.missouri.geom.Util;

import static edu.missouri.frame.Option.cruiseAltitude;

public class PlowDrone extends Drone {

    public PlowDrone(Area area) {
        super(area);
    }

    public void route() {
        setHeading(getPolygon().widthLine().measure() + Math.PI/2);
        for(Point p: subdivide(plan(getPolygon(), getLocation()))) {
            moveTo(new Point(p.x(), p.y(), cruiseAltitude));
            scan();
        }
    }

    public static List<Point> plan(Polygon poly, Point start) {
        Line widthLine = poly.widthLine();
        double theta1 = -widthLine.a().bearing(widthLine.b());
        double theta2 = -widthLine.b().bearing(widthLine.a());

        List<Point> option1 = PlowDrone.planTheta(poly, theta1, false);
        double score1 = option1.get(0).distance(start);
        List<Point> option2 =  PlowDrone.planTheta(poly, theta1, true);
        double score2 = option2.get(0).distance(start);
        List<Point> option3 =  PlowDrone.planTheta(poly, theta2, false);
        double score3 = option3.get(0).distance(start);
        List<Point> option4 =  PlowDrone.planTheta(poly, theta2, true);
        double score4 = option4.get(0).distance(start);

        double min = Util.min(score1, score2, score3, score4);
        List<Point> choice;
        if(min == score1) choice = option1;
        else if(min == score2) choice = option2;
        else if(min == score3) choice = option3;
        else if(min == score4) choice = option4;
        else throw new ArithmeticException();
        return choice;
    }

    public static List<Point> plan(Polygon original) {
        Line widthLine = original.widthLine();
        double theta = -widthLine.a().bearing(widthLine.b());
        return planTheta(original, theta, false);
    }


    public static List<Point> planTheta(Polygon original, double theta, boolean upsideDown) {
        double imageWidth = Option.defaultImageWidth();
        double imageHeight = Option.defaultImageHeight();
        double alt = Option.cruiseAltitude;
        double overlap = Option.overlap;
        Polygon poly = original.rotate(theta);
        List<Point> result = new ArrayList<>();
        double totalx = poly.longestLine().length(); //d
        double Lx = imageWidth;
//        double dx = (totalx - Lx)/(m-1);
        double maxX = poly.rightmost().x();
        double minX = poly.leftmost().x();
        int i = upsideDown? 1:0;
        double lastx = 0;
        for(double x = minX; x < maxX; x += imageWidth*(1-overlap)) {
//            lastx = x + imageWidth*(1-overlap);
//            Point iUp1 = poly.top(x - imageWidth/2.0);
//            Point iDown1 = poly.bottom(x - imageWidth/2.0);
//            Point iUp2 = poly.top(x + imageWidth/2.0);
//            Point iDown2 = poly.bottom(x + imageWidth/2.0);
            Point iUpx = poly.top(x);
            Point iDownx = poly.bottom(x);
//            double yUp = iUp1.y()<iUp2.y()?iUp1.y():iUp2.y();
//            double yDown = iDown1.y()>iDown2.y()?iDown1.y():iDown2.y();
            double yUp = iUpx.y() - imageHeight/2.0;
            double yDown = iDownx.y() + imageHeight/2.0;
            if(Math.abs(iUpx.y() - iDownx.y()) > 5){
                if(i%2 == 0) {
                    result.add(new Point(x , yUp + imageHeight/2.0, alt));
                    result.add(new Point(x , yDown - imageHeight/2.0, alt));
                } else {
                    result.add(new Point(x , yDown - imageHeight/2.0, alt));
                    result.add(new Point(x , yUp + imageHeight/2.0, alt));
                }
                i++;
            }
        }

        if(result.isEmpty()) {
            result.add(new Point(poly.leftmost().x(),  upsideDown? poly.upmost().y() : poly.downmost().y()));
            result.add(new Point(poly.rightmost().x(), upsideDown? poly.downmost().y() : poly.upmost().y()));
        }

        Point center = poly.center();
        return rotateAll(result, -theta, new Point(center.x(), center.y(), cruiseAltitude));
    }




    public static double traversalAltitude(Polygon p, double budget) {
        // first, non-coverage costs
        // cost to go from length to length is relative to perimeter
        // cost for the remainder segment is relative to area/width
        double testBudget = budget - p.perimeter()/4.0 - 2*p.reimannArea(500)/p.width();
        double width = p.reimannArea(500) / (testBudget);

        System.out.println(width / (2 * Math.sin(Drone.FOV_HEIGHT)));


        testBudget = budget - p.perimeter()/4.0 - 2*p.reimannArea(width)/p.width();
        return p.reimannArea(width) / (testBudget * 2 * Math.sin(Drone.FOV_HEIGHT));

    }

    private static List<Point> rotateAll(List<Point> points, double theta, Point c) {
        List<Point> result = new ArrayList<>();

        for (Point point : points) {
            result.add(new Point(
                    ((point.x() - c.x()) * Math.cos(theta) - (point.y() - c.y()) * Math.sin(theta)) + c.x(),
                    ((point.x() - c.x()) * Math.sin(theta) + (point.y() - c.y()) * Math.cos(theta)) + c.y(),
                    c.z()));
        }
        return result;
    }


    @Override
    public String toString() {
        return "Plowlike";
    }
}