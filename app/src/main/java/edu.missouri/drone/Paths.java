package edu.missouri.drone;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.ArrayList;
import java.util.List;

import edu.missouri.frame.Option;
import edu.missouri.geom.Point;


public class Paths {
    private List<Point> wayPoints;
    private  int startIndex;
    private  int endIndex;

    public Paths(List<Point> wayPoints, int index, int endIndex){
        this.wayPoints = wayPoints;
        this.startIndex = index;
        this.endIndex = endIndex;
    }

    public double getLength(){
        double length = 0;
        for(int i = startIndex+1;i<=endIndex;i++){
            WayPointCheck pointCheck = new WayPointCheck(wayPoints,i);
//            if (pointCheck.isBankedTurning()){
//                length += pointCheck.getBankedTurningLength(Option.cruiseSpeed);
//            }
//            else {
//                length += wayPoints.get(i-1).distance(wayPoints.get(i));
//            }
            length += wayPoints.get(i-1).distance(wayPoints.get(i));
        }
        return length;
    }

    public List<Double> getLengthPerWayPoint(){
        List<Double> result = new ArrayList<>();
        double length = 0.0;
        result.add(length);
        for(int i = startIndex+1;i<=endIndex;i++){
            WayPointCheck pointCheck = new WayPointCheck(wayPoints,i);
//            if (pointCheck.isBankedTurning()){
//                length += pointCheck.getBankedTurningLength(Option.cruiseSpeed);
//                result.add(length);
//            }
//            else {
//                length += wayPoints.get(i-1).distance(wayPoints.get(i));
//                result.add(length);
//            }
            length += wayPoints.get(i-1).distance(wayPoints.get(i));
            result.add(length);
        }
        return result;

    }

    public double[] getAccelarationTime(){
        double[] t = new double[]{0.0,5,10,15,20,25,30,35};
        double[] v = new double[]{0.0,5.0,8.5,12.0,14.0,15.0,15.2,15.3};
        WeightedObservedPoints points = new WeightedObservedPoints();
        for(int i=0;i<v.length;i++){
            points.add(v[i],t[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        double[] result = fitter.fit(points.toList());
        return result;
    }

    public double[] getAccelarationSpeed(){
        double[] t = new double[]{0.0,5,10,15,20,25,30,35};
        double[] v = new double[]{0.0,5.0,8.5,12.0,14.0,15.0,15.2,15.3};
        WeightedObservedPoints points = new WeightedObservedPoints();
        for(int i=0;i<v.length;i++){
            points.add(t[i],v[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        double[] result = fitter.fit(points.toList());
        return result;
    }

    public double[] getAccelarationPowver(){
        double[] t = new double[]{0.0,5,10,15,20,25,30,35};
        double[] p = new double[]{200.0,210.0,230.0,240.0,250.0,275.0,300.2,325.3};
        WeightedObservedPoints points = new WeightedObservedPoints();
        for(int i=0;i<t.length;i++){
            points.add(t[i],p[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        double[] result = fitter.fit(points.toList());
        return result;
    }

    public double getAccelarationDistance(){
        double maxSpeed = getOptimalSpeed();
        SimpsonIntegrator simpson = new SimpsonIntegrator();
        double[] speeds = getAccelarationTime();
        double time = speeds[0] + speeds[1]*maxSpeed +speeds[2]* Math.pow(maxSpeed,2);
        double[] vector = getAccelarationSpeed();
        PolynomialFunction f = new PolynomialFunction(vector);
        UnivariateFunction uf = (UnivariateFunction)new PolynomialFunction(vector);
        double i = simpson.integrate(10000, f, 0 , time);
        return i;
    }

    public double getAccelarationEnergy(){
        double maxSpeed = getOptimalSpeed();
        SimpsonIntegrator simpson = new SimpsonIntegrator();
        double[] speeds = getAccelarationTime();
        double time = speeds[0] + speeds[1]*maxSpeed +speeds[2]* Math.pow(maxSpeed,2);
        double[] vector = getAccelarationPowver();
        PolynomialFunction f = new PolynomialFunction(vector);
        UnivariateFunction uf = (UnivariateFunction)new PolynomialFunction(vector);
        double i = simpson.integrate(10000,f,0,time);
        return i;
    }

    public double[] getDecelarationTime(){
        double[] t = new double[]{0,2,4,6,8,10,12,14,15};
        double[] v = new double[]{15.0,14.0,12.0,9.5,7.5,6.0,3.0,1.0,0.1};
        WeightedObservedPoints points = new WeightedObservedPoints();
        for(int i=0;i<v.length;i++){
            points.add(v[i],t[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        double[] result = fitter.fit(points.toList());
        return result;
    }

    public double[] getDecelarationSpeed(){
        double[] t = new double[]{0,2,4,6,8,10,12,14,15};
        double[] v = new double[]{15.0,14.0,12.0,9.5,7.5,6.0,3.0,1.0,0.1};
        WeightedObservedPoints points = new WeightedObservedPoints();
        for(int i=0;i<v.length;i++){
            points.add(t[i],v[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        double[] result = fitter.fit(points.toList());
        return result;
    }

    public double[] getDecelarationPowver(){
        double[] t = new double[]{0.0,2,4,6,7,8,10,12,14,15};
        double[] p = new double[]{310.0,260.0,220.0,210.0,212.0,215.0,225.0,230.2,235.0,230.0};
        WeightedObservedPoints points = new WeightedObservedPoints();
        for(int i=0;i<t.length;i++){
            points.add(t[i],p[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
        double[] result = fitter.fit(points.toList());
        return result;
    }

    public double getDecelarationEnergy(){
        double maxSpeed = getOptimalSpeed();
        SimpsonIntegrator simpson = new SimpsonIntegrator();
        double[] speeds = getDecelarationTime();
        double time = speeds[0] + speeds[1]*maxSpeed +speeds[2]* Math.pow(maxSpeed,2);
        double[] vector = getDecelarationPowver();
        PolynomialFunction f = new PolynomialFunction(vector);
        UnivariateFunction uf = (UnivariateFunction)new PolynomialFunction(vector);
        double i = simpson.integrate(10000,f,time,15.5);
        return i;
    }

    public double getDecelarationDistance(){
        double maxSpeed = getOptimalSpeed();
        SimpsonIntegrator simpson = new SimpsonIntegrator();
        double[] speeds = getDecelarationTime();
        double time = speeds[0] + speeds[1]*maxSpeed +speeds[2]* Math.pow(maxSpeed,2);
        double[] vector = getDecelarationSpeed();
        PolynomialFunction f = new PolynomialFunction(vector);
        UnivariateFunction uf = (UnivariateFunction)new PolynomialFunction(vector);
        double i = simpson.integrate(10000, f, time , 15);
        return i;
    }

    public double getConstantSpeedDistance(){
        return getLength() - getAccelarationDistance() - getDecelarationDistance();
    }

    public double getConstantSpeedPower(double speed){
        double[] v = {0,2,4,6,8,10,12,14,16};
        double[] power = {222.0,220.0,215.0,210.0,205.0,215.0,235.0,280.0,340.0};
        WeightedObservedPoints points = new WeightedObservedPoints();
        for(int i=0;i<v.length;i++){
            points.add(v[i],power[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
        double[] vector = fitter.fit(points.toList());
        return vector[0] + speed*vector[1] + Math.pow(speed,2) * vector[2] + Math.pow(speed,3) * vector[3];
    }

    public double getConstantSpeedEnergy(){
        double length = getConstantSpeedDistance();
        double optimalSpeed = getOptimalSpeed();
        double flyingTime = length/optimalSpeed;
        return getConstantSpeedPower(optimalSpeed) * flyingTime;
    }

    public double getTotalEnergy(){
        return getAccelarationEnergy()+getDecelarationEnergy()+getConstantSpeedEnergy();
    }

    public List<Double> getSpeed(){
        double optimalSpeed = getOptimalSpeed();
        List<Double> speedList = new ArrayList<>();
        double accDistance = getAccelarationDistance();
        double decDistance = getDecelarationDistance();
        double distanceStartToDec = getLength() - decDistance;
        int indexOfWayPointEndAcc = 0;
        int indexOfWayPointStartDec = 0;
        List<Double> distancePerWaypoints = getLengthPerWayPoint();

        for(int i = 0;i< distancePerWaypoints.size();i++){
            if (distancePerWaypoints.get(i)>accDistance){
                indexOfWayPointEndAcc = startIndex + i;
            }
        }

        for(int i = 0;i< distancePerWaypoints.size();i++){
            if (distancePerWaypoints.get(i)>distanceStartToDec){
                indexOfWayPointStartDec = startIndex + i -1;
            }
        }

        for(int i=startIndex;i<indexOfWayPointEndAcc;i++){
                speedList.add(Math.sqrt(2* Option.accelaration*distancePerWaypoints.get(i-startIndex)));
            }

        if (indexOfWayPointEndAcc<=indexOfWayPointStartDec){
            for(int i=indexOfWayPointEndAcc;i<=indexOfWayPointStartDec;i++){
                speedList.add(optimalSpeed);
            }
        }
            for(int i=indexOfWayPointStartDec+1;i<=endIndex;i++){
                speedList.add(Math.sqrt(2* Option.decelaration*(getLength()-distancePerWaypoints.get(i-startIndex))));
            }

        return speedList;
    }

    public double getSpeedByWaypoint(int index){
        List<Double> speedList = getSpeed();
        return speedList.get(index);
    }

    public double getOptimalSpeed(){
        double[] x = new double[]{10,50,100,150,300,600,1200,3000};
        double[] y = new double[]{1.0,3.2,6.5,8.0,10.8,11.8,12.0,12.2};
        WeightedObservedPoints points = new WeightedObservedPoints();
        for(int i=0;i<x.length;i++){
            points.add(x[i],y[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        double[] result = fitter.fit(points.toList());
        return Math.pow(getLength(),2) * result[2] + getLength() * result[1] +result[0];
    }

}
