package edu.missouri.geom;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class
Polygon {

    private Point[] points;

    public Polygon() { }

    public Polygon(Point... points) {

        this.points = points;
        for(Point p: points) if(p == null) throw new Error(toString());
    }

    public Polygon(int n) {
        Polygon poly;
        double[] angles = new double[n];
        double sum = 0.0;
        for (int i = 0; i < angles.length; i++) {
            angles[i] = Math.random();
            sum += angles[i];
        }
        for(int i = 0; i < angles.length; i++) angles[i] = angles[i]* Math.PI*2/sum;

        Point[] points = new Point[n];

        double angle = 0;
        for(int i = 0; i < angles.length; i++) {
            angle += angles[i];
            points[i] = new Point(
                    400 + (300 * Math.random()+100) * Math.cos(-angle),
                    400 + (300 * Math.random()+100) * Math.sin(-angle)
            );
        }

        poly = new Polygon(points);

        double dx = poly.leftmost().x();
        double dy = poly.upmost().y();
        for (int i = 0; i < points.length; i++) points[i] = new Point(points[i].x() - dx, points[i].y() - dy);

        this.points = points;
    }

    public int numSides() { return points.length; }

    public Point[] toPoints() {
        return points.clone();
    }

    public Line longestLine(){
        Line line = null;
        Double longest = 0.0;
        Line[] lines = toLines();
        for(int i = 0; i < points.length; i++) {
            if(lines[i].length() > longest){//calculate length
                line = lines[i];
                longest = line.length();
            }
        }
        return line;
    }
    public Line[] toLines() {
        Line[] result = new Line[points.length];
        for(int i = 0; i < points.length; i++) {
            result[i] = new Line(points[i], points[(i+1)%points.length]);
        }
        return result;
    }
    public Angle[] toAngles() {
        int n = points.length;
        Angle[] result = new Angle[n];
        for(int i = 0; i < points.length; i++) {
            result[i] = new Angle(points[(i+n-1)%n], points[i], points[(i+1)%n]);
        }
        return result;
    }
    // return 2 points that related to p
    public Point[] related(Point p){
        Point[] vertices = toPoints();
        Line[] lines = toLines();
        boolean containA = false;
        Point[] result = new Point[2];
        int count = 0;
        for(int i = 0;i<vertices.length;i++){
            if (p.same(vertices[i])){
                containA = true;
            }
        }
        if (containA ==true){
            for(int i = 0;i<lines.length;i++){
                if (lines[i].contains(p)){
                   if(lines[i].a().same(p)){
                        result[count] = lines[i].b();

                   }
                   else {
                       result[count] = lines[i].a();
                   }
                   count ++;
                }
            }
        }

        return result;
    }

    public void addPoint(Point point, int v) {
        if(point == null) return;
        v %= (points.length+1);
        Point[] result = new Point[points.length + 1];
        for(int i = 0; i < result.length; i++) {
            if(i <  v) result[i] = points[i];
            if(i == v) result[i] = point;
            if(i >  v) result[i] = points[i-1];
        }
        points = result;
    }
    public void removePoint(int v) {
        Point[] result = new Point[points.length - 1];
        for(int i = 0; i < result.length; i++) {
            if(i <  v) result[i] = points[i];
            if(i >  v) result[i] = points[i+1];
        }
        points = result;
    }
    public Point getLeftBasicPoint(){
        return  getBasicLine().a().y()>getBasicLine().b().y()?getBasicLine().a():getBasicLine().b();
    }
    public Point getRightBasicPoint(){
        return getBasicLine().a().y()>getBasicLine().b().y()?getBasicLine().b():getBasicLine().a();
    }

    public Polygon[] split(int v1, int v2) {

        v1 %= numSides();
        v2 %= numSides();

        Point[] poly1 = new Point[Math.abs(v1-v2)+1];
        Point[] poly2 = new Point[points.length - poly1.length + 2];

        if(poly1.length <= 2 || poly2.length <= 2) {
            throw new IllegalArgumentException("Invalid polygon split: " + v1 + " to " + v2 + ".\n" + toString());
        }


        int i1 = 0;
        int i2 = 0;
        for(int i = 0; i < points.length; i++) {
            if(i == v1 || i == v2) {
                poly1[i1] = points[i];
                i1++;
                poly2[i2] = points[i];
                i2++;
            } else if(Util.within(v1, v2, i)) {
                poly1[i1] = points[i];
                i1++;
            } else {
                poly2[i2] = points[i];
                i2++;
            }
        }

        Polygon result1 = new Polygon(poly1);
        Polygon result2 = new Polygon(poly2);

        return new Polygon[]{result1, result2};
    }



    public Point[] intersections(Line l) {

        List<Point> result = new ArrayList<>();

        for (Line s : toLines()) {
            Point p = s.intersection(l);
            if (p != null) result.add(p);
        }

        return result.toArray(new Point[0]);
    }
    public Point intersection(Line l) {
        // Important distinction: This one returns the intersection
        // which is closest to the first point on the line.
        // Works well for rays.

        double bestDist = Double.MAX_VALUE;
        Point best = null;

        Point[] intersections = intersections(l);
        if(intersections.length == 1) return intersections[0];
        for(Point p : intersections) {
            double k = p.sqDistance(l.a());

            if(k < bestDist && k > 1) {
                bestDist = k;
                best = p;
            }
        }
        return best;
    }

    public Polygon rotate(double theta) {
        Point[] result = new Point[points.length];
        Point c = center();

        for(int i = 0; i < points.length; i++) {
            result[i] = new Point(
                    ((points[i].x()-c.x()) * Math.cos(theta) - (points[i].y()-c.y()) * Math.sin(theta)) + c.x(),
                    ((points[i].x()-c.x()) * Math.sin(theta) + (points[i].y()-c.y()) * Math.cos(theta)) + c.y());
        }
        return new Polygon(result);
    }

    public int indexOf(Point p) {

        double min = Double.MAX_VALUE;
        int minI = 0;
        Line[] sides = toLines();

        for(int i = 0; i < numSides(); i++) if(points[i].equals(p)) return i;

        for(int i = 0; i < sides.length; i++) {
            double d = p.distance(sides[i]);
            if(d < min) {
                min = d;
                minI = i;
            }
        }
        return minI;
    }

    public boolean isConcave() {
        if(numSides() <= 3) return false;
        for(Angle a: toAngles()) if(a.isConcave()) return true;
        return false;

    }
    public boolean contains(Point p){
        boolean result = false;
        int[] mPolyX = new int[points.length];
        int[] mPolyY = new int[points.length];
        int mPolySize = points.length;
        for (int i = 0; i<points.length;i++){
            mPolyX[i] = points[i].ix();
            mPolyY[i] = points[i].iy();
        }
        for (int i = 0, j = mPolySize - 1; i < mPolySize; j = i++) {
            if ((mPolyY[i] < p.iy() && mPolyY[j] >= p.iy())
                    || (mPolyY[j] < p.iy() && mPolyY[i] >= p.iy())) {
                if (mPolyX[i] + (p.iy() - mPolyY[i]) / (mPolyY[j] - mPolyY[i])
                        * (mPolyX[j] - mPolyX[i]) < p.ix()) {
                    result = !result;
                }
            }
        }
        return result;


    }

    public Point[] getBounds() {
        int[] RangeX = new int[2];
        int[] RangeY = new int[2];
        for (int i = 0; i<points.length;i++) {
            RangeX[0] = Math.min(points[i].ix(), RangeX[0]);
            RangeX[1] = Math.max(points[i].ix(), RangeX[1]);
            RangeY[0] = Math.min(points[i].iy(), RangeX[0]);
            RangeY[1] = Math.max(points[i].iy(), RangeX[1]);
        }
        Point[] bound = new Point[2] ;
        bound[0] = new Point(RangeX[0],RangeY[0]);
        bound[1] = new Point(RangeX[1],RangeY[1]);
        return bound;
    }
    public double getBoundsWidth(){
        Point[] bounds = getBounds();
        return bounds[1].ix()-bounds[0].ix();
    }
    public double getBoundsHeight(){
        Point[] bounds = getBounds();
        return bounds[1].iy()-bounds[0].iy();
    }


    public double distance(Point p) {
        double min = Double.MAX_VALUE;
        for(Line l: toLines()) {
            double k = p.distance(l);
            if(k < min) min = k;
        }
        return min;
    }


    public double width() {
        return widthLine().length();
    }

    public double perimeter() {
        double sum = 0;
        for(Line l: toLines()) sum += l.length();
        return sum;
    }

    public Polygon getClone() {
        return new Polygon(points.clone());
    }

    public Point center() {
        double sumX = 0, sumY = 0;
        for(Point p: points) {
            sumX += p.x();
            sumY += p.y();
        }
        return new Point(sumX/numSides(), sumY/numSides());
    }

    public Point farthest(Line l) {
        double max = 0;
        Point best = null;
        for(Angle a: toAngles()){
//            if(a.isConcave()) continue;
            Point p = a.b();
            double k = p.orthoDistance(l);
            if(k > max) {
                max = k;
                best = p;
            }
        }
        return best;
    }

    public Point closest(Point p) {
        double bestDist = Double.MAX_VALUE;
        Point bestPoint = null;
        for(Line l: toLines()) {
            Point q = l.closest(p);
            if(bestPoint == null || q.distance(p) < bestDist) {
                bestPoint = q;
                bestDist = q.distance(p);
            }
        }
        return bestPoint;
    }

    public Point leftmost() {
        Point best = points[0];
        for(Point p: points) if(p.x() < best.x()) best = p;
        return best;
    }
    public Point rightmost() {
        Point best = points[0];
        for(Point p: points) if(p.x() > best.x()) best = p;
        return best;
    }
    public Point upmost() {
        Point best = points[0];
        for(Point p: points) if(p.y() < best.y()) best = p;
        return best;
    }
    public Point downmost() {
        Point best = points[0];
        for(Point p: points) if(p.y() > best.y()) best = p;
        return best;
    }

    public Point top(double x) {
        Point[] intersections = intersections(new Line(new Point(x, 1), new Point(x, 2), Line.INFINITE));

        if(intersections.length == 0) return null;
        Point top = intersections[0];
        for(Point p: intersections) if(p.y() < top.y()) top = p;
        return top;
    }
    public Point bottom(double x) {
        Point[] intersections = intersections(new Line(new Point(x, 1), new Point(x, 2), Line.INFINITE));

        if(intersections.length == 0) return null;
        Point bottom = intersections[0];
        for(Point p: intersections) if(p.y() > bottom.y()) bottom = p;
        return bottom;
    }
    public Point above(Point p) {
        Point[] intersections = intersections(new Line(new Point(p.x(), p.y()), new Point(p.x(), p.y()+1), Line.INFINITE));

        if(intersections.length == 0) return null;
        Point best = top(p.x());
        if(best == null) return null;

        for(Point k: intersections) if(k.y() < p.y() && k.y() > best.y()) best = k;
        return best;
    }
    public Point below(Point p) {
        Point[] intersections = intersections(new Line(new Point(p.x(), p.y()), new Point(p.x(), p.y()-1), Line.INFINITE));

        if(intersections.length == 0) return null;
        Point best = bottom(p.x());
        if(best == null) return null;

        for(Point k: intersections) if(k.y() > p.y() && k.y() < best.y()) best = k;
        return best;
    }

    public double area() {
        double area = 0;
        int j = numSides()-1;

        for (int i = 0; i < numSides(); i++) {
            area = area +  (points[j].x() + points[i].x()) * (points[j].y() - points[i].y());
            j = i;
        }
        return area/2;
    }

    public double reimannArea(double granularity) {
        double sum = 0;
        double height;
        for(double x = leftmost().x(); x < rightmost().x(); x += granularity) {
            if(x + granularity > rightmost().x()) height = Math.abs(top(x).y() - bottom(x).y());
            else height = Math.max(
                    Math.abs(top(x).y() - bottom(x).y()),
                    Math.abs(top(x + granularity).y() - bottom(x + granularity).y())
            );
            sum += height * granularity;
        }
        return sum;
    }

    public boolean hasPoint(Point point) {
        for(Point p: points) if(p.equals(point)) return true;
        return false;
    }

    public List<Point> sharedPoints(Polygon other) {
        List<Point> common = new ArrayList<>();
        for(Point p: points) if(other.distance(p) < 2) common.add(p);
        for(Point p: other.points) if(this.distance(p) < 2 && ! common.contains(p)) common.add(p);
        return common;
    }

    public Polygon combine(Polygon other) {

        List<Point> result = new ArrayList<>();
        List<Point> common = sharedPoints(other);

        if(common.size() > 2) throw new IllegalArgumentException("These polygons are too adjacent...");
        if(common.size() < 2) throw new IllegalArgumentException("These polygons are not adjacent...");


        if(! other.hasPoint(common.get(0))) other.addPoint(common.get(0), other.indexOf(common.get(0))+1);
        if(! other.hasPoint(common.get(1))) other.addPoint(common.get(1), other.indexOf(common.get(1))+1);
        if(!  this.hasPoint(common.get(0)))  this.addPoint(common.get(0), indexOf(common.get(0))+1);
        if(!  this.hasPoint(common.get(1)))  this.addPoint(common.get(1), indexOf(common.get(1))+1);

        int ic1 = indexOf(common.get(0));
        int ic2 = indexOf(common.get(1));
        int begin = Math.max(ic1, ic2);

        // Special case for modular arithmetic
        if(begin == numSides()-1 && (ic1 == 0 || ic2 == 0)) begin = 0;

        int i1 = begin;

        while(i1 != (begin - 1 + numSides()) % numSides()) {
            result.add(points[i1]);
            i1 = (i1 + 1) % numSides();
        }

        int i2 = other.indexOf(points[(begin - 1 + numSides()) % numSides()]);
        while(i2 != other.indexOf(points[begin])) {
            result.add(other.points[i2]);
            i2 = (i2 + 1) % other.numSides();
        }

        Polygon p = new Polygon(result.toArray(new Point[0]));
        if(p.numSides() != this.numSides() + other.numSides() - 2) System.err.println("!!!");
        return p;
    }


    public Line widthLine() {
        int n = points.length;
        double min = Double.POSITIVE_INFINITY;
        Line best = null;

        if(isConcave()) best = concaveWidthLine();
        // We still have to try the lines even if it's concave
        for(Line l: toLines()) {

            // A suitable line only intersects with the two lines next to it
            if(intersections(new Line(l, Line.INFINITE)).length > 2) continue;

            Point p = farthest(l);
            if(p == null) {

                System.err.println("Could not find a point closest to " + l);
                System.err.println("  There are " + numSides() + " points in this polygon.");
                System.err.println("  It is " + (isProper()? "":"not ") + "a proper polygon.");
                System.err.println(this);
                continue;
            }
            Line subBest = Line.perpendicularTo(l, p);
            if(subBest.length() < min){
                best = subBest;
                min = subBest.length();//FAREST LINE?
            }
        }

        if(best == null) {
            throw new IllegalArgumentException("Could not find the width of the polygon " + toString());
        }

        return best;
    }

    public Line getBasicLine() {
        int n = points.length;
        double min = Double.POSITIVE_INFINITY;
        Line best = null;
        Line basicLine = null;
        if(isConcave()) best = concaveWidthLine();
        // We still have to try the lines even if it's concave
        for(Line l: toLines()) {

            // A suitable line only intersects with the two lines next to it
            if(intersections(new Line(l, Line.INFINITE)).length > 2) continue;

            Point p = farthest(l);
            if(p == null) {

                System.err.println("Could not find a point closest to " + l);
                System.err.println("  There are " + numSides() + " points in this polygon.");
                System.err.println("  It is " + (isProper()? "":"not ") + "a proper polygon.");
                System.err.println(this);
                continue;
            }
            Line subBest = Line.perpendicularTo(l, p);
            if(subBest.length() < min){
                best = subBest;
                min = subBest.length();//FAREST LINE?
                basicLine = l;
            }
        }

        if(best == null) {
            throw new IllegalArgumentException("Could not find the width of the polygon " + toString());
        }

        return basicLine;
    }

    public Point getTopPoint() {

        return farthest(getBasicLine());
    }



//    public Line widthLine() {
//        int n = points.length;
//        double max = 0.0;
//        Line best = null;
//        Line longsetSide = longestLine();
//        System.out.println(longsetSide.length());
//        if(isConcave()) best = concaveWidthLine();
//
//        // We still have to try the lines even if it's concave
//
//        // A suitable line only intersects with the two lines next to it
//
//        Point p = farthest(longsetSide);
//        if(p == null) {
//
//            System.err.println("Could not find a point closest to " + longsetSide);
//            System.err.println("  There are " + numSides() + " points in this polygon.");
//            System.err.println("  It is " + (isProper()? "":"not ") + "a proper polygon.");
//            System.err.println(this);
//        }
//
//        best = Line.perpendicularTo(longsetSide, p);
//
//        if(best == null) {
//            throw new IllegalArgumentException("Could not find the width of the polygon " + toString());
//        }
//
//        return best;
//    }
    private Line concaveWidthLine() {
        Line best = null;
        for(Point a: points) {
            for(Point b: points) {
                if(a == b) continue;
                Line candidate = new Line(a, b);

                Line la = Line.infPerpendicularTo(candidate, a);
                Line lb = Line.infPerpendicularTo(candidate, b);
                la = new Line(la, Line.INFINITE);
                lb = new Line(lb, Line.INFINITE);

                boolean nonintersecting = true;

                for(Point p: intersections(la)) if(! p.equals(a)) nonintersecting = false;
                for(Point p: intersections(lb)) if(! p.equals(b)) nonintersecting = false;

                if(nonintersecting && (best == null || candidate.length() < best.length())) best = candidate;
            }
        }
        return best;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for(Point p: points) result.append("\t").append(p).append("\n");
        return result.toString();
    }

    public boolean isProper() {
        for(Line n: toLines()) {
            for(Line m: toLines()) {
                if(n.intersection(m) != null
                        && ! n.a().equals(m.a())
                        && ! n.a().equals(m.b())
                        && ! n.b().equals(m.a())
                        && ! n.b().equals(m.b())) {
                    System.err.println("This polygon is not proper.");
                    System.err.println(m);
                    System.err.println(n);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isSimplyTraversable() {
        Line widthLine = widthLine();
        double theta = -widthLine.a().bearing(widthLine.b());
        Polygon poly = rotate(theta);

        for(Angle a: poly.toAngles()) {
            if(a.isConcave() && (! Util.within(a.a().x(), a.c().x(), a.b().x()))) return false;
        }
        return true;
    }
}