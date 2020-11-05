package edu.missouri.frame;


import java.util.ArrayList;
import java.util.List;


public class main {


    public static void main(String args[]){
        //arg[0]=GPSVertices arg[1]=GPSstartpoint arg[2]=height arg[3] = overlap
        Double p0_x = 38.9129228409671;//x-lati,y-longti
        Double p0_Y = -92.2959491063508;
        Double p1_x = 38.9129228409671;
        Double p1_Y = -92.2959491063508;
        Double p2_x = 38.9113696239793;
        Double p2_Y = -92.2960270901189;
        Double p3_x = 38.9113237082811;
        Double p3_Y = -92.2939188738332;
        Double p4_x = 38.9128514328361;
        Double p4_Y = -92.2940476198659;
        List<GePoint> GPSVertices = new ArrayList<GePoint>();
        GPSVertices.add(new GePoint(p1_x, p1_Y));
        GPSVertices.add(new GePoint(p3_x, p3_Y));
        GPSVertices.add(new GePoint(p2_x, p2_Y));
        GPSVertices.add(new GePoint(p4_x, p4_Y));
        GePoint GPSstartpoint = new GePoint(p0_x, p0_Y);
        List<GePoint> GPSVertices2 = new ArrayList<GePoint>();
        GPSVertices2.add(new GePoint(p1_x, p1_Y));
        GPSVertices2.add(new GePoint(p3_x, p3_Y));
        GPSVertices2.add(new GePoint(p4_x, p4_Y));
        double height = 20;
        double overlap = 0.1;
//        String[] test = new String[4];
//        test[0] = "38.9129228409671,-92.2959491063508 38.9113696239793,-92.2960270901189 38.9113237082811,-92.2939188738332 38.9128514328361,-92.2940476198659";
//        test[1] = "38.9129228409671,-92.2959491063508";
//        test[2] = "20";
//        test[3] = "0";
//        String verticeString = args[0];
//        String startPointString = args[1];
//        String heightString = args[2];
//        String overlapString = args[3];
//        List<GePoint> GPSVertices = splitPointString(verticeString);
//        GePoint GPSstartpoint = splitPointString(startPointString).get(0);
//        double height = Double.parseDouble(heightString);
//        double overlap = Double.parseDouble(overlapString);


// below are output
        ReadFlightParameters model = new ReadFlightParameters();
        model.UpdateBounds(GPSVertices2, GPSstartpoint, height, overlap,1.0,5);


        List<GePoint> wayPoints = model.getWaypoints();
        List<Boolean> isTurnings = model.getIsTurning();
        List<Double> altitudes = model.getAltitudes();
        System.out.println(wayPoints.size());
        System.out.println(wayPoints.get(1).latitude);
        model.UpdateBounds(GPSVertices, GPSstartpoint, height, overlap,1.0,5);

        List<Double> altitudes2 = model.getAltitudes();
        List<GePoint> wayPoints2 = model.getWaypoints();
        System.out.println(wayPoints2.size());
        String[] output = dataToString(wayPoints,isTurnings,altitudes);
    }

    public static List<GePoint> splitPointString(String pointsString){
        List<GePoint> GPSPoints = new ArrayList<GePoint>();
        String[] pointStringArr = pointsString.split(" ");
        for (String pointString: pointStringArr){
            double latitude = Double.parseDouble(pointString.split(",")[0]);
            double longtitude = Double.parseDouble(pointString.split(",")[1]);
            GPSPoints.add(new GePoint(latitude,longtitude));
        }
        return GPSPoints;
    }

    public static String[] dataToString(List<GePoint> wayPoints, List<Boolean> isTurnings, List<Double> altitudes){
        int pointNum = wayPoints.size();
        String[] result = new String[pointNum];
        for(int i = 0;i< pointNum;i++){
            GePoint wayPoint = wayPoints.get(i);
            boolean isTurning = isTurnings.get(i);
            double altitude = altitudes.get(i);
            result[i] = String.format("%s,%s,%s,%s", Double.toString(wayPoint.latitude), Double.toString(wayPoint.longtitude), Double.toString(altitude), Boolean.toString(isTurning));
        }
        return result;

    }
}
