package edu.missouri.frame;

import java.util.ArrayList;
import java.util.List;

import edu.missouri.geom.Point;
import edu.missouri.geom.Polygon;


public class Area {

    private Polygon poly;
    private List<Detectable> objects;


    private static final byte BORDER = 1;
    private static final byte OUTSIDE = 2;
    private static final byte VISITED = 3;
    private static final byte INSIDE = 4;

    public static final byte RANDOM = -1;
    public static final byte CLUSTERED = -2;

    public static final double COORD_SCALE = 10000;
    public static final double ABS_ALTITUDE = 228;

    public Area(Point... points) {
        poly = new Polygon(points);
        objects = new ArrayList<>(genBirds(Option.numObjects, Option.distributor));
    }

    public Area(int n) {
        double[] angles = new double[n];
        double sum = 0.0;
        for (int i = 0; i < angles.length; i++) {
            angles[i] = Math.random() + 3.0;
            sum += angles[i];
        }
        for(int i = 0; i < angles.length; i++) angles[i] = angles[i]* Math.PI*2/sum;

        Point[] points = new Point[n];

        double angle = 0;
        for(int i = 0; i < angles.length; i++) {
            angle += angles[i];
            points[i] = new Point(
                    (600 * Math.random()+200) * Math.cos(-angle),
                    (600 * Math.random()+200) * Math.sin(-angle)
            );
        }

        poly = new Polygon(points);

        double dx = poly.leftmost().x();
        double dy = poly.upmost().y();
        for (int i = 0; i < points.length; i++) points[i] = new Point(points[i].x() - dx, points[i].y() - dy);

        poly = new Polygon(points);
        objects = new ArrayList<>(genBirds(Option.numObjects, Option.distributor));
    }

//    public Area(int n) {
//        double[] angles = new double[n];
//        double sum = 0.0;
//        double mean = 0.5;
//        double var = 1;
//        for (int i = 0; i < angles.length; i++) {
//            angles[i] = Math.random() + 3.0;
//            sum += angles[i];
//        }
//        for(int i = 0; i < angles.length; i++) angles[i] = angles[i]*Math.PI*2/sum;
//
//        Point[] points = new Point[n];
//
//        double angle = 0;
//        for(int i = 0; i < angles.length; i++) {
//            angle += angles[i];
//            Random r = new Random();
//            double randomNum = Math.sqrt(var)* r.nextGaussian() + mean;
//            points[i] = new Point(
//                    (600 * randomNum + 200) * Math.cos(-angle),
//                    (600 * randomNum + 200) * Math.sin(-angle)
//            );
//        }
//
//        poly = new Polygon(points);
//
//        double dx = poly.leftmost().x();
//        double dy = poly.upmost().y();
//        for (int i = 0; i < points.length; i++) points[i] = new Point(points[i].x() - dx, points[i].y() - dy);
//
//        poly = new Polygon(points);
//        objects = new ArrayList<>(genBirds(Option.numObjects, Option.distributor));
//    }

/***
    public Area(String imageFile, double fidelity) {
        byte[][] data = null;
        try {
            URL file = Area.class.getClassLoader().getResource(imageFile);
            if (file == null) {
                System.err.println("Error: Image \"" + imageFile + "\" could not be found.");
                System.exit(0);
            }
            data = imageToData(ImageIO.read(file));
        } catch (IOException e) {
            System.err.println("Error: Could not read image.");

            e.printStackTrace();
            System.exit(0);
        }
        List<Point> raw = new ArrayList<>();

        int startX = 0, startY = 0;
        boolean foundBorder = false;

        // Look for a valid starting location...
        for (; startX < data.length && !foundBorder; startX++) {
            for (startY = 0; startY < data[0].length && !foundBorder; startY++) {
                if (isExactBorder(startX, startY, data)) foundBorder = true;
            }
        }
        if (!foundBorder) throw new IllegalArgumentException("Improperly formatted image.");

        Point current = new Point(startX, startY);

        while (current != null) {
            raw.add(current);
            data[current.ix()][current.iy()] = VISITED;
            current = neighborder(current, data);
        }

        // This is going to be a bit flawed, because this is not a research project about polygonization.

        // First question: How far away from the other points can we go without needing to make
        // an angle to get there?
        // 100% fidelity - no data loss is acceptable. Returns a very complicated polygon (that hasn't been discretized.)
        // 0% fidelity - the simplest polygon imaginable.
        double maxStray = Math.min(data.length, data[0].length) * (1 - fidelity);

        List<Point> result = new ArrayList<>();
        List<Point> currentSeg = new ArrayList<>();

        // The first two are always good
        currentSeg.add(raw.get(0));
        currentSeg.add(raw.get(1));
        Point currentStart = raw.get(0);

        for (int i = 2; i < raw.size(); i++) {
            // The good news is that it's easy to find the distance of a point from a line segment.
            // We're going to essentially add points to a segment until we find one that's too far away.

            Point currentEnd = raw.get(i);

            // Does adding that point to the segment make any point too far away?
            boolean acceptable = true;
            for(Point p : currentSeg) if(p.distance(new Line(currentStart, currentEnd)) > maxStray) acceptable = false;

            if(!acceptable) {
                // Adding that point to the segment resulted in some other point being too far from the polygon.
                // In other words, we need an angle at the *last* point.
                result.add(raw.get(i - 1));
                currentSeg.clear();

                // as before, the first two are always good
                currentSeg.add(raw.get(i - 1));
                currentSeg.add(raw.get(i));
                currentStart = raw.get(i - 1);
            } else currentSeg.add(currentEnd);
        }

        result.add(currentSeg.get(currentSeg.size()-1));
        poly = new Polygon(result.toArray(new Point[0]));
        objects = new ArrayList<>(genBirds(Option.numObjects, Option.distributor));
    }
    public Area(File file) {
        Charset charset = Charset.forName("US-ASCII");
        StringBuilder kml = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {
            String line;
            while ((line = reader.readLine()) != null) {
                kml.append(line);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        if(! kml.toString().contains("<coordinates>") && kml.toString().contains("</coordinates>")) throw new IllegalArgumentException("Invalid KML file.");
        kml = new StringBuilder(kml.toString().split("<coordinates>")[1].split("</coordinates>")[0].replace("\n", ""));

        ArrayList<Point> result = new ArrayList<>();

        for(String s: kml.toString().split(" ")) {
            if(! s.equals("")) {
                String[] coords = s.split(",");
                if(coords.length < 2) continue;
                // for whatever reason, longitude comes first (it is the x coordinate I guess)
                // altitude is included, but we disregard it here
                result.add(new Point(-COORD_SCALE*Double.parseDouble(coords[0]), COORD_SCALE*Double.parseDouble(coords[1])));
            }
        }
        poly = new Polygon(result.toArray(new Point[0]));
        objects = new ArrayList<>(genBirds(Option.numObjects, Option.distributor));
    }
***/
    private List<Detectable> genBirds(int n, int positionModel) {
        List<Detectable> result = new ArrayList<>();
        Point[] positions;
        if (positionModel == CLUSTERED) positions = Positions.clustered(n, toPolygon());
        else positions = Positions.random(n, toPolygon());


        for(int i = 0; i < n; i++) {
            boolean truth = i < n/2;
            Detectable d = new Detectable(
                    positions[i].x(),
                    positions[i].y(),
                    0,
                    Math.random()*.2 + 1.0,
                    truth
            );
            result.add(d);
        }
        return result;
    }
    public void redistribute() {objects = genBirds(Option.numObjects, Option.distributor); }

    public Polygon toPolygon() {
        return poly;
    }
    public double getWidth() {
        Point[] bounds = poly.getBounds();
        return bounds[1].ix()-bounds[0].ix();
    }

    public double getHeight() {
        Point[] bounds = poly.getBounds();
        return bounds[1].iy()-bounds[0].iy();
    }

    public boolean contains(double x, double y) {
        return poly.contains(new Point(x, y));
    }

    public Point getStart() {
        return new Point(0,0) ;
    }

    public Point getEnd() { return getStart(); }
/***
    public void draw(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        java.awt.Polygon ap = poly.toAWTPolygon();
        g.setColor(new Color(150, 200, 255));
        g.fillPolygon(ap);
        BasicStroke bs = new BasicStroke(5);
        g.setStroke(bs);
        g.setColor(new Color(241,181,45));
        g.drawPolygon(ap);

        for(Detectable d: objects) d.render(g);

        g.drawLine((int) getWidth()-110, (int) getHeight()-10, (int) getWidth()-10, (int) (getHeight()-10));
        g.drawString("100 meters", (int) getWidth()-110, (int) getHeight()-20);

        getStart().render(g, Point.CROSS);
    }

    public static Area randomParallelogram() {
//        Point p1 = new Point(0, 0);
//        Point p2 = new Point(250 + Math.random() * 200, Math.random() * 300);
//        Point p3 = new Point(Math.random() * 200, 250+Math.random() * 200);
//        Point p4 = new Point(p3.x() + p2.x() - p1.x(), p3.y() + p2.y() - p1.y());
        Point p1 = new Point(0, 0);
        Point p2 = new Point(250 , 0);
        Point p3 = new Point(0, 250);
        Point p4 = new Point(250, 250
        );
        return new Area(p4, p2, p1, p3);
    }
 ***/
    public static Area readPolygonFromCSV() {
        Point[] vertices = Option.vertices;
        return new Area(vertices);
    }


    private boolean isExactBorder(int x, int y, byte[][] data) {
        // only pixels with all eight neighbors are eligible
        if (x <= 0 || y <= 0 || x >= data.length - 1 || y >= data[0].length - 1) return false;

        // only gold pixels are eligible
        if (data[x][y] != BORDER) return false;


        return (data[x + 1][y] == INSIDE ||
                data[x - 1][y] == INSIDE ||
                data[x][y + 1] == INSIDE ||
                data[x][y - 1] == INSIDE ||
                data[x + 1][y + 1] == INSIDE ||
                data[x + 1][y - 1] == INSIDE ||
                data[x - 1][y + 1] == INSIDE ||
                data[x - 1][y - 1] == INSIDE);
    }
    /***
    private Point neighborder(Point p, byte[][] data) {

        if (isExactBorder(p.ix() + 1, p.iy(), data)) return new Point(p.ix() + 1, p.iy());
        if (isExactBorder(p.ix() - 1, p.iy(), data)) return new Point(p.ix() - 1, p.iy());
        if (isExactBorder(p.ix(), p.iy() + 1, data)) return new Point(p.ix(), p.iy() + 1);
        if (isExactBorder(p.ix(), p.iy() - 1, data)) return new Point(p.ix(), p.iy() - 1);
        if (isExactBorder(p.ix() + 1, p.iy() + 1, data)) return new Point(p.ix() + 1, p.iy() + 1);
        if (isExactBorder(p.ix() + 1, p.iy() - 1, data)) return new Point(p.ix() + 1, p.iy() - 1);
        if (isExactBorder(p.ix() - 1, p.iy() + 1, data)) return new Point(p.ix() - 1, p.iy() + 1);
        if (isExactBorder(p.ix() - 1, p.iy() - 1, data)) return new Point(p.ix() - 1, p.iy() - 1);
        return null;
    }

    private static byte[][] imageToData(BufferedImage image) {
        byte[][] result = new byte[image.getWidth()][image.getHeight()];

        for (int x = 0; x < result.length; x++) {
            for (int y = 0; y < result[0].length; y++) {
                switch (image.getRGB(x, y) & 0xffffff) {
                    case 0x0000ff: {
                        result[x][y] = INSIDE;
                        break;
                    }
                    case 0x888800: {
                        result[x][y] = BORDER;
                        break;
                    }
                    case 0x008800: {
                        result[x][y] = OUTSIDE;
                        break;
                    }
                }
            }
        }
        return result;
    }
     ***/
    public List<Detectable> getDetectables() {
        return new ArrayList<>(objects);
    }

    public List<Detectable> getDetectables(Polygon p, double h) {
        List<Detectable> result = new ArrayList<>();
        for(Detectable d: objects) {
            if(p.contains(d)) {
                Detectable n = d.atHeight(h);
                if(n.confidence() > 0) result.add(n);
            }
        }
        return result;
    }
}
