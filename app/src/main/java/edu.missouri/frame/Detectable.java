package edu.missouri.frame;

import java.util.Random;

import edu.missouri.geom.Point;
import edu.missouri.geom.Util;

public class Detectable extends Point {

    private double height; // height it was detected at, in meters
    private double base; // assumed to be detectable at 0 meters
    private double confidence;
    private double noisyConfidence;
    private boolean truth;

    public Detectable(double x, double y, double h, double g, boolean t) {
        super(x, y);
        height = h;
        base = g;
        truth = t;
        Random random = new Random((long) (x*y+h));

        if(truth) confidence = (1/base) * (2.02 - 0.357* Math.log(h));
        else confidence = base * (0.0163 + 0.157* Math.log(h));

        double r;
        if(truth) r = random.nextGaussian() * (.32+.00412*h-.0000249*h*h);
        else r = random.nextGaussian() * (0.341 + .000716*h-.00000709*h*h);

//        confidence = truth? trueConf(h) : falseConf(h);
//        if(confidence > base) System.err.println("Invalid confidence generated");

        noisyConfidence = Util.constrain(0.0, 1.0, confidence + r);

    }

    // TODO: we have no empirical data here yet
    public double confidence() {
        return noisyConfidence;
    }

    public double detectedFrom() { return height; }

    public Detectable atHeight(double h) {
        return new Detectable(x(), y(), h, base, truth);
    }

/***
    @Override
    public void render(Graphics g) {
//        if(base > 0.5) g.setColor(new Color(0, 180, 80));
//        else g.setColor(new Color(200, 40, 0));

        g.setColor(Color.BLACK);
        if(truth) g.setColor(new Color(0, 160, 160));
        else g.setColor(new Color(200, 80, 0));
        g.fillOval(ix()-5, iy()-5, 10, 10);

        g.setColor(Color.BLACK);
        if(sqDistance(Input.mouse()) < 100) {
            String s = truth? "T" : "F";
            g.drawString(String.format("%2.0f%% (%s)", base * 100, s), ix() + 5, iy());
        }
    }
***/
    public boolean real() {
        return truth;
    }
}
