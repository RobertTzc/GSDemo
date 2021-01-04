package edu.missouri.geom;
public class Angle {

    private Point a;
    private Point b;
    private Point c;

    public Angle(Point a, Point b, Point c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Point a() { return a; }
    public Point b() { return b; }
    public Point c() { return c; }

    public Line ab() { return new Line(a, b); }
    public Line bc() { return new Line(b, c); }

    public boolean containsPoint(Point g) {
        double angleA = b.bearing(a);
        double angleC = b.bearing(c);
        double angleG = b.bearing(g);

        if(Math.abs(angleC - angleA) > Math.PI) return ! Util.within(angleA, angleC, angleG, 0);
        else return Util.within(angleA, angleC, angleG,0);
    }

    public boolean isConcave() {
        return measure() > Math.PI;
    }

    public boolean isStraight() {
        return measure() == Math.PI;
    }
    public boolean isStraight(double t) {
        if(a == null || b == null || c == null) return true;
        return Util.approx(Math.PI, measure(), t) || Util.approx(0.0, measure(), t);
    }

    public double measure() {
        return (b.bearing(c) - b.bearing(a) + 2 * Math.PI) % (2 * Math.PI);
    }

    public static Angle[] arrayFromPoints(Point[] points) {
        if(points.length < 2) return new Angle[0];
        Angle[] result = new Angle[points.length-2];
        for(int i = 0; i < result.length; i++) {
            result[i] = new Angle(points[i], points[i+1], points[i+2]);
        }
        return result;
    }

    public Angle rotate(double theta) {
        Point a2 = new Point(a.x() - b.x(), a.y() - b.y());
        Point c2 = new Point(c.x() - b.x(), c.y() - b.y());

        a2 = a2.rotate(theta);
        c2 = c2.rotate(theta);

        a2 = new Point(a2.x() + b.x(), a2.y() + b.y());
        c2 = new Point(c2.x() + b.x(), c2.y() + b.y());

        return new Angle(a2, b, c2);
    }
}
