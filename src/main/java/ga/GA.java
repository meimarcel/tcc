/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;
import utils.Function;
import utils.Logger;
import utils.Plotter;
import libeps.*;

/**
 *
 * @author meimarcel
 */
public class GA {
    
    public final static double DEFAULT_CROSSOVER_PROBABILITY = 0.6;
    public final static double DEFAULT_MUTATION_PROBABILITY = 0.05;
    public final static int DEFAULT_NUMBER_OF_POPULATION = 50;
    public final static int DEFAULT_ITERATION_LIMIT = 100;
    public final static int DEFAULT_ELITISM = 0;
    public final static CrossoverType DEFAULT_CROSSOVER_TYPE = CrossoverType.ONE_POINT;
    public final Logger LOGGER = new Logger();
    
    private boolean plotGraph = false;
    private int numberOfPopulation, iterationLimit;
    private doubleArray beginRange, endRange;
    private double crossoverProbability, mutationProbability;
    private int elitism;
    private Function function;
    private CrossoverType crossoverType;
    private StopConditionType stopCondition;
    private SelectionType selectionType;
    
    private double conditionTarget;
    private double conditionError;
    private int conditionWindow;
    private List<Double> conditionList;
    private double conditionFit;
    
    private Individual globalBestIndividual;
    private Individual localBestIndividual;
    private int bestIteration = 0;
    private double totalEval = -1;
    
    private eps sep;
    private param_fc param;
    
    private Random rand;
    
    public GA(Function function, SelectionType selectionType, eps sep, param_fc param) {
        this.numberOfPopulation = DEFAULT_NUMBER_OF_POPULATION;
        this.iterationLimit = DEFAULT_ITERATION_LIMIT;
        this.crossoverProbability = DEFAULT_CROSSOVER_PROBABILITY;
        this.mutationProbability = DEFAULT_MUTATION_PROBABILITY;
        this.elitism = DEFAULT_ELITISM;
        this.function = function;
        this.selectionType = selectionType;
        this.sep = sep;
        this.param = param;
        this.beginRange = new doubleArray(this.function.getNumberOfVariables());
        this.endRange = new doubleArray(this.function.getNumberOfVariables());
        libeps.lower_limits_decision_variables_optimal_reactive_dispatch(sep, beginRange.cast(), this.function.getNumberOfVariables());
        libeps.upper_limits_decision_variables_optimal_reactive_dispatch(sep, endRange.cast(), this.function.getNumberOfVariables());
    }
    
    public GA(int numberOfPopulation, int iterationLimit, double crossoverProbability, double mutationProbability, 
            int elitism, CrossoverType crossoverType, Function function, SelectionType selectionType, eps sep, param_fc param) {
        this.numberOfPopulation = numberOfPopulation;
        this.iterationLimit = iterationLimit;
        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
        this.elitism = elitism;
        this.crossoverType = crossoverType;
        this.function = function;
        this.selectionType = selectionType;
        this.sep = sep;
        this.param = param;
        this.beginRange = new doubleArray(this.function.getNumberOfVariables());
        this.endRange = new doubleArray(this.function.getNumberOfVariables());
        libeps.lower_limits_decision_variables_optimal_reactive_dispatch(sep, beginRange.cast(), this.function.getNumberOfVariables());
        libeps.upper_limits_decision_variables_optimal_reactive_dispatch(sep, endRange.cast(), this.function.getNumberOfVariables());
    }
    
    public String runGA() {
        StringBuilder log = new StringBuilder();
        long startTime = System.nanoTime();
        List<Double> dataFit = new ArrayList<>();
        List<double[]> dataMean = new ArrayList<>();
        List<double[]> dataStandardDeviation = new ArrayList<>();
        
        if(this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || this.stopCondition == StopConditionType.FUNCTION_SLOPE || this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION) {
            this.conditionList = new ArrayList<>();
        }
        
        Individual population[] = this.initialize();
        this.globalBestIndividual = population[0];
        this.bestIteration = 0;
        
        log.append("\n");
        log.append(LOGGER.white("---------------------------EXECUTING---------------------------\n"));
        int i = 0;
        for(; i < this.iterationLimit && this.stopConditionEvaluete(); ++i) {
            double individualDist[] = new double[function.getNumberOfVariables()];
            double standardDeviation[] =  new double[function.getNumberOfVariables()];
            Individual sortedPopulation[] = Arrays.copyOf(population, this.numberOfPopulation);
            Arrays.sort(sortedPopulation);
            this.localBestIndividual = sortedPopulation[0];
            
            log.append(LOGGER.white("Iteration :"+i+" BestEval = "+this.localBestIndividual.getEval()+" BestPositions = ["));
            dataFit.add(this.localBestIndividual.getEval());
            int end = this.localBestIndividual.getGenes().length - 1;
            double genes[] = this.localBestIndividual.getGenes();
            for(int j = 0; j < end; ++j) {
                log.append(LOGGER.white(genes[j]+", "));
            }
            log.append(LOGGER.white(genes[end]+"]\n"));
            
            if(this.globalBestIndividual.getEval() > this.localBestIndividual.getEval()) {
                this.globalBestIndividual = this.localBestIndividual;
                this.bestIteration = i;
            }
            
            this.conditionFit = this.localBestIndividual.getEval();
            if(this.stopCondition == StopConditionType.FUNCTION_SLOPE || this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT) {
                this.conditionList.add(this.localBestIndividual.getEval());
            }
            
            for(Individual individual : population) {
                genes = individual.getGenes();
                for(int j = 0; j < individualDist.length; ++j) {
                    individualDist[j] += genes[j];
                }
            }
            
            for(int j = 0; j < individualDist.length; ++j) {
                individualDist[j] = individualDist[j] / (double) this.numberOfPopulation;
            }
            dataMean.add(individualDist);
            
            for(Individual individual : population) {
                genes = individual.getGenes();
                for(int j = 0; j < genes.length; ++j) {
                    standardDeviation[j] += ((genes[j] - individualDist[j]) * (genes[j] - individualDist[j])); 
                }
            }
            
            for(int j = 0; j < standardDeviation.length; ++j) {
                standardDeviation[j] = Math.sqrt(standardDeviation[j] / (double) this.numberOfPopulation);
            }
            dataStandardDeviation.add(standardDeviation);
            
            Individual newPopulation[] = new Individual[this.numberOfPopulation];
            int index = 0;
            
            for(; index < this.elitism; ++index) {
                newPopulation[index] = sortedPopulation[index];
            }
            
            while(index < this.numberOfPopulation) {
                Individual parent1 = this.select(population, sortedPopulation[0]);
                Individual parent2 = this.select(population, sortedPopulation[0]);
                while(parent1.equals(parent2)) {
                    parent2 = this.select(population, sortedPopulation[0]);
                }
                Individual children[]  = this.crossover(parent1, parent2, i);
                
                for(int j = 0; j < children.length; ++j) {
                    children[j] = this.mutate(children[j]);
                }
                
                newPopulation[index++] = children[0];
                if(children.length == 2 && index < this.numberOfPopulation) {
                    newPopulation[index++] = children[1];
                }
            }
            this.totalEval = -1;
            
            if(this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION) {
                double sumDist = 0;
                Individual sortedNewPopulation[] = Arrays.copyOf(newPopulation, newPopulation.length);
                Arrays.sort(sortedNewPopulation);
                for(int j = 0; j < this.numberOfPopulation; ++j) {    
                    double dist = 0;
                    double genesOld[] = sortedPopulation[j].getGenes();
                    double genesNew[] = sortedNewPopulation[j].getGenes();
                    for(int k = 0; k < this.function.getNumberOfVariables(); ++k) {
                         dist += ((genesOld[k] - genesNew[k]) * (genesOld[k] - genesNew[k]));
                    }
                    dist = Math.sqrt(dist);
                    sumDist += dist;
                }
                this.conditionList.add(sumDist / (double) this.numberOfPopulation);
            }
            
            population = Arrays.copyOf(newPopulation, this.numberOfPopulation);
            
        }
        double individualDist[] = new double[this.function.getNumberOfVariables()];
        double standardDeviation[] =  new double[this.function.getNumberOfVariables()];
        
        log.append(LOGGER.white("Iteration :"+i+" BestEval = "+this.localBestIndividual.getEval()+" BestPositions = ["));
        dataFit.add(this.localBestIndividual.getEval());
        int end = this.localBestIndividual.getGenes().length - 1;
        double genes[] = this.localBestIndividual.getGenes();
        for(int j = 0; j < end; ++j) {
            log.append(LOGGER.white(genes[j]+", "));
        }
        log.append(LOGGER.white(genes[end]+"]\n"));
        
        for(Individual individual : population) {
            genes = individual.getGenes();
            for(int j = 0; j < genes.length; ++j) {
                individualDist[j] += genes[j];
            }
        }
        for(int j = 0; j < individualDist.length; ++j) {
            individualDist[j] = individualDist[j] / (double) this.numberOfPopulation;
        }
        dataMean.add(individualDist);
        
        for(Individual individual : population) {
            genes = individual.getGenes();
            for(int j = 0; j < genes.length; ++j) {
                standardDeviation[j] += ((genes[j] - individualDist[j]) * (genes[j] - individualDist[j])); 
            }
        }
        for(int j = 0; j < standardDeviation.length; ++j) {
            standardDeviation[j] = Math.sqrt(standardDeviation[j] / (double) this.numberOfPopulation);
        }
        dataStandardDeviation.add(standardDeviation);
        
        long stopTime = System.nanoTime();
        
        System.out.println("");
        log.append("\n");
        log.append(LOGGER.message("---------------------------RESULT---------------------------\n"));
        log.append(LOGGER.message("Iteration = "+this.bestIteration+"\n"));
        log.append(LOGGER.message("Best Eval = "+this.globalBestIndividual.getEval()+"\n"));
        log.append(LOGGER.message("Final Best Positions = ["));
        genes = this.globalBestIndividual.getGenes();
        end = genes.length - 1;
        System.out.print(LOGGER.ANSI_CYAN);
        for(int j = 0; j < end; ++j) {
            log.append(genes[j]).append(", ");
            System.out.print(genes[j]+", ");
        }
        log.append(genes[end]).append("]\n");
        System.out.print(genes[end]+"]\n");
        log.append(LOGGER.message("Execution time: "+ ((stopTime - startTime) / 1000000) + " ms\n"));
        log.append(LOGGER.message("---------------------------COMPLETE---------------------------\n"));
        
        if(this.plotGraph) {
            Plotter plotter = new Plotter(dataFit, dataMean, dataStandardDeviation);
            plotter.start();
        }
        
        return log.toString();
    }
    
    public String runGAFile(int testNumber) {
        StringBuilder log = new StringBuilder();
                
        if(this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || this.stopCondition == StopConditionType.FUNCTION_SLOPE || this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION) {
            this.conditionList = new ArrayList<>();
        }
        
        Individual population[] = this.initialize();
        this.globalBestIndividual = population[0];
        this.bestIteration = 0;
        
        long startTime = System.nanoTime();
        int i = 0;
        for(; i < this.iterationLimit && this.stopConditionEvaluete(); ++i) {
            Individual sortedPopulation[] = Arrays.copyOf(population, this.numberOfPopulation);
            Arrays.sort(sortedPopulation);
            this.localBestIndividual = sortedPopulation[0];
            
            if(this.globalBestIndividual.getEval() > this.localBestIndividual.getEval()) {
                this.globalBestIndividual = this.localBestIndividual;
                this.bestIteration = i;
            }
            
            this.conditionFit = this.localBestIndividual.getEval();
            if(this.stopCondition == StopConditionType.FUNCTION_SLOPE || this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT) {
                this.conditionList.add(this.localBestIndividual.getEval());
            }
            
            Individual newPopulation[] = new Individual[this.numberOfPopulation];
            int index = 0;
            
            for(; index < this.elitism; ++index) {
                newPopulation[index] = sortedPopulation[index];
            }
            
            while(index < this.numberOfPopulation) {
                Individual parent1 = this.select(population, sortedPopulation[0]);
                Individual parent2 = this.select(population, sortedPopulation[0]);
                while(parent1.equals(parent2)) {
                    parent2 = this.select(population, sortedPopulation[0]);
                }
                Individual children[]  = this.crossover(parent1, parent2, i);
                
                for(int j = 0; j < children.length; ++j) {
                    children[j] = this.mutate(children[j]);
                }
                
                newPopulation[index++] = children[0];
                if(children.length == 2 && index < this.numberOfPopulation) {
                    newPopulation[index++] = children[1];
                }
            }
            this.totalEval = -1;
            
            if(this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION) {
                double sumDist = 0;
                Individual sortedNewPopulation[] = Arrays.copyOf(newPopulation, newPopulation.length);
                Arrays.sort(sortedNewPopulation);
                for(int j = 0; j < this.numberOfPopulation; ++j) {    
                    double dist = 0;
                    double genesOld[] = sortedPopulation[j].getGenes();
                    double genesNew[] = sortedNewPopulation[j].getGenes();
                    for(int k = 0; k < this.function.getNumberOfVariables(); ++k) {
                         dist += ((genesOld[k] - genesNew[k]) * (genesOld[k] - genesNew[k]));
                    }
                    dist = Math.sqrt(dist);
                    sumDist += dist;
                }
                this.conditionList.add(sumDist / (double) this.numberOfPopulation);
            }
            
            population = Arrays.copyOf(newPopulation, this.numberOfPopulation);
            
        }  
        long stopTime = System.nanoTime();
        log.append(LOGGER.message("Test: "+testNumber+"\n"));
        log.append(LOGGER.message("Best Iteration = "+this.bestIteration+"\n"));
        log.append(LOGGER.message("Best Eval = "+this.globalBestIndividual.getEval()+"\n"));
        log.append(LOGGER.message("Final Best Positions = ["));
        double[] genes = this.globalBestIndividual.getGenes();
        int end = genes.length - 1;
        System.out.print(LOGGER.ANSI_CYAN);
        for(int j = 0; j < end; ++j) {
            log.append(genes[j]).append(", ");
            System.out.print(genes[j]+", ");
        }
        log.append(genes[end]).append("]\n");
        System.out.print(genes[end]+"]\n");
        log.append(LOGGER.message("Execution time: "+ ((stopTime - startTime) / 1000000) + " ms\n"));
        
        return log.toString();
    }
    
    
    private double[] setUniformDistribution() {
        double[] genes = new double[this.function.getNumberOfVariables()];
        for(int i = 0; i < this.function.getNumberOfVariables(); ++i) {
            double begin = Math.min(beginRange.getitem(i), endRange.getitem(i));
            double end = Math.max(beginRange.getitem(i), endRange.getitem(i));
            genes[i] = (rand.nextDouble() * (end - begin))+ begin;
        }
        return genes;
    }
    
    
    private Individual[] initialize() {
        Individual[] population = new Individual[this.numberOfPopulation];
        for(int i = 0; i < this.numberOfPopulation; ++i) {
            double[] genes = this.setUniformDistribution();
            population[i] = new Individual(this.function, genes, param, sep);
        }
        
        return population;
    }
    
    private Individual[] crossover(Individual parent1, Individual parent2, int iteration) {
        Individual[] children = new Individual[2];
        double[] parent1Genes = parent1.getGenes();
        double[] parent2Genes = parent2.getGenes();
        
        if(rand.nextDouble() <= this.crossoverProbability) {
            if(this.crossoverType == CrossoverType.ARITHMETIC_MEAN) {
                children = new Individual[1];
                        
                double[] genes = new double[this.function.getNumberOfVariables()];
                for(int i = 0; i < genes.length; ++i) {
                    genes[i] = (parent1Genes[i] + parent2Genes[i]) / 2;
                }
                children[0] = new Individual(this.function, genes, param, sep);
            
            } else if(this.crossoverType == CrossoverType.WEIGHTED_MEAN) {
                double beta = rand.nextDouble();
                double[] genes = new double[this.function.getNumberOfVariables()];
                for(int i = 0; i < genes.length; ++i) {
                    genes[i] = (beta * parent1Genes[i]) + ((1 - beta) * parent2Genes[i]);
                }
                children[0] = new Individual(this.function, genes, param, sep);
                
                for(int i = 0; i < genes.length; ++i) {
                    genes[i] = ((1 - beta) * parent1Genes[i]) + (beta * parent2Genes[i]);
                }
                children[1] = new Individual(this.function, genes, param, sep);
                
            } else if(this.crossoverType == CrossoverType.ONE_POINT) {
                int point = rand.nextInt(this.function.getNumberOfVariables());
                double[] genes;
                genes = ArrayUtils.addAll(
                        Arrays.copyOfRange(parent1Genes, 0, point+1), 
                        Arrays.copyOfRange(parent2Genes, point+1, this.function.getNumberOfVariables()));
                children[0] = new Individual(this.function, genes, param, sep);
                
                genes = ArrayUtils.addAll(
                        Arrays.copyOfRange(parent2Genes, 0, point+1), 
                        Arrays.copyOfRange(parent1Genes, point+1, this.function.getNumberOfVariables()));
                children[1] = new Individual(this.function, genes, param, sep);
            
            } else if(this.crossoverType == CrossoverType.TWO_POINT) {
                int point1 = rand.nextInt(this.function.getNumberOfVariables());
                int point2 = rand.nextInt(this.function.getNumberOfVariables());
                int begin  = Math.min(point1, point2);
                int end    = Math.max(point1, point2);
                
                double[] genes;
                genes = ArrayUtils.addAll(
                            ArrayUtils.addAll(
                                Arrays.copyOfRange(parent1Genes, 0, begin),
                                Arrays.copyOfRange(parent2Genes, begin, end+1)
                            ), 
                            Arrays.copyOfRange(parent1Genes, end+1, this.function.getNumberOfVariables())
                        );
                children[0] = new Individual(this.function, genes, param, sep);
                
                genes = ArrayUtils.addAll(
                            ArrayUtils.addAll(
                                Arrays.copyOfRange(parent2Genes, 0, begin),
                                Arrays.copyOfRange(parent1Genes, begin, end+1)
                            ), 
                            Arrays.copyOfRange(parent2Genes, end+1, this.function.getNumberOfVariables())
                        );
                children[1] = new Individual(this.function, genes, param, sep);
                
            }
        } else {
            children[0] = new Individual(this.function, parent1Genes, param, sep);
            children[1] = new Individual(this.function, parent2Genes, param, sep);
        }
        
        return children;
    }
    
    private Individual mutate(Individual individual) {
        double[] genes = individual.getGenes();
        for(int i = 0; i < genes.length; ++i) {
            if(rand.nextDouble() <= this.mutationProbability) {
                genes[i] *= rand.nextGaussian();
            }
        }
        
        for(int k = 0; k < this.function.getNumberOfVariables(); ++k) {
            double begin = Math.min(beginRange.getitem(k), endRange.getitem(k));
            double end = Math.max(beginRange.getitem(k), endRange.getitem(k));
            if(begin > genes[k]) {
                genes[k] = begin;
            } else if(end < genes[k]) {
                genes[k] = end;
            }
        }
        
        individual.setGenes(genes);
        individual.calculateEval(param, sep);
        return individual;
    }
    
    private Individual select(Individual[] population, Individual minIndividual) {
        switch(this.selectionType) {
            case ROULETTE_WHEEL:
                return this.rouletteWheel(population, minIndividual);
            case TOURNAMENT:
                return this.tournament(population);
            default :
                return this.tournament(population);
        }
    }
    
    private Individual rouletteWheel(Individual[] population, Individual minIndividual) {
        if(this.totalEval == -1) {
            this.totalEval = 0;
            for(Individual i : population) {
                this.totalEval += (1 / (1 + i.getEval() - minIndividual.getEval()));
            }
            for(int i = 0; i < this.numberOfPopulation; ++i) {
                population[i].setProbability((1 / (1 + population[i].getEval() - minIndividual.getEval())) / this.totalEval);
            }
        }
        double probability = rand.nextDouble();
        int i = 0;
        double sum = population[i].getProbability();
        while(sum < probability) {
            ++i;
            sum += population[i].getProbability();
        }
        
        return population[i];
        
    }
    
    private Individual tournament(Individual[] population) {
        int index1 = rand.nextInt(this.numberOfPopulation);
        int index2 = rand.nextInt(this.numberOfPopulation);
        while(index1 == index2) {
            index2 = rand.nextInt(this.numberOfPopulation);
        }
        Individual i1 = population[index1];
        Individual i2 = population[index2];
        
        return i1.getEval() < i2.getEval() ? i1 : i2;
    }
    
    public double NUM(double t, double y) {
        double exp = (1 - (t/(double)this.iterationLimit));
        return y * (1.0 - Math.pow(rand.nextDouble(), exp));
    }
    
    public void setPlotGraph(boolean plotGraph) {
        this.plotGraph = plotGraph;
    }
    
    public void setStopConditionType(StopConditionType stopCondition) {
        this.stopCondition = stopCondition;
    }
    
    public void setConditionError(double error) {
        this.conditionError = error;
    }
    
    public void setConditionTarget(double target) {
        this.conditionTarget = target;
    }
    
    public void setConditionWindow(int window) {
        this.conditionWindow = window;
    }
    
    public void setRandom(Random random) {
        rand = random;
    }
    
    public double getGlobalBestEval() {
        return this.globalBestIndividual.getEval();
    }
    
    private boolean stopConditionEvaluete() {
        switch(this.stopCondition) {
            case ONLY_ITERATION:
                return StopCondition.onlyIteration();
            case ACCEPTABLE_ERROR:
                return StopCondition.acceptableError(this.conditionTarget, this.conditionError, this.conditionFit);
            case NUMBER_OF_ITERATION_IMPROVEMENT:
                return StopCondition.numberOfIterationImprovment(this.conditionWindow, this.conditionError, this.conditionList);
            case NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION:
                return StopCondition.numberOfIterationImprovmentPopulation(this.conditionWindow, this.conditionError, this.conditionList);
            case FUNCTION_SLOPE:
                return StopCondition.functionSlope(this.conditionWindow, this.conditionError, this.conditionList);
            default:
                return StopCondition.onlyIteration();
        }
    }
    
    
    public enum CrossoverType {
        ARITHMETIC_MEAN,
        WEIGHTED_MEAN,
        ONE_POINT,
        TWO_POINT;
    }
    
        
    public enum StopConditionType {
        ONLY_ITERATION,
        ACCEPTABLE_ERROR,
        NUMBER_OF_ITERATION_IMPROVEMENT,
        NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION,
        FUNCTION_SLOPE;
    }
    
    public enum SelectionType {
        ROULETTE_WHEEL,
        TOURNAMENT;
    }
    
}
