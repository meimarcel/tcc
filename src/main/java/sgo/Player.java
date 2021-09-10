/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sgo;

import utils.Function;
import java.util.Arrays;
import java.util.Random;
import libeps.doubleArray;
import libeps.eps;
import libeps.param_fc;

/**
 * 
 * @author meimarcel
 */
public class Player implements Comparable<Player> {
    private double position[];
    private double bestPosition[];
    private double bestEval;
    private int numerOfVariables;
    private Function function;
    private static Random rand = null;
    
    public Player(Function function, doubleArray beginRange, doubleArray endRange, Random random, eps sep, param_fc param) {
        rand = random;
        
        this.function = function;
        this.numerOfVariables = this.function.getNumberOfVariables();
        this.position = new double[this.numerOfVariables];
        this.bestPosition = new double[this.numerOfVariables];
        
        this.setUniformDistribution(beginRange, endRange, this.numerOfVariables);
        this.bestPosition = Arrays.copyOf(this.position, this.numerOfVariables);
        this.bestEval = this.function.fit(this.position, param, sep);
    }
    
    public Player(Function function, double[] position, double bestEval, Random random) {
        rand = random;
        
        this.function = function;
        this.numerOfVariables = this.function.getNumberOfVariables();
        this.position = position;
        this.bestPosition = Arrays.copyOf(this.position, this.numerOfVariables);
        this.bestEval = bestEval;
    }
    
    
    private void setUniformDistribution(doubleArray beginRange, doubleArray endRange, int variables) {
        for(int i = 0; i < variables; ++i) {
            double begin = Math.min(beginRange.getitem(i), endRange.getitem(i));
            double end = Math.max(beginRange.getitem(i), endRange.getitem(i));
            this.position[i] = (rand.nextDouble() * (end - begin))+ begin;
        }
    }
    
    
    public void calculatePersonalBest(param_fc param, eps sep) {
        double eval = this.function.fit(this.position, param, sep);
        if(eval < this.bestEval) {
            this.bestEval = eval;
            this.bestPosition = Arrays.copyOf(this.position, this.position.length);
        }
    }
    
    public void setPosition(double[] position) {
        this.position = position;
    }
    
    public void setBestPosition(double[] bestPosition) {
        this.bestPosition = bestPosition;
    }
    
    public void setBestEval(double bestEval) {
        this.bestEval = bestEval;
    }
    
    public int getNumberOfVariables() {
        return this.numerOfVariables;
    }
    
    public double[] getPosition() {
        return Arrays.copyOf(this.position, this.position.length);
    }
    
    
    public double[] getBestPosition() {
        return Arrays.copyOf(this.bestPosition, this.position.length);
    }
    
    
    public double getBestEval() {
        return this.bestEval;
    }

    @Override
    public int compareTo(Player p) {
         return Double.compare(this.bestEval, p.bestEval);
    }
}
