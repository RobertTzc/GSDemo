package edu.missouri.drone.variable_height;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import edu.missouri.drone.Drone;
import edu.missouri.drone.Paths;
import edu.missouri.drone.Turning;
import edu.missouri.drone.WayPointCheck;
import edu.missouri.drone.static_height.JiaoDrone;
import edu.missouri.drone.static_height.PlowDrone;
import edu.missouri.frame.Area;
import edu.missouri.frame.Detectable;
import edu.missouri.frame.Option;
import edu.missouri.frame.QueueFuntions;
import edu.missouri.geom.Line;
import edu.missouri.geom.Point;
import edu.missouri.geom.Polygon;

public class ImprovedDirectDrone extends Drone {


    Queue<Polygon> regions = new LinkedList<>();
    double thoroughness = 1.0;

    public ImprovedDirectDrone(Area area) {
        super(area);
        regions.addAll(new JiaoDrone(area).decompose(getPolygon(), 500));
    }

    public void route() {
        List<Point> done = new ArrayList<>();
        double overviewAltitude = Option.cruiseAltitude;
        Polygon origin = getPolygon();
        setHeading(getPolygon().widthLine().measure() + Math.PI/2.0);
        Polygon poly = getPolygon();
        List<Point> wayToPoints= PlowDrone.plan(poly,poly.getLeftBasicPoint());
        Queue<Point> currentPoints = new LinkedList<>(Drone.subdivide(wayToPoints));
        Queue<Double> angles = new LinkedList<>();
        for (int k = 0;k < currentPoints.size();k++){
            angles.add(getPolygon().widthLine().measure() + Math.PI/2.0);
        }

        List<Integer> index = getWayBackPoints(poly.getTopPoint());//get the way back(point index on the way back)
        List<Point> wayBackPoints = new ArrayList<>();
        Point[] points = poly.toPoints();
        List<Double> angle = new ArrayList<>();
        for(int i = 0; i<index.size()-1;i++){
            int indexStart = index.get(i);
            int indexEnd = index.get(i+1);
            Point[] originPoints = poly.toPoints();
            Point startOrigin = originPoints[indexStart];
            Point endOrigin = originPoints[indexEnd];
            Line line = new Line(startOrigin,endOrigin);
            Point farthestPoint = poly.farthest(line);
            Line widthLine = Line.perpendicularTo(line,farthestPoint);
            double theta = widthLine.measure() + Math.PI/2.0;
            angle.add(theta);
            wayBackPoints.addAll(PlowDrone.planBack(getPolygon(),indexStart,indexEnd));
            int num = Drone.subdivide(PlowDrone.planBack(getPolygon(),indexStart,indexEnd)).size();
            for (int j=0;j<num;j++){
                angles.add(theta);
            }
        }
        List<Point> turningPoints = new ArrayList<>();
        Queue<Point> pointBack = new LinkedList<>(Drone.subdivide(wayBackPoints));
        Queue<Point> wayPoints = QueueFuntions.mergeQueue(currentPoints,pointBack);

        Point firstWayPoint = QueueFuntions.findClosetPoint(wayPoints,area.getStart());
        wayPoints = QueueFuntions.mergeQueue(currentPoints,pointBack);

        int indexOfFirstWayPoint = QueueFuntions.pointToIndex(wayPoints,firstWayPoint);
        wayPoints = QueueFuntions.mergeQueue(currentPoints,pointBack);
        Queue<Point> reSortedQueue = QueueFuntions.pointReSortQueue(wayPoints,indexOfFirstWayPoint,area.getStart());
        predecideWayPoints = QueueFuntions.pointQueueToList(reSortedQueue);
        Queue<Double> reSortedAngles = QueueFuntions.angleReSortQueue(angles,indexOfFirstWayPoint);
        int indexOfPath = 0;

        for (int i=0;i<predecideWayPoints.size();i++){
            WayPointCheck point = new WayPointCheck(predecideWayPoints,i);
            if (point.isATurn()||i == predecideWayPoints.size()-1){
                paths.add(new Paths(predecideWayPoints,indexOfPath,i));
                indexOfPath = i;
            }
        }

        for (int i=0;i<predecideWayPoints.size();i++){
            WayPointCheck point = new WayPointCheck(predecideWayPoints,i);
            boolean isTurn = point.isATurn();
            if(isTurn){
                turnings.add(new Turning(predecideWayPoints,i));
            }
        }

        while(! reSortedQueue.isEmpty()) {

            Point p = new Point();
            Double a =0.0;
            Capture c;
            p = reSortedQueue.remove();
            p = new Point(p.x(), p.y(), overviewAltitude);
            a = reSortedAngles.remove();
            moveTo(p);
            if (a==10000.0){

            }
            else {
                if (a==null){
                    c = scan();
                }
                else {
                    c = scan(a);
                }
                List<Point> toDo = new ArrayList<>();
                for(Detectable d: c.detectables) {
                    if (done.contains(d)) continue;
                    done.add(d);
                    double k = altitudeNeeded(d);
                    if (k >= overviewAltitude) continue;
                    Point q = scanArea(new Point(d.x(), d.y(), k-10)).closest(getLocation());
                    Point p2 = new Point(q.x(), q.y(), k);
                    toDo.add(p2);
                }

                for(Point q: heuristicTSP(toDo, getLocation(), currentPoints.peek())) {
                    moveTo(q);
                    if (a!=null){
                        scan(a);
                    }
                    else {
                        scan();
                    }
                }
            }
        }

    }

    public Map<Point, Boolean> routes() {
        List<Point> done = new ArrayList<>();
        double overviewAltitude = Option.cruiseAltitude;
        Polygon origin = getPolygon();
        setHeading(getPolygon().widthLine().measure() + Math.PI/2.0);
        Polygon poly = getPolygon();
        List<Point> wayToPoints= PlowDrone.plan(poly,poly.getLeftBasicPoint());
        Queue<Point> currentPoints = new LinkedList<>(Drone.subdivide(wayToPoints));
        Queue<Double> angles = new LinkedList<>();
        for (int k = 0;k < currentPoints.size();k++){
            angles.add(getPolygon().widthLine().measure() + Math.PI/2.0);
        }

        List<Integer> index = getWayBackPoints(poly.getTopPoint());//get the way back(point index on the way back)
        List<Point> wayBackPoints = new ArrayList<>();
        Point[] points = poly.toPoints();
        List<Double> angle = new ArrayList<>();
        for(int i = 0; i<index.size()-1;i++){
            int indexStart = index.get(i);
            int indexEnd = index.get(i+1);
            Point[] originPoints = poly.toPoints();
            Point startOrigin = originPoints[indexStart];
            Point endOrigin = originPoints[indexEnd];
            Line line = new Line(startOrigin,endOrigin);
            Point farthestPoint = poly.farthest(line);
            Line widthLine = Line.perpendicularTo(line,farthestPoint);
            double theta = widthLine.measure() + Math.PI/2.0;
            angle.add(theta);
            wayBackPoints.addAll(PlowDrone.planBack(getPolygon(),indexStart,indexEnd));
            int num = Drone.subdivide(PlowDrone.planBack(getPolygon(),indexStart,indexEnd)).size();
            for (int j=0;j<num;j++){
                angles.add(theta);
            }
        }
        List<Point> turningPoints = new ArrayList<>();
        Queue<Point> pointBack = new LinkedList<>(Drone.subdivide(wayBackPoints));
        Queue<Point> wayPoints = QueueFuntions.mergeQueue(currentPoints,pointBack);

        Point firstWayPoint = QueueFuntions.findClosetPoint(wayPoints,area.getStart());
        wayPoints = QueueFuntions.mergeQueue(currentPoints,pointBack);

        int indexOfFirstWayPoint = QueueFuntions.pointToIndex(wayPoints,firstWayPoint);
        wayPoints = QueueFuntions.mergeQueue(currentPoints,pointBack);
        Queue<Point> reSortedQueue = QueueFuntions.pointReSortQueue(wayPoints,indexOfFirstWayPoint,area.getStart());
        predecideWayPoints = QueueFuntions.pointQueueToList(reSortedQueue);
        Queue<Double> reSortedAngles = QueueFuntions.angleReSortQueue(angles,indexOfFirstWayPoint);
        int indexOfPath = 0;

        for (int i=0;i<predecideWayPoints.size();i++){
            WayPointCheck point = new WayPointCheck(predecideWayPoints,i);
            if (point.isATurn()||i == predecideWayPoints.size()-1){
                paths.add(new Paths(predecideWayPoints,indexOfPath,i));
                indexOfPath = i;
            }
        }

        for (int i=0;i<predecideWayPoints.size();i++){
            WayPointCheck point = new WayPointCheck(predecideWayPoints,i);
            boolean isTurn = point.isATurn();
            if(isTurn){
                turnings.add(new Turning(predecideWayPoints,i));
            }
        }

        while(! reSortedQueue.isEmpty()) {

            Point p = new Point();
            Double a =0.0;
            Capture c;
            p = reSortedQueue.remove();
            p = new Point(p.x(), p.y(), overviewAltitude);
            a = reSortedAngles.remove();
            moveTo(p);
            if (a==10000.0){

            }
            else {
                if (a==null){
                    c = scan();
                }
                else {
                    c = scan(a);
                }
                List<Point> toDo = new ArrayList<>();
                for(Detectable d: c.detectables) {
                    if (done.contains(d)) continue;
                    done.add(d);
                    double k = altitudeNeeded(d);
                    if (k >= overviewAltitude) continue;
                    Point q = scanArea(new Point(d.x(), d.y(), k-10)).closest(getLocation());
                    Point p2 = new Point(q.x(), q.y(), k);
                    toDo.add(p2);
                }

                for(Point q: heuristicTSP(toDo, getLocation(), currentPoints.peek())) {
                    moveTo(q);
                    if (a!=null){
                        scan(a);
                    }
                    else {
                        scan();
                    }
                }
            }
        }
        Map<Point, Boolean> maps = new LinkedHashMap<>();
        for(Point point : wayBackPoints){
            wayToPoints.add(point);
        }

        for(Point data : predecideWayPoints){
            boolean isTurning = false;
            for (Point point : wayToPoints){
                if (data.x() == point.x() && data.y() == point.y()){
                    isTurning = true;
                }
            }
            maps.put(new Point(data.x(),data.y()),isTurning);
        }
        return maps;


    }

    private double altitudeNeeded(Detectable d) {
        double k = d.detectedFrom() * Math.abs(d.confidence() - Option.confidenceThreshold) * (1 / thoroughness);
        return Math.max(k, Option.minCruiseAltitude);
    }
/***
    @Override
    public void visualize(Graphics g) {

    }
***/
    public List<Point> heuristicTSP(List<? extends Point> points, Point start, Point end) {

        if(end == null) end = getTargetEnd();

        final Point finalEnd = end;
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point point, Point t1) {
                return (int) (point.distance(finalEnd) - t1.distance(finalEnd));
            }
        });

        List<Point> result = new ArrayList<>();
        result.add(start);
        result.add(end);


        for(Point p: points) {
            int bestIndex = 1;
            double bestLength = Double.MAX_VALUE;
            for(int j = 1; j < result.size()-1; j++) {
                result.add(j, p);
                double d = pathLength(result);
                if(d < bestLength) {
                    bestLength = d;
                    bestIndex = j;
                }
                result.remove(p);
            }
            result.add(bestIndex, p);
        }
        result.remove(start);
        result.remove(end);
        return result;
    }

//    public void reroute(double energy) {
//        Option.cruiseAltitude = Option.minCruiseAltitude;
//        thoroughness = 1.0;
//        reroute();
//        while(energyUsed() > energy && Option.cruiseAltitude < Option.maxAltitude) {
//            Option.cruiseAltitude += 1;
//            thoroughness -= 1.0/Option.maxAltitude;
//            reroute();
//        }
//    }
    //find the lowest flying attitude that cover the whole area within energy buget
//    public void reroute(Area area, double energyBudget) {
//        Option.cruiseAltitude = Option.minCruiseAltitude;
//        thoroughness = 1.0;
//        reroute(area);
//        double e = energyUsed();
//        while(e > energyBudget && Option.cruiseAltitude < Option.maxAltitude) {
//            Option.cruiseAltitude += 1;
//            thoroughness -= 1.0/Option.maxAltitude;
//            reroute(area);
//            e = energyUsed();
//        }
//
//    }

    public void reroute(Area area, double energyBudget) {
        thoroughness = 1.0;
        reroute(area);
    }
    public List<Integer> getWayBackPoints(Point p){
        Point LeftBasicPoint = getPolygon().getLeftBasicPoint();
        Point RightBasicPoint = getPolygon().getRightBasicPoint();
        Point[] points = getPolygon().toPoints();
        int pIndex = getPolygon().indexOf(p);
//        System.out.println(pIndex);
        int LeftBasicIndex = getPolygon().indexOf(LeftBasicPoint);
        int RightBasicIndex = getPolygon().indexOf(RightBasicPoint);
        int BasicIndex;
        BasicIndex = LeftBasicIndex;
//        System.out.println(BasicIndex);
        if(pIndex>BasicIndex){

            int step1 = pIndex-BasicIndex;
            int step2 = points.length - pIndex + BasicIndex;
            List<Integer> path1 = new ArrayList<>();
            for (int i =0;i<step1 +1;i++){
                path1.add( pIndex -i);
            }
            List<Integer> path2 = new ArrayList<>();

            int j = 0;
            for (int i =pIndex;i<points.length;i++){
                path2.add(pIndex +j) ;
                j++;
            }
            for (int i =0;i<=BasicIndex;i++){
                path2.add(i);
                j++;
            }

            boolean containRightPoint = false;
            if(path1.contains(RightBasicIndex)){
                containRightPoint = true;
            }
            return containRightPoint?path2:path1;
        }

        int step1 = BasicIndex - pIndex;
        List<Integer> path1 = new ArrayList<>();
        for (int i =0;i<step1 +1;i++){
            path1.add(pIndex + i) ;
        }
        List<Integer> path2 = new ArrayList<>();

        int j = 0;
        for (int i =pIndex;i>-1;i--){
            path2.add(i);
            j++;
        }
        for (int i =points.length-1;i>=BasicIndex;i--){
            path2.add(i);
            j++;
        }

        boolean containRightPoint = false;
        if(path1.contains(RightBasicIndex)){
            containRightPoint = true;
        }

        return containRightPoint?path2:path1;
    }

    private double pathLength(List<Point> points) {
        double sum = 0;
        for(Line l: Line.arrayFromPoints(points.toArray(new Point[0]))) {
            sum += l.length();
        }
        return sum;
    }

    // Energy calculations
    // credit to DiFranco and Buttazzo

}
