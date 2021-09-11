/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sgo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import utils.Function;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import libeps.eps;
import libeps.libeps;
import libeps.param_fc;
import utils.FileManager;
import utils.Logger;
import utils.Plotter;
import utils.Utils;

/**
 *
 * @author meimarcel
 */
public class MainSGO {
    
    private static Logger LOGGER = new Logger();
        
    
    public static void run(boolean plotGraph, boolean saveLog, long seedDefined, String header) {
        StringBuilder log = new StringBuilder();
        log.append(header);
        log.append("\n");
        log.append(LOGGER.headerSGO());
        
        Random random = new Random();
        random.setSeed(seedDefined);
        
        
        Function function = Utils.getFunction();
        int playerNumber = Utils.getInt("Número de jogadores[1 - 1000]", 1, 1000);
        int substituteNumber = Utils.getInt("Número de substitutos[0 - "+playerNumber+"]", 1, playerNumber);
        int kicksLimit = Utils.getInt("Número máximo de chutes(iterações)[1 - 100.000]", 1, 100_000);

        double inertiaWeight = Utils.getDoubleWithDefault("Peso de inécia (Deixe em branco para manter o valor padrão "+SGO.DEFAULT_INERTIA_WEIGHT+")", 0, 100_000, SGO.DEFAULT_INERTIA_WEIGHT);
        double cognitiveWeight = Utils.getDoubleWithDefault("Peso cognitivo (Deixe em branco para manter o valor padrão "+SGO.DEFAULT_COGNITIVE_WEIGHT+")", 0, 100_000, SGO.DEFAULT_COGNITIVE_WEIGHT);
        double socialWeight = Utils.getDoubleWithDefault("Peso social (Deixe em branco para manter o valor padrão "+SGO.DEFAULT_SOCIAL_WEIGHT+")", 0, 100_000, SGO.DEFAULT_SOCIAL_WEIGHT);
        double moveOffProbability = Utils.getDoubleWithDefault("Probabilidade de Move Off(Deixe em branco para manter o valor padrão "+SGO.DEFAULT_MOVE_OFF_PROBABILITY+")", 0, 1, SGO.DEFAULT_MOVE_OFF_PROBABILITY);
        double moveForwardAfterMoveOffProbability = Utils.getDoubleWithDefault("Probabilidade de Move Forward após Move Off (Deixe em branco para manter o valor padrão "+SGO.DEFAULT_MOVE_FORWARD_AFTER_MOVE_OFF+")", 0, 1, SGO.DEFAULT_MOVE_FORWARD_AFTER_MOVE_OFF);
        double substitutionProbability = Utils.getDoubleWithDefault("Probabilidade de substituição (Deixe em branco para manter o valor padrão "+SGO.DEFAULT_SUBSTITUTION_PROBABILITY+")", 0, 1, SGO.DEFAULT_SUBSTITUTION_PROBABILITY);
        double nonUniformityDegree = Utils.getDoubleWithDefault("Grau de não uniformidade (Deixe em branco para manter o valor padrão "+SGO.DEFAULT_NON_UNIFORMITY_DEGREE+")", 0, 100_000, SGO.DEFAULT_NON_UNIFORMITY_DEGREE);

        SGO.StopConditionType stopCondition = getStopCondition();
        double conditionError = 0.0001;
        double conditionTarget = 0;
        int conditionWindow = 20;
        
        eps sep = getEps(function);
        param_fc param = getParamFc();

        SGO sgo = new SGO(playerNumber, substituteNumber, kicksLimit, inertiaWeight, 
            cognitiveWeight, socialWeight, function,
            moveOffProbability, moveForwardAfterMoveOffProbability,
            substitutionProbability, nonUniformityDegree, sep, param);
        
        sgo.setStopConditionType(stopCondition);
        sgo.setRandom(random);

        if(stopCondition == SGO.StopConditionType.ACCEPTABLE_ERROR) {
            conditionTarget = Utils.getDouble("Alvo", -10e9, 10e9);
            conditionError = Utils.getDouble("Error", -10e9, 10e9);

            sgo.setConditionTarget(conditionTarget);
            sgo.setConditionError(conditionError);

        } else if(stopCondition == SGO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == SGO.StopConditionType.FUNCTION_SLOPE) {
            conditionWindow = Utils.getInt("Janela de interações", Integer.MIN_VALUE, Integer.MAX_VALUE);
            conditionError = Utils.getDouble("Error", -10e9, 10e9);

            sgo.setConditionWindow(conditionWindow);
            sgo.setConditionError(conditionError);

        }

        System.out.println("");
        log.append("\n");
        log.append(LOGGER.info("Function: "+function.getStringFunction()+"\n"));
        log.append(LOGGER.info("Number of players: "+playerNumber+"\n"));
        log.append(LOGGER.info("Number of substitutes: "+substituteNumber+"\n"));
        log.append(LOGGER.info("Kicks(iteration) Limit: "+kicksLimit+"\n"));
        log.append(LOGGER.info("Inertia Weight: "+inertiaWeight+"\n"));
        log.append(LOGGER.info("Cognitive Weight: "+cognitiveWeight+"\n"));
        log.append(LOGGER.info("Social Weight: "+socialWeight+"\n"));
        
        log.append(LOGGER.info("Move Off Probability: "+moveOffProbability+"\n"));
        log.append(LOGGER.info("Move Forward after Move Off Probability: "+moveForwardAfterMoveOffProbability+"\n"));
        log.append(LOGGER.info("Substitution Probability: "+substitutionProbability+"\n"));
        log.append(LOGGER.info("Non-Uniformity Degree: "+nonUniformityDegree+"\n"));
        
        log.append(LOGGER.info("Stop Condition: "+stopCondition+"\n"));

        if(stopCondition == SGO.StopConditionType.ACCEPTABLE_ERROR) {
            log.append(LOGGER.info("Target: "+conditionTarget+"\n"));
            log.append(LOGGER.info("Error: "+conditionError+"\n"));

        } else if(stopCondition == SGO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == SGO.StopConditionType.FUNCTION_SLOPE) {
            log.append(LOGGER.info("Iteration Window: "+conditionWindow+"\n"));
            log.append(LOGGER.info("Error: "+conditionError+"\n"));

        }
        System.out.println("");

        if(plotGraph) 
            sgo.setPlotGraph(plotGraph);

        log.append(sgo.runSGO());
        
        if(saveLog) {
            String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            FileManager.Write(FileSystems.getDefault().getPath("").toAbsolutePath()+"/data/"+timeStamp, "log.txt", log.toString());
        }
        
        libeps.free_EPS(sep);
        
    }
    
    public static void runFile(boolean plotGraph, boolean saveLog, long seedDefined, String header, String filePath) {
        StringBuilder log = new StringBuilder();
        log.append(header);
        log.append("\n");
        log.append(LOGGER.headerSGO());
        
        Random random = new Random();
        random.setSeed(seedDefined);
        
        try {
            BufferedReader bf = new BufferedReader(new FileReader(filePath));
            Gson gson = new Gson();
            JsonArray jsonObj = gson.fromJson(bf, JsonArray.class);
            int caseIndex = 1;
            long startTime = System.nanoTime();
            for(JsonElement testCase : jsonObj) {
                random.setSeed(seedDefined);
                JsonObject json = testCase.getAsJsonObject();
                Function function = Utils.parseFunction(json.get("function").getAsString());
                int numberOfTests = Utils.parseInt("number_of_tests", json.get("number_of_tests").getAsInt(), 1, 100000);
                int playerNumber = Utils.parseInt("player_number", json.get("player_number").getAsInt(), 1, 1000);
                int substituteNumber = Utils.parseInt("substitute_number", json.get("substitute_number").getAsInt(), 1, playerNumber);
                int kicksLimit = Utils.parseInt("kicks_limit", json.get("kicks_limit").getAsInt(), 1, 900_000);

                double inertiaWeight = Utils.parseDouble("inertia_weight", json.get("inertia_weight").getAsDouble(), 0, 100_000);
                double cognitiveWeight = Utils.parseDouble("cognitive_weight", json.get("cognitive_weight").getAsDouble(), 0, 100_000);
                double socialWeight = Utils.parseDouble("social_weight", json.get("social_weight").getAsDouble(), 0, 100_000);
                double moveOffProbability = Utils.parseDouble("move_off_probability", json.get("move_off_probability").getAsDouble(), 0, 1);
                double moveForwardAfterMoveOffProbability = Utils.parseDouble("move_forward_after_move_off_probability", json.get("move_forward_after_move_off_probability").getAsDouble(), 0, 1);
                double substitutionProbability = Utils.parseDouble("substitution_probability", json.get("substitution_probability").getAsDouble(), 0, 1);
                double nonUniformityDegree = Utils.parseDouble("non_uniformity_degree", json.get("non_uniformity_degree").getAsDouble(), 0, 100_000);
                double functionMinimum = Utils.parseDouble("function_minimum", json.get("function_minimum").getAsDouble(), -10e9, 10e9);
                JsonObject stopConditionTypeJson = json.get("stop_condition_type").getAsJsonObject();
                SGO.StopConditionType stopCondition = parseStopCondition(stopConditionTypeJson.get("type").getAsString());
                double conditionError = 0.0001;
                double conditionTarget = 0;
                int conditionWindow = 20;

                if(stopCondition == SGO.StopConditionType.ACCEPTABLE_ERROR) {
                    conditionTarget = Utils.parseDouble("target", stopConditionTypeJson.get("target").getAsDouble(), -10e9, 10e9);
                    conditionError = Utils.parseDouble("error", stopConditionTypeJson.get("error").getAsDouble(), -10e9, 10e9);

                } else if(stopCondition == SGO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == SGO.StopConditionType.FUNCTION_SLOPE) {
                    conditionWindow = Utils.parseInt("iteration_window", stopConditionTypeJson.get("iteration_window").getAsInt(),Integer.MIN_VALUE, Integer.MAX_VALUE);
                    conditionError = Utils.parseDouble("error", stopConditionTypeJson.get("error").getAsDouble(), -10e9, 10e9);

                }

                System.out.println("");
                log.append(LOGGER.white("---------------------------CASE: "+(caseIndex++)+"---------------------------\n"));
                log.append("\n");
                log.append(LOGGER.info("Number of Tests: "+numberOfTests+"\n"));
                log.append(LOGGER.info("Function: "+function.getStringFunction()+"\n"));
                log.append(LOGGER.info("Function Minimum: "+functionMinimum+"\n"));
                log.append(LOGGER.info("Number of players: "+playerNumber+"\n"));
                log.append(LOGGER.info("Number of substitutes: "+substituteNumber+"\n"));
                log.append(LOGGER.info("Kicks(iteration) Limit: "+kicksLimit+"\n"));
                log.append(LOGGER.info("Inertia Weight: "+inertiaWeight+"\n"));
                log.append(LOGGER.info("Cognitive Weight: "+cognitiveWeight+"\n"));
                log.append(LOGGER.info("Social Weight: "+socialWeight+"\n"));
                log.append(LOGGER.info("Move Off Probability: "+moveOffProbability+"\n"));
                log.append(LOGGER.info("Move Forward after Move Off Probability: "+moveForwardAfterMoveOffProbability+"\n"));
                log.append(LOGGER.info("Substitution Probability: "+substitutionProbability+"\n"));
                log.append(LOGGER.info("Non-Uniformity Degree: "+nonUniformityDegree+"\n"));

                log.append(LOGGER.info("Stop Condition: "+stopCondition+"\n"));

                if(stopCondition == SGO.StopConditionType.ACCEPTABLE_ERROR) {
                    log.append(LOGGER.info("Target: "+conditionTarget+"\n"));
                    log.append(LOGGER.info("Error: "+conditionError+"\n"));

                } else if(stopCondition == SGO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == SGO.StopConditionType.FUNCTION_SLOPE) {
                    log.append(LOGGER.info("Iteration Window: "+conditionWindow+"\n"));
                    log.append(LOGGER.info("Error: "+conditionError+"\n"));

                }
                System.out.println("");
                log.append("\n");
                log.append(LOGGER.white("---------------------------EXECUTING---------------------------\n"));
                log.append("\n");
                System.out.println("");
                
                List<Double> evalList = new ArrayList<>();
                int bestTest = 0;
                double bestEval = Integer.MAX_VALUE;
                eps sep = getEps(function);
                param_fc param = getParamFc();
                
                for(int i = 1; i <= numberOfTests; ++i) {
                    SGO sgo = new SGO(playerNumber, substituteNumber, kicksLimit, inertiaWeight, 
                        cognitiveWeight, socialWeight, function,
                        moveOffProbability, moveForwardAfterMoveOffProbability,
                        substitutionProbability, nonUniformityDegree, sep, param);

                    sgo.setStopConditionType(stopCondition);
                    sgo.setRandom(random);

                    if(stopCondition == SGO.StopConditionType.ACCEPTABLE_ERROR) {
                        sgo.setConditionTarget(conditionTarget);
                        sgo.setConditionError(conditionError);

                    } else if(stopCondition == SGO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == SGO.StopConditionType.FUNCTION_SLOPE) { 
                        sgo.setConditionWindow(conditionWindow);
                        sgo.setConditionError(conditionError);

                    }

                    log.append(sgo.runSGOFile(i));
                    evalList.add(sgo.getGlobalBestEval());
                    if(sgo.getGlobalBestEval() < bestEval) {
                        bestTest = i;
                        bestEval = sgo.getGlobalBestEval();
                    }
                }
                
                libeps.free_EPS(sep);
                
                double meanError = 0;
                double standardDeviation = 0;
                for(int i = 0; i < numberOfTests; ++i) {
                    meanError += Math.abs(evalList.get(i) - functionMinimum);
                }
                meanError /= numberOfTests;
                
                double mean = 0;
                for(int i = 0; i < numberOfTests; ++i) {
                    mean += evalList.get(i);
                }
                mean /= numberOfTests;
                
                for(int i = 0; i < numberOfTests; ++i) {
                    standardDeviation += ((evalList.get(i) - mean) * (evalList.get(i) - mean));
                }
                standardDeviation = Math.sqrt(standardDeviation / numberOfTests);
                
                log.append("\n");
                log.append(LOGGER.message("---------------------------RESULT---------------------------\n"));
                log.append(LOGGER.message("Best Test = "+bestTest+" Eval = "+bestEval+"\n"));
                log.append(LOGGER.message("Mean = "+mean+"\n"));
                log.append(LOGGER.message("Average Error = "+meanError+"\n"));
                log.append(LOGGER.message("Standar Deviation = "+standardDeviation+"\n"));
                log.append("\n");
                log.append(LOGGER.message("------------------------------------------------------------\n"));
                log.append("\n");
            }
            long stopTime = System.nanoTime();
            
            log.append(LOGGER.message("Total Execution time: "+ ((stopTime - startTime) / 1000000) + " ms\n"));
            log.append(LOGGER.message("---------------------------COMPLETE---------------------------\n"));
            if(plotGraph) {
                /*Plotter plotter = new Plotter(dataFit, dataMean, dataStandardDeviation);
                plotter.start();*/
            }
            
            if(saveLog) {
                String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
                FileManager.Write(FileSystems.getDefault().getPath("").toAbsolutePath()+"/data/"+timeStamp, "log.txt", log.toString());
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Não foi possível abrir o arquivo em " + filePath+"\n");   
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("JSON malformatado: "+e.getMessage()+"\n"); 
        }
        
    }
    
    public static SGO.StopConditionType parseStopCondition(String stopCondition) {
        stopCondition = stopCondition.toUpperCase();
        switch(stopCondition) {
            case "ONLY_ITERATION":
                return SGO.StopConditionType.ONLY_ITERATION;
            case "ACCEPTABLE_ERROR":
                return SGO.StopConditionType.ACCEPTABLE_ERROR;
            case "NUMBER_OF_ITERATION_IMPROVEMENT":
                return SGO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT;
            case "FUNCTION_SLOPE":
                return SGO.StopConditionType.FUNCTION_SLOPE;
            default:
                throw new RuntimeException("Condição de para não encontrada");
        }
    }
    
    public static SGO.StopConditionType getStopCondition() {
        String option;
        Scanner in = new Scanner(System.in);
        System.out.println("\nEscolha a condição de parada");
        System.out.println("1 - Somente número de iterações");
        System.out.println("2 - Parar em um erro aceitável");
        System.out.println("3 - Parar quando não houver melhorias");
        System.out.println("4 - Parar quando a inclinação da função objetiva for menor que um erro");
        System.out.print("[ENTRADA]: ");
        
        List<String> options = Arrays.asList("1","2","3","4");
        while(true) {
            option = in.nextLine();
            option = option.trim();
            if(!options.contains(option)) {
                LOGGER.error("Opção inválida\n");
                System.out.print("[ENTRADA]: ");
            } else {
                break;
            }
        }
        switch(option) {
            case "1":
                return SGO.StopConditionType.ONLY_ITERATION;
            case "2":
                return SGO.StopConditionType.ACCEPTABLE_ERROR;
            case "3":
                return SGO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT;
            case "4":
                return SGO.StopConditionType.FUNCTION_SLOPE;
            default:
                return SGO.StopConditionType.ONLY_ITERATION;
        }
    }
    
    private static eps getEps(Function function) {
        if(function.getFunctionType() == Function.FunctionType.ARQ_STAGG_5) {
            return libeps.new_EPS(libeps.ARQ_STAGG_5);
        } else if(function.getFunctionType() == Function.FunctionType.ARQ_6_BUS) {
            return libeps.new_EPS(libeps.ARQ_6_BUS);
        } else if(function.getFunctionType() == Function.FunctionType.ARQ_IEEE_14_BUS) {
            return libeps.new_EPS(libeps.ARQ_IEEE_14_BUS);
        } else if(function.getFunctionType() == Function.FunctionType.ARQ_IEEE_30_BUS) {
            return libeps.new_EPS(libeps.ARQ_IEEE_30_BUS);
        }
        return null;
    }
    
    private static param_fc getParamFc() {
        param_fc param = new param_fc();
        libeps.init_power_flow_parameters(param);
        return param;
    }
    
}
