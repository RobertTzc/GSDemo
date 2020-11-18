package edu.missouri.geom;

@SuppressWarnings("unused")
public class Point {

    private static final double GRANULARITY = 1;

    // Style arguments
    public static final int CROSS = 1;
    private static final int DOT = 0;

    private double[] coordinates;

    public Point(double x, double y) {
        coordinates = new double[2];
        coordinates[0] = x;
        coordinates[1] = y;
    }

    public Point(Point p, double z) {
        coordinates = new double[3];
        coordinates[0] = p.x();
        coordinates[1] = p.y();
        coordinates[2] = z;
    }


    public Point(double... coordinates) {
        this.coordinates = coordinates.clone();
    }

    public double[] toArray() {
        return new double[]{coordinates[0], coordinates[1]};
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");

        for(int i = 0; i < coordinates.length; i++) {
            builder.append(String.format("%.2f", coordinates[i]));
            if(i != coordinates.length-1) builder.append(", ");
        }
        builder.append(")");
        return builder.toString();
    }

    public double x()      { return (coordinates.length > 0)? coordinates[0] : 0; }
    public double y()      { return (coordinates.length > 1)? coordinates[1] : 0; }
    public double z()      { return (coordinates.length > 2)? coordinates[2] : 0; }
    public double n(int n) { return (coordinates.length > n)? coordinates[n] : 0; }

    public int ix()      { return (int) Util.constrain(((coordinates.length > 0)? coordinates[0] : 0), Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public int iy()      { return (int) Util.constrain(((coordinates.length > 1)? coordinates[1] : 0), Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public int iz()      { return (int) Util.constrain(((coordinates.length > 2)? coordinates[2] : 0), Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public int in(int n) { return (int) Util.constrain(((coordinates.length > n)? coordinates[n] : 0), Integer.MIN_VALUE, Integer.MAX_VALUE); }

    public int dim() { return coordinates.length; }

    public boolean same(Point other){
        if(x()==other.x() && y() == other.y()){
            return true;
        }
        else {
            return false;
        }
    }

    public double distance(Point other) { return Math.sqrt(sqDistance(other)); }
    public double distance(Line l) {
        Point a = l.a();
        Point b = l.b();
        double len = a.sqDistance(b);
        if (len == 0) return sqDistance(a);
        double t = ((ix() - a.ix()) * (b.ix() - a.ix()) + (iy() - a.iy()) * (b.iy() - a.iy())) / len;
        t = Util.constrain(t, 0, 1);
        return Math.sqrt(sqDistance(new Point((int) (a.ix() + t * (b.ix() - a.ix())), (int) (a.iy() + t * (b.iy() - a.iy())))));
    }

    public double orthoDistance(Line l) {
        Point a = l.a();
        Point b = l.b();
//        if(parallel(l)) return null;
        double len = a.sqDistance(b);
        if (len == 0) return sqDistance(a);
        double t = ((ix() - a.ix()) * (b.ix() - a.ix()) + (iy() - a.iy()) * (b.iy() - a.iy())) / len;
        return Math.sqrt(sqDistance(new Point((int) (a.ix() + t * (b.ix() - a.ix())), (int) (a.iy() + t * (b.iy() - a.iy())))));
    }

    public Point rotate(double theta) {
        return new Point(
                x() * Math.cos(theta) - y() * Math.sin(theta),
                x() * Math.sin(theta) + y() * Math.cos(theta));
    }

    public double sqDistance(Point other) {
        int sum = 0;
        for(int i = 0; i < Math.max(dim(), other.dim()); i++)
            sum += (other.n(i) - n(i))*(other.n(i) - n(i));
        return sum;
    }

    public double bearing() {return bearing(new Point(0, 0)); }
    public double bearing(Point p) {
        if(p.x() == x()) return Math.PI/2 + ((y() > p.y())? Math.PI : 0);
        return (Math.atan((p.y() - y()) / (p.x() - x())) + (p.x()<x()? Math.PI:0) + 2* Math.PI) % (2 * Math.PI);
    }



    @Override
    public boolean equals(Object p) {
        if(! (p instanceof Point)) return false;
        if(this == p) return true;
        Point q = (Point) p;
        return Util.approx(q.x(), x(), GRANULARITY)
                && Util.approx(q.y(), y(), GRANULARITY);
    }

    /***public java.awt.Point toAWTPoint() {
        return new java.awt.Point(ix(), iy());
    }
    ***/
    public Polygon toSquare(double width) {
        return new Polygon(
                new Point(x() + width, y() + width),
                new Point(x() + width, y() - width),
                new Point(x() - width, y() - width),
                new Point(x() - width, y() + width)
        );
    }
}
