package edu.missouri.geom;

import java.util.ArrayList;
import java.util.List;

import static edu.missouri.frame.Area.ABS_ALTITUDE;
import static edu.missouri.frame.Area.COORD_SCALE;

@SuppressWarnings("unused")
public class Util {

    public static double det(double[][] mat) {
        if(mat.length != mat[0].length) throw new ArithmeticException("determinant of non-square matrix");
        if(mat.length == 1) return mat[0][0]; // base case

        double sum = 0;
        for(int i = 0; i < mat.length; i++) {
            sum += ((i%2==0)? 1:-1) * mat[i][0] * det(minor(mat, i, 0));
        }
        return sum;
    }

    public static double[][] minor(double[][] mat, int x, int y) {

        if(x >= mat.length || y >= mat[0].length) return mat;

        double[][] result = new double[mat.length-1][mat[0].length-1];

        for(int i = 0; i < mat.length; i++) {
            for(int j = 0; j < mat.length; j++) {
                if(i < x && j < y) result[i][j] = mat[i][j];
                if(i < x && j > y) result[i][j-1] = mat[i][j];
                if(i > x && j < y) result[i-1][j] = mat[i][j];
                if(i > x && j > y) result[i-1][j-1] = mat[i][j];

            }
        }
        return result;
    }

    public static boolean within(double a, double b, double c, double t) {
        return c - t < Math.max(a, b) && c + t >= Math.min(a, b);

    } public static boolean within(double a, double b, double c) {
        return c <= Math.max(a, b) && c >= Math.min(a, b);
    }
    public static boolean approx(double a, double b, double t) {
        return Math.abs(a-b) < t;
    }

    @SuppressWarnings("unchecked")
    public static <I extends Comparable> I max(I... array) {
        I best = array[0];
        for(int i = 1; i < array.length; i++) {
            if(array[i].compareTo(best) > 0) best = array[i];
        }
        return best;
    }

    @SuppressWarnings("unchecked")
    public static <I extends Comparable> I min(I... array) {
        I best = array[0];
        for(int i = 1; i < array.length; i++) {
            if(array[i].compareTo(best) < 0) best = array[i];
        }
        return best;
    }

    public static double constrain(double i, double a, double b) {
        if(i > max(a, b)) i = max(a, b);
        if(i < min(a, b)) i = min(a, b);
        return i;
    }

    public static <I> List<Point> adjacent(I[][] grid, Point p, int d) {
        List<Point> result = new ArrayList<>();
        for (int i = -d; i < d; i++) {
            safeAdd(result, grid, p.ix()+i, p.iy()+d);
            safeAdd(result, grid, p.ix()+i+1, p.iy()-d);
            safeAdd(result, grid, p.ix()+d, p.iy()+i+1);
            safeAdd(result, grid, p.ix()-d, p.iy()+i);
        }
        return result;
    }

    private static <I> void safeAdd(List<Point> result, I[][] grid, int x, int y) {
        if(x < 0 || y < 0 || x > grid.length || y > grid[0].length) return;
        result.add(new Point(x, y));
    }

    public static <I> List<I> removeNull(List<I> list) {
        while(list.contains(null)) list.remove(null);
        return list;
    }

    public static int factorial(int num) {
        return (num <= 1)? 1 : num * factorial(num - 1);
    }

    public static <E> List<E> occurencesOf(E e, List<E> list) {
        List<E> result = new ArrayList<>();

        while(list.contains(e)) {
            result.add(list.remove(list.lastIndexOf(e)));
        }
        return result;
    }

    public static String toKML(List<Point> points) {
        StringBuilder result = new StringBuilder("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                "  <Document>\n" +
                "    <name>Coverage path</name>\n" +
                "    <description>Automatically generated.</description>\n" +
                "    <Style id=\"thickRedLine\">\n" +
                "          <LineStyle>\n" +
                "            <color>ff0000ff</color>\n" +
                "            <width>10</width>\n" +
                "          </LineStyle>\n" +
                "        </Style>\n" +
                "    <Placemark>\n" +
                "      <name>Absolute Extruded</name>\n" +
                "      <description></description>\n" +
                "      <styleUrl>#thickRedLine</styleUrl>\n" +
                "      <LineString>\n" +
                "        <tessellate>1</tessellate>\n" +
                "        <altitudeMode>relativeToGround</altitudeMode>\n" +
                "        <coordinates>\n");
        for (Point p : points)
            result.append("            ").append(-p.x()/COORD_SCALE).append(",").append(p.y()/COORD_SCALE).append(",").append(p.z() + ABS_ALTITUDE).append("\n");
        result.append("        </coordinates>\n      </LineString>\n    </Placemark>\n  </Document>\n</kml>");
        return result.toString();
    }
}
