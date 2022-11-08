package edu.missouri.frame;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.missouri.geom.Point;

import static java.lang.Double.POSITIVE_INFINITY;

public class QueueFuntions {

    public static Queue<Point> mergeQueue(Queue<Point> a, Queue<Point> b) {
        if (b.size() > 0) {
            while (!b.isEmpty()) {
                a.add(b.peek());
                b.remove();
            }
        }
        return a;
    }

    public static List<Point> pointQueueToList(Queue<Point> a){
        List<Point> list = new ArrayList<>();
        for(Point x : a){
            list.add(x);
        }
        return list;
    }
    public static List<Double> angleQueueToList(Queue<Double> a){
        List<Double> list = new ArrayList<>();
        while (!a.isEmpty()) {
            list.add(a.peek());
            a.remove();
        }
        return list;
    }

    public static Point findClosetPoint(Queue<Point> a, Point start){
        List<Point> list = pointQueueToList(a);
        Point newStart = new Point();
        double distance = POSITIVE_INFINITY;

        for(int i=0; i<list.size();i++){
            if(start.distance(list.get(i))<distance){
                distance = start.distance(list.get(i));
                newStart = list.get(i);
            }
        }
        return newStart;
    }

    public static int  pointToIndex(Queue<Point> a, Point point){
        List<Point> list = pointQueueToList(a);
        int index = 0;
        for(int i=0;i<list.size();i++){
            if(point.same(list.get(i))){
                index = i;
            }
        }
        return index;
    }

    public static Queue<Point> pointReSortQueue(Queue<Point> a, int index, Point startPoint) {
        List<Point> list = pointQueueToList(a);
        Queue<Point> newQueue = new LinkedList<Point>();
        int newStart= index;
        newQueue.add(startPoint);
        for(int i=newStart;i<list.size();i++){
            newQueue.add(list.get(i));
        }
        for(int j=0;j<newStart;j++){
            newQueue.add(list.get(j));
        }
        newQueue.add(startPoint);
        return newQueue;
    }

    public static Queue<Double> angleReSortQueue(Queue<Double> a, int index) {
        List<Double> list = angleQueueToList(a);
        Queue<Double> newQueue = new LinkedList<Double>();
        int newStart= index;
        newQueue.add(10000.0);
        for(int i=newStart;i<list.size();i++){
            newQueue.add(list.get(i));
        }
        for(int j=0;j<newStart;j++){
            newQueue.add(list.get(j));
        }
        newQueue.add(10000.0);
        return newQueue;
    }

}
