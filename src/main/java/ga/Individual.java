/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;

import java.util.Arrays;
import utils.Function;
import libeps.eps;
import libeps.param_fc;

/**
 *
 * @author meimarcel
 */
public class Individual implements Comparable<Individual> {
    private static double increment = 0;
    private double id;
    private double genes[];
    private double eval;
    private double probability;
    private int numerOfVariables;
    private Function function;
    
    
    public Individual(Function function, double[] genes, param_fc param, eps sep) {
        this.id = increment++;
        this.function = function;
        this.numerOfVariables = this.function.getNumberOfVariables();
        this.genes = genes;
        this.eval = this.function.fit(this.genes, param, sep);
        this.probability = 0;
    }
    
    public void calculateEval(param_fc param, eps sep) {
        this.eval = this.function.fit(this.genes, param, sep);
    }
     
    public int getNumberOfVariables() {
        return this.numerOfVariables;
    }
    
    public double getEval() {
        return this.eval;
    }
    
    public double getProbability() {
        return this.probability;
    }
    
    public void setProbability(double probability) {
        this.probability = probability;
    }
    
    public double[] getGenes() {
        return Arrays.copyOf(this.genes, this.numerOfVariables);
    }
    
    public void setGenes(double[] genes) {
        this.genes = genes;
    }
    
    public double getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Individual other = (Individual) obj;
        if (this.getId() != other.getId()) {
            return false;
        }
        return true;
    }
    
    @Override
    public int compareTo(Individual i) {
        return Double.compare(this.eval, i.eval); //To change body of generated methods, choose Tools | Templates.
    }
    
}
