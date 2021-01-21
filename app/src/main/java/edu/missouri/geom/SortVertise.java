package edu.missouri.geom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.missouri.frame.GePoint;

public class SortVertise {

    public static List<GePoint> GPSVertices;
    public static GePoint GPScenter;


    public SortVertise(List<GePoint> points){
        double plusX = 0, plusY = 0;
        for (int i =0;i<points.size();i++) {
            plusX += points.get(i).latitude;
            plusY +=  points.get(i).longtitude;
        }
        this.GPSVertices = points;
        this.GPScenter = new GePoint(plusX / points.size(), plusY / points.size());


    }


    public double getAngle1(double lat_a, double lng_a, double lat_b, double lng_b) {
        double y = Math.sin(lng_b - lng_a) * Math.cos(lat_b);
        double x = Math.cos(lat_a) * Math.sin(lat_b) - Math.sin(lat_a) * Math.cos(lat_b) * Math.cos(lng_b - lng_a);
        double brng = Math.atan2(y, x);
        brng = Math.toDegrees(brng);
        if (brng < 0)
            brng = brng + 360;
        return brng;
    }

    public HashMap<Integer, ArrayList<Object>> getMapAll(List<GePoint> GPSVertices, GePoint GPScenter){
        HashMap<Integer, ArrayList<Object>> mapAll = new HashMap<>();
        for (int i = 0; i < GPSVertices.size(); i++) {
            ArrayList<Object> objList = new ArrayList<>();
            objList.add(GPSVertices.get(i));
            objList.add(getAngle1(GPScenter.latitude, GPScenter.longtitude,
                    GPSVertices.get(i).latitude, GPSVertices.get(i).longtitude));
            mapAll.put(i, objList);
        }
        return mapAll;
    }

    public HashMap<Integer, ArrayList<Object>> SortMapAll(HashMap<Integer, ArrayList<Object>> mapAll){
        ArrayList<Object> temp = new ArrayList<>();
        int size = mapAll.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1 - i; j++) {
                if (Double.parseDouble(mapAll.get(j).get(1).toString()) >
                        Double.parseDouble(mapAll.get(j + 1).get(1).toString()))  //交换两数位置
                {
                    temp = mapAll.get(j);
                    mapAll.put(j, mapAll.get(j + 1));
                    mapAll.put(j + 1, temp);
                }
            }
        }
        return mapAll;
    }

    public List<GePoint> sortCounterClockwiseVertices(){
        HashMap<Integer, ArrayList<Object>> mapAll = SortMapAll(getMapAll(GPSVertices,GPScenter));
        List<GePoint> result = new ArrayList<>();
        for(Integer integer:mapAll.keySet()){
            result.add((GePoint) mapAll.get(integer).get(0));
        }
        Collections.reverse(result);
        return result;
    }

    public List<GePoint> getCounterClockwiseVertices(){
        HashMap<Integer, ArrayList<Object>> mapAll = getMapAll(GPSVertices,GPScenter);
        int size = mapAll.size();
        if(Double.parseDouble(mapAll.get(size-1).get(1).toString())> Double.parseDouble(mapAll.get(size-2).get(1).toString())){
            Collections.reverse(GPSVertices);
        }
        return GPSVertices;
    }



}
