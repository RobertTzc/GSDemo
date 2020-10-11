package edu.missouri.drone.static_height;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.missouri.drone.Drone;
import edu.missouri.frame.Area;
import edu.missouri.frame.Option;
import edu.missouri.geom.Point;
import edu.missouri.geom.Util;

import static edu.missouri.frame.Option.cruiseAltitude;

public class PathTransformDrone extends Drone {

    private Point start, end;

    private double areaWidth;
    private double areaHeight;
    private static double imageWidth = Option.defaultImageWidth();

    Integer[][] grid;


    public PathTransformDrone(Area area) {
        super(area);
    }
    protected int boundaryDistance(Point p) {
        Point a = toReal(p);
        return ((int) (8*getPolygon().distance(a)/imageWidth + .5));
    }

    private void initGrid() {
        areaWidth = getPolygon().getBoundsWidth();
        areaHeight = getPolygon().getBoundsHeight();

        grid = new Integer[(int) (areaWidth /imageWidth+1)][(int) (areaHeight /imageWidth+1)];

        end = toGrid(getTargetEnd());
        start = toGrid(getLocation());
        grid[end.ix()][end.iy()] = 0;

        java.util.Queue<Point> queue = new LinkedList<>(Util.adjacent(grid, start, 1));
        while(! queue.isEmpty()) {
            Point current = queue.remove();

            if(! onArea(current))  continue;
            if(grid[current.ix()][current.iy()] == null) grid[current.ix()][current.iy()] = 1;

            List<Point> surroundings = Util.adjacent(grid, current, 1);
            for(Point p: surroundings) {
                if(! onArea(p)) continue;
                if(grid[p.ix()][p.iy()] == null || grid[p.ix()][p.iy()] > grid[current.ix()][current.iy()] + 1) {
                    grid[p.ix()][p.iy()] = grid[current.ix()][current.iy()] + 1;
                    queue.addAll(Util.adjacent(grid, p, 1));
                }
            }
        }

        for(int x = 0; x < grid.length; x++) {
            for(int y = 0; y < grid[0].length; y++) {
                if(x == end.ix() && y == end.iy()) continue;
                if(grid[x][y] != null) grid[x][y] += boundaryDistance(new Point(x, y));
            }
        }
    }

    public void route() {
        initGrid();
        List<Point> result = new ArrayList<>();
        result.add(toReal(start));

        Point current = start;
        while(true) {

            Point bestPoint = null;
            int bestValue = Integer.MIN_VALUE;

            for(int i = 0; (bestPoint == null || bestPoint.equals(end)) && i < grid.length; i++) {
                List<Point> candidates = Util.adjacent(grid, current, i);

                for (Point p : candidates) {
                    if (! onArea(p) || grid[p.ix()][p.iy()] == null) continue;
                    if (bestPoint == null
                            || grid[p.ix()][p.iy()] > bestValue
                            || (grid[p.ix()][p.iy()] == bestValue) && (p.distance(current) < bestPoint.distance(current))) {
                        bestPoint = p;
                        bestValue = grid[p.ix()][p.iy()];
                    }
                }
            }

            if(bestPoint == null) {
                setHeading(Math.PI/2);
                for(Point p: result) {
                    moveTo(new Point(p.x(), p.y(), cruiseAltitude));
                    scan();
                }
            }

            current = bestPoint;
            grid[bestPoint.ix()][bestPoint.iy()] = null;
            result.add(toReal(bestPoint));
        }
    }

//    public void visualize(Graphics g) {



//        initGrid();
//        g.setColor(Color.BLACK);
//        for(int i = 0; i < grid.length; i++) {
//            for(int j = 0; j < grid[0].length; j++) {
//                if(grid[i][j] == null) continue;
//                Point loc = toReal(new Point(i, j));
//                g.drawString(grid[i][j] + "", loc.ix() - imageWidth/4, loc.iy() + imageWidth/2);
////                loc.render(g);
//            }
//        }

//    }

    public boolean onArea(Point p) {
        if(p.ix() < 0 || p.iy() < 0 || p.ix() > grid.length || p.iy() > grid[0].length) return false;
        p = toReal(p);
        return getPolygon().contains(new Point(p.x(), p.y()));
    }

    public Point toGrid(Point p) {
        return new Point(p.x() / imageWidth, p.y() / imageWidth);
    }
    public Point toReal(Point p) {
        return new Point(p.x() * imageWidth + imageWidth/2.0, p.y() * imageWidth + imageWidth/2.0, cruiseAltitude);
    }

    @Override
    public String toString() {return "Zelinsky - Path Transform"; }
}
