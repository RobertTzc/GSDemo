package edu.missouri.frame;

import edu.missouri.geom.Point;

public class Input {

    public static int mouseX = 0;
    public static int mouseY = 0;
    public static int clicks = 0;

    public static Point mouse() { return new Point(mouseX, mouseY); }
}
