package edu.missouri.geom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import edu.missouri.frame.Option;

public class csvCreate {
    private List<Point> predecideWayPoints;
    private List<Point> wayToPorint;
    private List<Point> wayBackPoint;
    List<CordtoGPS> coordinates = new ArrayList<>();
    private double standardLatitude;//y
    private double standardLongitude;//x
    private double standaX;
    private double standaY;
    private double altitude;
    public csvCreate(List<Point> predecideWayPoints, List<Point> wayToPorint){
        this.predecideWayPoints = predecideWayPoints;
        this.wayToPorint = wayToPorint;
        standardLatitude = Option.GPSstartPoint.latitude;
        standardLongitude =  Option.GPSstartPoint.longtitude;
        standaX = 0;
        standaY  = 0;
        altitude = Option.cruiseAltitude;

        for(Point data : predecideWayPoints){
            boolean isTurning = false;
            for (Point point : wayToPorint){
                if (data.x() == point.x() && data.y() == point.y()){
                    isTurning = true;
                }
            }
            coordinates.add(new CordtoGPS(standardLongitude,standardLatitude,data.x()-standaX, data.y()-standaY,altitude,0,0,isTurning));
        }
    }

    public List<CordtoGPS> getCoordinates(){
        return coordinates;
    }

    public void writeCSV( String finalPath) {
        FileOutputStream out = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            File finalCSVFile = new File(finalPath);
            out = new FileOutputStream(finalCSVFile);
            osw = new OutputStreamWriter(out, "UTF-8");
            // 手动加上BOM标识
            osw.write(new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}));
            bw = new BufferedWriter(osw);
            /**
             * 往CSV中写新数据
             */
            String title = "";
            title = "latitude,longitude,altitude(m),isTurning";
            bw.append(title).append("\r");

            if (coordinates != null && !coordinates.isEmpty()) {
                for (CordtoGPS data : coordinates) {
                    bw.append(data.getLatitude() + ",");
                    bw.append(data.getLongitude() + ",");
                    bw.append(data.getAltitude() + ",");
                    bw.append(data.isTurning() + ",");
                    for(int i=0;i<41;i++){
                        bw.append(0 + ",");
                    }
                    bw.append("\r");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (bw != null) {
                try {
                    bw.close();
                    bw = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                    osw = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        System.out.println(finalPath + "数据导出成功");
    }
}

