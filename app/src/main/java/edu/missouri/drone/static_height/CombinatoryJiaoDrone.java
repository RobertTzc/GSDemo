package edu.missouri.drone.static_height;

import edu.missouri.frame.Area;

public class CombinatoryJiaoDrone extends JiaoDrone{
    public CombinatoryJiaoDrone(Area area) {
        super(area);
    }

    @Override
    public void preplan() {
        recompose();
        super.preplan();
    }

    public void recompose() {
//        for (int i = 0; i < regions.size(); i++) {
//            for (int j = i; j < regions.size(); j++) {
//                Polygon a = regions.get(i);
//                Polygon b = regions.get(j);
//                if (a == b) continue;
//                if (a.sharedPoints(b).size() == 2
//                        && a.widthLine().parallel(b.widthLine(), 0.5)) {
//                    regions.remove(a);
//                    regions.remove(b);
//                    regions.add(a.combine(b));
//                    i = 0;
//                    j = 0;
//                }
//            }
//        }
    }


    @Override
    public String toString() {
        return "Jiao w/ combination";
    }
}
