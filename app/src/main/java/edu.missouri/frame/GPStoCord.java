package edu.missouri.frame;

import edu.missouri.geom.Point;

public class GPStoCord {

    private static final double MACRO_AXIS = 6378137; // 赤道圆的平均半径
    private static final double MINOR_AXIS = 6356752; // 半短轴的长度，地球两极距离的一半

    // 返回Y坐标

    public static double[] getCord(GePoint basePoint, GePoint point) {
        double[] result = new double[2];
        result[0] = turnY(basePoint, point);
        result[1] = turnX(basePoint,point);
        return result;
    }
    private static double turnY(GePoint basePoint, GePoint point) {
        double a = Math.pow(MACRO_AXIS, 2.0);
        double b = Math.pow(MINOR_AXIS, 2.0);
        double c = Math.pow(Math.tan(basePoint.getLatitude()), 2.0);
        double d = Math.pow(1/ Math.tan(basePoint.getLatitude()),2.0);
        double x = a/ Math.sqrt(a + b*c);
        double y = b/ Math.sqrt(b + a*d);

        c = Math.pow(Math.tan(point.getLatitude()), 2.0);
        d = Math.pow(1/ Math.tan(point.getLatitude()), 2.0);

        double m = a/ Math.sqrt(a + b*c);
        double n = b/ Math.sqrt(b + a*d);

        double result = 0;

        if (basePoint.getLatitude() > point.getLatitude()){
            result = -new Point(x, y).distance(new Point(m, n));
        }
        else{
            result = new Point(x, y).distance(new Point(m, n));
        }
        return result;
    }
    // 返回X坐标
    private static double turnX(GePoint basePoint, GePoint point) {
        double a = Math.pow(MACRO_AXIS, 2.0);
        double b = Math.pow(MINOR_AXIS, 2.0);
        double c = Math.pow(Math.tan(basePoint.getLatitude()), 2.0);
        double x = a/ Math.sqrt(a + b*c);
        return x * (-point.getLongtitude() + basePoint.getLongtitude());
    }

}
