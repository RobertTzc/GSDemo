package edu.missouri.frame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import edu.missouri.geom.Point;
import edu.missouri.geom.Polygon;


public class Positions {

//Pseudorandom
        public static Point[] Pseudorandom(int n, Polygon poly) {
            Point[] result = new Point[n];
            for (int i = 0; i < n; i++) {
                Point p = new Point(
                        Math.random() * poly.getBoundsWidth()+ poly.leftmost().x(),
                        Math.random() * poly.getBoundsHeight() + poly.upmost().y()
                );
                if (!poly.contains(p)) i--;
                else result[i] = p;
            }
            return result;
        }

//    Real Gaussian
    public static Point[] random(int n, Polygon poly) {
        Point[] result = new Point[n];
        Random rx = new Random(1);
        Random ry = new Random(2);
        Point center = poly.center();
        for (int i = 0; i < n; i++) {
            Point p = new Point(
                    Math.sqrt(1000)*rx.nextGaussian() + center.x(),
                    Math.sqrt(1000)*rx.nextGaussian() + center.y()
            );
            if (!poly.contains(p)) i--;
            else result[i] = p;
        }
        return result;
    }

//    public static Point[] random(int n, Polygon poly)  {
//
//        int[] num ={12,2,7,4,322,930,298,84,4,31};
//        int index = 8;
//        Point center = poly.center();
//        double[][] positions = getFromTXT(index);
//        Point[] result = new Point[num[index-1]];
//        double averX = 0;
//        double averY = 0;
//        double zoom = 1.0/5.0;
//        for (int o = 0; o < num[index-1]; o++) {
//            averX = zoom * averX+positions[o][0]/(double)num[index-1];
//            averY = zoom *  averX+positions[o][1]/(double)num[index-1] ;
//        }
//        for (int m = 0; m < num[index-1]; m++){
//            Point p = new Point(
//              zoom*positions[m][0]  -poly.leftmost().x(),
//              zoom*positions[m][1] -poly.upmost().y()
//            );
//            result[m] = p;
//        }
//
//        return result;
//    }

    public static double[][] getFromTXT(int index)  {
            int i = 0;
            int k = 0;
            int[] num ={12,2,7,4,322,930,298,84,4,31};
            double[][] results = new double[num[index-1]][2];
            try{
                String file = "/home/yangzhang/LBAI_mega_test/log.txt";
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                String[] name = new String[10];
                while(line != null) {
                    boolean status = line.contains("jpg");
                    if(status== true){
                        name[i] = line;
                        i++;
                        k = 0;
                    }
                    else {
                        if(i == index){
                            String[] strs = line.split("=");
                            double x = Double.parseDouble(strs[1].split(" ")[0]);
                            double y = Double.parseDouble(strs[2]);
                            results[k][0] = x;
                            results[k][1] = y;
                            k++;
                        }
                    }
                    line = reader.readLine();

                }
                reader.close();
                return results;
            } catch (IOException e) {
                e.printStackTrace();
            }
                return results;
    }



    public static Point[] clustered(int n, Polygon poly)  {
        Point[] result = new Point[n];

        int i = 0;
        while(i < n) {
            int clusterSize = Math.min((int) (Math.random()*8 + 2), n - i);
            double clusterWidth = poly.width() / 8 * Math.random() + 5;
            Point center = Pseudorandom(1, poly)[0];
            double mean = 0.5;
            double var = 1;
            for(int j = 0; j < clusterSize; j++){
                Point p = random(1, center.toSquare(clusterWidth))[0];

                if(! poly.contains(p)) j--;
                else {
                    result[i] = p;
                    i++;
                }
            }
        }
        return result;
    }

}