/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pso;

import java.util.List;

/**
 *
 * @author meimarcel
 */
public class StopCondition {
    public static boolean onlyIteration() {
        return true;
    }
    
    public static boolean acceptableError(double target, double error, double fit) {
        return (Math.abs(target - fit) > error);
    }
    
    public static boolean numberOfIterationImprovment(int window, double error, List<Double> averageMovementList) {
        if(averageMovementList.size() < window) {
            return true;
        } else {
            int start = averageMovementList.size() - window;
            int end = averageMovementList.size() - 1;
            for(; start < end; ++start) {
                if(Math.abs(averageMovementList.get(start) - averageMovementList.get(start + 1)) > error) {
                    return true;
                }
            }
            return false;
        }
    }
    
    public static boolean normalizedRadius(double beginDiameter, double maxRadius, double error) {
        return (Math.abs(maxRadius/beginDiameter) > error);
    }
    
    public static boolean functionSlope(int window, double error, List<Double> fitList) {
        if(fitList.size() < window) {
            return true;
        } else {
            int start = fitList.size() - window + 1;
            int end = fitList.size();
            for(; start < end; ++start) {
                if(Math.abs((fitList.get(start) - fitList.get(start - 1)) / fitList.get(start)) > error) {
                    return true;
                }
            }
            return false;
        }
    }
}
