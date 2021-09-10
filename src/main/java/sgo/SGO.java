/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sgo;

import utils.Function;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import libeps.eps;
import libeps.libeps;
import libeps.param_fc;
import libeps.doubleArray;
import utils.Logger;
import utils.Plotter;

/**
 *
 * @author meimarcel
 */
public class SGO {
    public final static double DEFAULT_INERTIA_WEIGHT = 0.191;
    public final static double DEFAULT_COGNITIVE_WEIGHT = 0.191;
    public final static double DEFAULT_SOCIAL_WEIGHT = 0.618;
    public final static int DEFAULT_BEGIN_RANGE = -100;
    public final static int DEFAULT_END_RANGE = 100;
    public final static double DEFAULT_MOVE_OFF_PROBABILITY = 0.2;
    public final static double DEFAULT_MOVE_FORWARD_AFTER_MOVE_OFF = 0.05;
    public final static double DEFAULT_SUBSTITUTION_PROBABILITY = 0.1;
    public final static double DEFAULT_NON_UNIFORMITY_DEGREE = 1;
    public final Logger LOGGER = new Logger();
    
    private boolean plotGraph = false;
    private int playerNumber, substituteNumber, kicksLimit;
    private double moveOffProbability, moveForwardAfterMoveOffProbability, substitutionProbability, nonUniformityDegree;
    private double inertiaWeight, cognitiveWeight, socialWeight;
    private doubleArray beginRange, endRange;
    private double[] globalBestPosition;
    private double globalBestEval = Integer.MAX_VALUE;
    private Function function;
    
    private StopConditionType stopCondition;
    private double conditionTarget;
    private double conditionError;
    private int conditionWindow;
    private List<Double> conditionList;
    private double conditionFit;
    
    private eps sep;
    private param_fc param;
    
    private static Random rand;
    
    
    public SGO(int playerNumber, int substituteNumber, int kicksLimit, Function function, eps sep, param_fc param) {
        if(rand == null) {
            rand = new Random();
        }
        
        this.playerNumber = playerNumber;
        this.substituteNumber = substituteNumber;
        this.kicksLimit = kicksLimit;
        this.function = function;
        this.inertiaWeight = DEFAULT_INERTIA_WEIGHT;
        this.cognitiveWeight = DEFAULT_COGNITIVE_WEIGHT;
        this.socialWeight = DEFAULT_SOCIAL_WEIGHT;
        this.sep = sep;
        this.param = param;
        this.beginRange = new doubleArray(this.function.getNumberOfVariables());
        this.endRange = new doubleArray(this.function.getNumberOfVariables());
        libeps.lower_limits_decision_variables_optimal_reactive_dispatch(sep, beginRange.cast(), this.function.getNumberOfVariables());
        libeps.upper_limits_decision_variables_optimal_reactive_dispatch(sep, endRange.cast(), this.function.getNumberOfVariables());
        this.moveOffProbability = DEFAULT_MOVE_OFF_PROBABILITY;
        this.moveForwardAfterMoveOffProbability = DEFAULT_MOVE_FORWARD_AFTER_MOVE_OFF;
        this.substitutionProbability = DEFAULT_SUBSTITUTION_PROBABILITY;
        this.nonUniformityDegree = DEFAULT_NON_UNIFORMITY_DEGREE;
        
    }
    
    public SGO(int playerNumber, int substituteNumber, int kicksLimit, double inertiaWeight, 
            double cognitiveWeight, double socialWeight, Function function, eps sep, param_fc param) {
        if(rand == null) {
            rand = new Random();
        }
        
        this.playerNumber = playerNumber;
        this.substituteNumber = substituteNumber;
        this.kicksLimit = kicksLimit;
        this.function = function;
        this.inertiaWeight = inertiaWeight;
        this.cognitiveWeight = cognitiveWeight;
        this.socialWeight = socialWeight;
        this.sep = sep;
        this.param = param;
        this.beginRange = new doubleArray(this.function.getNumberOfVariables());
        this.endRange = new doubleArray(this.function.getNumberOfVariables());
        libeps.lower_limits_decision_variables_optimal_reactive_dispatch(sep, beginRange.cast(), this.function.getNumberOfVariables());
        libeps.upper_limits_decision_variables_optimal_reactive_dispatch(sep, endRange.cast(), this.function.getNumberOfVariables());
        this.moveOffProbability = DEFAULT_MOVE_OFF_PROBABILITY;
        this.moveForwardAfterMoveOffProbability = DEFAULT_MOVE_FORWARD_AFTER_MOVE_OFF;
        this.substitutionProbability = DEFAULT_SUBSTITUTION_PROBABILITY;
        this.nonUniformityDegree = DEFAULT_NON_UNIFORMITY_DEGREE;
    }
    
    public SGO(int playerNumber, int substituteNumber, int kicksLimit, double inertiaWeight, 
            double cognitiveWeight, double socialWeight, Function function,
            double moveOffProbability, double moveForwardAfterMoveOffProbability,
            double substitutionProbability, double nonUniformityDegree, eps sep, param_fc param) {
        if(rand == null) {
            rand = new Random();
        }
        
        this.playerNumber = playerNumber;
        this.substituteNumber = substituteNumber;
        this.kicksLimit = kicksLimit;
        this.function = function;
        this.inertiaWeight = inertiaWeight;
        this.cognitiveWeight = cognitiveWeight;
        this.socialWeight = socialWeight;
        this.sep = sep;
        this.param = param;
        this.beginRange = new doubleArray(this.function.getNumberOfVariables());
        this.endRange = new doubleArray(this.function.getNumberOfVariables());
        libeps.lower_limits_decision_variables_optimal_reactive_dispatch(sep, beginRange.cast(), this.function.getNumberOfVariables());
        libeps.upper_limits_decision_variables_optimal_reactive_dispatch(sep, endRange.cast(), this.function.getNumberOfVariables());
        this.moveOffProbability = moveOffProbability;
        this.moveForwardAfterMoveOffProbability = moveForwardAfterMoveOffProbability;
        this.substitutionProbability = substitutionProbability;
        this.nonUniformityDegree = nonUniformityDegree;
    }
    
    public String runSGO() {
        StringBuilder log = new StringBuilder();
        List<Double> dataFit = new ArrayList<>();
        List<double[]> dataMean = new ArrayList<>();
        List<double[]> dataStandardDeviation = new ArrayList<>();
        
        
        if(this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || this.stopCondition == StopConditionType.FUNCTION_SLOPE) {
            this.conditionList = new ArrayList<>();
        }
        
        Player[] players = this.initialize();
        List<Player> substitutes = this.initializeSubstitutes(players);
        
        log.append("\n");
        log.append(LOGGER.white("---------------------------EXECUTING---------------------------\n"));
        long startTime = System.nanoTime();
        int i = 0;
        for(; i < this.kicksLimit && this.stopConditionEvaluete(); ++i) {
            double playerDist[] = new double[function.getNumberOfVariables()];
            double standardDeviation[] =  new double[function.getNumberOfVariables()];
            
            log.append(LOGGER.white("Iteration :"+i+" BestEval = "+this.globalBestEval+" BestPositions = ["));
            dataFit.add(this.globalBestEval);
            int end = this.globalBestPosition.length - 1;
            for(int j = 0; j < end; ++j) {
                log.append(LOGGER.white(this.globalBestPosition[j]+", "));
            }
            log.append(LOGGER.white(this.globalBestPosition[end]+"]\n"));
            
            
            for(Player p : players) {
                double position[] = p.getPosition();
                for(int j = 0; j < playerDist.length; ++j) {
                    playerDist[j] += position[j];
                }
            }
            
            for(int j = 0; j < playerDist.length; ++j) {
                playerDist[j] = playerDist[j] / (double) this.playerNumber;
            }
            dataMean.add(playerDist);
            
            double sumPositions = 0;
            for(Player p : players) {
                double personalBestPosition[] = p.getBestPosition();
                double position[] = p.getPosition();
                
                for(int j = 0; j < position.length; ++j) {                
                    standardDeviation[j] += (position[j] - playerDist[j]) * (position[j] - playerDist[j]); 
                } 
                
                if(rand.nextDouble() <= this.moveOffProbability) {
                    p.setPosition(this.moveOff(position, i));
                    if(rand.nextDouble() <= this.moveForwardAfterMoveOffProbability) {
                        p.setPosition(this.moveForward(p.getPosition(), personalBestPosition));
                    }
                } else {
                    p.setPosition(this.moveForward(position, personalBestPosition));
                }
                
                double currentPosition[] = p.getPosition();
                boolean changed = false;
                for(int j = 0; j < this.function.getNumberOfVariables(); ++j) {
                    double beginR = Math.min(beginRange.getitem(j), endRange.getitem(j));
                    double endR = Math.max(beginRange.getitem(j), endRange.getitem(j));
                    if(beginR > currentPosition[j]) {
                        changed = true;
                        currentPosition[j] = beginR;
                    } else if(endR < currentPosition[j]) {
                        changed = true;
                        currentPosition[j] = endR;
                    }
                }
                
                if(changed) {
                    p.setPosition(currentPosition);
                }
                
                if(this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT) {
                    double dist = 0;
                    for(int j = 0; j < currentPosition.length; ++j) {
                         dist += ((currentPosition[j] - position[j]) * (currentPosition[j] - position[j]));
                    }
                    dist = Math.sqrt(dist);
                    sumPositions += dist;
                }
                
                p.calculatePersonalBest(param, sep);
                if(p.getBestEval() < this.globalBestEval) {
                    this.globalBestEval = p.getBestEval();
                    this.globalBestPosition = p.getBestPosition();
                }
            }
            
            if(rand.nextDouble() <= this.substitutionProbability) {
                int playerIndex = rand.nextInt(this.playerNumber);
                int substituteIndex = rand.nextInt(this.substituteNumber);
                Player substitute = substitutes.get(substituteIndex);
                if(substitute.getBestEval() < players[playerIndex].getBestEval()) {
                    players[playerIndex].setPosition(substitute.getBestPosition());
                    players[playerIndex].setBestPosition(substitute.getBestPosition());
                    players[playerIndex].setBestEval(substitute.getBestEval());
                }
            }
            
            Arrays.sort(players);
            int playerIndex = 0;
            for(int j = 0; j < this.substituteNumber; ++j) {
                if(substitutes.get(j).getBestEval() > players[playerIndex].getBestEval()) {
                    substitutes.add(j, new Player(this.function, players[playerIndex].getBestPosition(), players[playerIndex].getBestEval(), rand));
                    ++playerIndex;
                    substitutes.remove(this.substituteNumber);
                }
            }
            
            this.conditionFit = this.globalBestEval;
            if(this.stopCondition == StopConditionType.FUNCTION_SLOPE) {
                this.conditionList.add(this.globalBestEval);
            }    
            
            for(int j = 0; j < standardDeviation.length; ++j) {
                standardDeviation[j] = Math.sqrt(standardDeviation[j] / (double) this.playerNumber);
            }
            dataStandardDeviation.add(standardDeviation);
            
            
            if(this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT) {
                this.conditionList.add(sumPositions / (double) this.playerNumber);
            }
            
            
        }
        
        double playerDist[] = new double[function.getNumberOfVariables()];
        double standardDeviation[] =  new double[function.getNumberOfVariables()];
        
        dataFit.add(this.globalBestEval);
        for(Player p : players) {
            double position[] = p.getPosition();
            for(int j = 0; j < playerDist.length; ++j) {
                playerDist[j] += position[j];
            }
        }
        for(int j = 0; j < playerDist.length; ++j) {
            playerDist[j] = playerDist[j] / (double) this.playerNumber;
        }
        dataMean.add(playerDist);
        
        for(Player p : players) {
            double position[] = p.getPosition();
            for(int j = 0; j < position.length; ++j) {
                standardDeviation[j] += (position[j] - playerDist[j]) * (position[j] - playerDist[j]); 
            }
        }

        for(int j = 0; j < standardDeviation.length; ++j) {
            standardDeviation[j] = Math.sqrt(standardDeviation[j] / (double) this.playerNumber);
        }
        
        dataStandardDeviation.add(standardDeviation);
        
        long stopTime = System.nanoTime();
        System.out.println("");
        log.append("\n");
        log.append(LOGGER.message("---------------------------RESULT---------------------------\n"));
        log.append(LOGGER.message("Iteration = "+i+"\n"));
        log.append(LOGGER.message("Best Eval = "+this.globalBestEval+"\n"));
        log.append(LOGGER.message("Final Best Positions = ["));
        int end = this.globalBestPosition.length - 1;
        System.out.print(LOGGER.ANSI_CYAN);
        for(int j = 0; j < end; ++j) {
            log.append(this.globalBestPosition[j]).append(", ");
            System.out.print(this.globalBestPosition[j]+", ");
        }
        log.append(this.globalBestPosition[end]).append("]\n");
        System.out.print(this.globalBestPosition[end]+"]\n");
        log.append(LOGGER.message("Execution time: "+ ((stopTime - startTime) / 1000000) + " ms\n"));
        log.append(LOGGER.message("---------------------------COMPLETE---------------------------\n"));
        
        if(this.plotGraph) {
            Plotter plotter = new Plotter(dataFit, dataMean, dataStandardDeviation);
            plotter.start();
        }
        
        return log.toString();
    }
    
    public String runSGOFile(int testNumber) {
        StringBuilder log = new StringBuilder();
            
        if(this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || this.stopCondition == StopConditionType.FUNCTION_SLOPE) {
            this.conditionList = new ArrayList<>();
        }
        
        Player[] players = this.initialize();
        List<Player> substitutes = this.initializeSubstitutes(players);
        
        long startTime = System.nanoTime();
        int moveOffExecuted = 0;
        int substitutionsExecuted = 0;
        int i = 0;
        for(; i < this.kicksLimit && this.stopConditionEvaluete(); ++i) {
            double sumPositions = 0;
            for(Player p : players) {
                double personalBestPosition[] = p.getBestPosition();
                double position[] = p.getPosition();
                
                if(rand.nextDouble() <= this.moveOffProbability) {
                    p.setPosition(this.moveOff(position, i));
                    if(rand.nextDouble() <= this.moveForwardAfterMoveOffProbability) {
                        p.setPosition(this.moveForward(p.getPosition(), personalBestPosition));
                    }
                    ++moveOffExecuted;
                } else {
                    p.setPosition(this.moveForward(position, personalBestPosition));
                }
                
                double currentPosition[] = p.getPosition();
                boolean changed = false;
                for(int j = 0; j < this.function.getNumberOfVariables(); ++j) {
                    double beginR = Math.min(beginRange.getitem(j), endRange.getitem(j));
                    double endR = Math.max(beginRange.getitem(j), endRange.getitem(j));
                    if(beginR > currentPosition[j]) {
                        changed = true;
                        currentPosition[j] = beginR;
                    } else if(endR < currentPosition[j]) {
                        changed = true;
                        currentPosition[j] = endR;
                    }
                }
                
                if(changed) {
                    p.setPosition(currentPosition);
                }
                
                if(this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT) {
                    double dist = 0;
                    for(int j = 0; j < currentPosition.length; ++j) {
                         dist += ((currentPosition[j] - position[j]) * (currentPosition[j] - position[j]));
                    }
                    dist = Math.sqrt(dist);
                    sumPositions += dist;
                }
                
                p.calculatePersonalBest(param, sep);
                if(p.getBestEval() < this.globalBestEval) {
                    this.globalBestEval = p.getBestEval();
                    this.globalBestPosition = p.getBestPosition();
                }
            }
            
            if(rand.nextDouble() <= this.substitutionProbability) {
                int playerIndex = rand.nextInt(this.playerNumber);
                int substituteIndex = rand.nextInt(this.substituteNumber);
                Player substitute = substitutes.get(substituteIndex);
                if(substitute.getBestEval() < players[playerIndex].getBestEval()) {
                    players[playerIndex].setPosition(substitute.getBestPosition());
                    players[playerIndex].setBestPosition(substitute.getBestPosition());
                    players[playerIndex].setBestEval(substitute.getBestEval());
                }
                ++substitutionsExecuted;
            }
            
            Arrays.sort(players);
            int playerIndex = 0;
            for(int j = 0; j < this.substituteNumber; ++j) {
                if(substitutes.get(j).getBestEval() > players[playerIndex].getBestEval()) {
                    substitutes.add(j, new Player(this.function, players[playerIndex].getBestPosition(), players[playerIndex].getBestEval(), rand));
                    ++playerIndex;
                    substitutes.remove(this.substituteNumber);
                }
            }
            
            this.conditionFit = this.globalBestEval;
            if(this.stopCondition == StopConditionType.FUNCTION_SLOPE) {
                this.conditionList.add(this.globalBestEval);
            }    
            
            if(this.stopCondition == StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT) {
                this.conditionList.add(sumPositions / (double) this.playerNumber);
            }
            
            
        }
        long stopTime = System.nanoTime();
        log.append(LOGGER.message("Test: "+testNumber+"\n"));
        log.append(LOGGER.message("Best Eval = "+this.globalBestEval+"\n"));
        log.append(LOGGER.message("Substitution Eval = "+substitutes.get(0).getBestEval()+"\n"));
        log.append(LOGGER.message("Move Offs = "+moveOffExecuted+"\n"));
        log.append(LOGGER.message("Substitutions = "+substitutionsExecuted+"\n"));
        log.append(LOGGER.message("Final Best Positions = ["));
        int end = this.globalBestPosition.length - 1;
        System.out.print(LOGGER.ANSI_CYAN);
        for(int j = 0; j < end; ++j) {
            log.append(this.globalBestPosition[j]).append(", ");
            System.out.print(this.globalBestPosition[j]+", ");
        }
        log.append(this.globalBestPosition[end]).append("]\n");
        System.out.print(this.globalBestPosition[end]+"]\n");
        log.append(LOGGER.message("Execution time: "+ ((stopTime - startTime) / 1000000) + " ms\n"));
        
        return log.toString();
    }
    
    public Player[] initialize() {
        Player[] players = new Player[this.playerNumber];
        for(int i = 0; i < this.playerNumber; ++i) {
            players[i] = new Player(this.function, this.beginRange, this.endRange, rand, sep, param);
            if(players[i].getBestEval() < this.globalBestEval) {
                this.globalBestEval = players[i].getBestEval();
                this.globalBestPosition = players[i].getBestPosition();
            }
        }
        this.conditionFit = this.globalBestEval;
        
        if(this.stopCondition == StopConditionType.FUNCTION_SLOPE) {
            this.conditionList.add(this.globalBestEval);
        }
                
        return players;
    }
    
    public List<Player> initializeSubstitutes(Player[] players) {
        List<Player> substitutes = new ArrayList<>();
        Arrays.sort(players);
        for(int i = 0; i < this.substituteNumber; ++i) {
            substitutes.add(new Player(this.function, players[i].getBestPosition(), players[i].getBestEval(), rand));
        }
        return substitutes;
    }
    
    public double[] moveOff(double[] position, int kick) {
        double[] newPosition = new double[position.length];
        for(int i = 0; i < position.length; ++i) {
            if(rand.nextDouble() <= 0.5) {
                newPosition[i] = position[i] + NUM(kick, (this.endRange.getitem(i) - position[i]));
            } else {
                newPosition[i] = position[i] - NUM(kick, (position[i] - this.beginRange.getitem(i)));
            }
        }
        return newPosition;
    }
    
    public double NUM(double t, double y) {
        double exp = Math.pow((1 - (t/(double)this.kicksLimit)), this.nonUniformityDegree);
        return y * (1.0 - Math.pow(rand.nextDouble(), exp));
    }
    
    public double[] moveForward(double[] position, double[] bestPosition) {
        double[] newPosition = new double[position.length];
        for(int i = 0; i < position.length; ++i) {
            newPosition[i] = ((this.inertiaWeight * position[i]) + 
                            (this.cognitiveWeight * bestPosition[i]) + 
                            (this.socialWeight * this.globalBestPosition[i])) / 
                            (this.inertiaWeight + this.cognitiveWeight + this.socialWeight);
        }
        return newPosition;
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
        return this.globalBestEval;
    }
    
    public boolean stopConditionEvaluete() {
        switch(this.stopCondition) {
            case ONLY_ITERATION:
                return StopCondition.onlyIteration();
            case ACCEPTABLE_ERROR:
                return StopCondition.acceptableError(this.conditionTarget, this.conditionError, this.conditionFit);
            case NUMBER_OF_ITERATION_IMPROVEMENT:
                return StopCondition.numberOfIterationImprovment(this.conditionWindow, this.conditionError, this.conditionList);
            case FUNCTION_SLOPE:
                return StopCondition.functionSlope(this.conditionWindow, this.conditionError, this.conditionList);
            default:
                return StopCondition.onlyIteration();
        }
    }
    
    public enum StopConditionType {
        ONLY_ITERATION,
        ACCEPTABLE_ERROR,
        NUMBER_OF_ITERATION_IMPROVEMENT,
        FUNCTION_SLOPE;
    }
}
