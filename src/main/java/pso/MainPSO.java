/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pso;

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
import utils.Utils;

/**
 *
 * @author meimarcel
 */
public class MainPSO {
    
    private static Logger LOGGER = new Logger();
        
    
    public static void run(boolean plotGraph, boolean saveLog, long seedDefined, String header) {
        StringBuilder log = new StringBuilder();
        log.append(header);
        log.append("\n");
        log.append(LOGGER.headerPSO());
        
        Random random = new Random();
        random.setSeed(seedDefined);
        
        
        int neighborhoodSize = -1;
        String psoType = getPSOType();
        Function function = Utils.getFunction();
        int particleNumber = Utils.getInt("Número de partículas[1 - 1000]", 1, 10_000);
        int iterationLimit = Utils.getInt("Número máximo de iterações[1 - 100.000]", 1, 100_000);

        if(psoType.equals("2")) 
            neighborhoodSize = Utils.getInt("Tamanho da vizinhança[1 - "+particleNumber+"]", 1, particleNumber);

        double inertiaWeight = Utils.getDoubleWithDefault("Peso de inécia (Deixe em branco para manter o valor padrão "+PSO.DEFAULT_INERTIA_WEIGHT+")", 0, 100_000, PSO.DEFAULT_INERTIA_WEIGHT);
        double cognitiveWeight = Utils.getDoubleWithDefault("Peso cognitivo (Deixe em branco para manter o valor padrão "+PSO.DEFAULT_COGNITIVE_WEIGHT+")", 0, 100_000, PSO.DEFAULT_COGNITIVE_WEIGHT);
        double socialWeight = Utils.getDoubleWithDefault("Peso social (Deixe em branco para manter o valor padrão "+PSO.DEFAULT_SOCIAL_WEIGHT+")", 0, 100_000, PSO.DEFAULT_SOCIAL_WEIGHT);

        PSO.StopConditionType stopCondition = getStopCondition();
        double conditionError = 0.0001;
        double conditionTarget = 0;
        int conditionWindow = 20;
        
        eps sep = getEps(function);
        param_fc param = new param_fc();
        libeps.init_power_flow_parameters(param);

        PSO pso = new PSO(particleNumber, iterationLimit, inertiaWeight, cognitiveWeight, socialWeight, function, sep, param);
        pso.setStopConditionType(stopCondition);
        pso.setRandom(random);

        if(stopCondition == PSO.StopConditionType.ACCEPTABLE_ERROR) {
            conditionTarget = Utils.getDouble("Alvo", -10e9, 10e9);
            conditionError = Utils.getDouble("Error", -10e9, 10e9);

            pso.setConditionTarget(conditionTarget);
            pso.setConditionError(conditionError);

        } else if(stopCondition == PSO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == PSO.StopConditionType.FUNCTION_SLOPE) {
            conditionWindow = Utils.getInt("Janela de interações", Integer.MIN_VALUE, Integer.MAX_VALUE);
            conditionError = Utils.getDouble("Error", -10e9, 10e9);

            pso.setConditionWindow(conditionWindow);
            pso.setConditionError(conditionError);

        } else if(stopCondition == PSO.StopConditionType.NORMALIZED_RADIUS) {
            conditionError = Utils.getDouble("Error", -10e9, 10e9);
            pso.setConditionError(conditionError);
        }

        System.out.println("");
        log.append("\n");
        log.append(LOGGER.info("PSO Type: "+((psoType.equals("1")) ? "Gbest PSO" : "Lbest PSO")+"\n"));
        log.append(LOGGER.info("Function: "+function.getStringFunction()+"\n"));
        log.append(LOGGER.info("Number of particles: "+particleNumber+"\n"));
        log.append(LOGGER.info("Iteration Limit: "+iterationLimit+"\n"));

        if(psoType.equals("2")) 
            log.append(LOGGER.info("Neighborhood Size: "+neighborhoodSize+"\n"));

        log.append(LOGGER.info("Inertia Weight: "+inertiaWeight+"\n"));
        log.append(LOGGER.info("Cognitive Weight: "+cognitiveWeight+"\n"));
        log.append(LOGGER.info("Social Weight: "+socialWeight+"\n"));
        log.append(LOGGER.info("Stop Condition: "+stopCondition+"\n"));

        if(stopCondition == PSO.StopConditionType.ACCEPTABLE_ERROR) {
            log.append(LOGGER.info("Target: "+conditionTarget+"\n"));
            log.append(LOGGER.info("Error: "+conditionError+"\n"));

        } else if(stopCondition == PSO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == PSO.StopConditionType.FUNCTION_SLOPE) {
            log.append(LOGGER.info("Iteration Window: "+conditionWindow+"\n"));
            log.append(LOGGER.info("Error: "+conditionError+"\n"));

        } else if(stopCondition == PSO.StopConditionType.NORMALIZED_RADIUS) {
            log.append(LOGGER.info("Error: "+conditionError+"\n"));
        }
        System.out.println("");

        if(plotGraph) 
            pso.setPlotGraph(plotGraph);

        if(psoType.equals("1")) {
            log.append(pso.runGBestPSO());
        } else {
            pso.setNeighborhoodSize(neighborhoodSize);
            log.append(pso.runLBestPSO());
        }
        
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
        log.append(LOGGER.headerPSO());
        
        Random random = new Random();
        random.setSeed(seedDefined);
        
        try {
            BufferedReader bf = new BufferedReader(new FileReader(filePath));
            Gson gson = new Gson();
            JsonArray jsonObj = gson.fromJson(bf, JsonArray.class);
            int caseIndex = 1;
            long startTime = System.nanoTime();
            for(JsonElement testCase : jsonObj) {
                JsonObject json = testCase.getAsJsonObject();
                
                int neighborhoodSize = -1;
                String psoType = parsePSOType(json.get("pso_type").getAsString());
                int numberOfTests = Utils.parseInt("number_of_tests", json.get("number_of_tests").getAsInt(), 1, 100000);
                double functionMinimum = Utils.parseDouble("function_minimum", json.get("function_minimum").getAsDouble(), -10e9, 10e9);
                Function function = Utils.parseFunction(json.get("function").getAsString());
                int particleNumber = Utils.parseInt("particle_number", json.get("particle_number").getAsInt(), 1, 10_000);
                int iterationLimit = Utils.parseInt("iteration_limit", json.get("iteration_limit").getAsInt(), 1, 100_000);

                if(psoType.equals("2")) 
                    neighborhoodSize = Utils.parseInt("neighborhood_size", json.get("neighborhood_size").getAsInt(), 1, particleNumber);

                double inertiaWeight = Utils.parseDouble("inertia_weight", json.get("inertia_weight").getAsDouble(), 0, 100_000);
                double cognitiveWeight = Utils.parseDouble("cognitive_weight", json.get("inertia_weight").getAsDouble(), 0, 100_000);
                double socialWeight = Utils.parseDouble("social_weight", json.get("social_weight").getAsDouble(), 0, 100_000);
                
                JsonObject stopConditionTypeJson = json.get("stop_condition_type").getAsJsonObject();
                PSO.StopConditionType stopCondition = parseStopCondition(stopConditionTypeJson.get("type").getAsString());
                double conditionError = 0.0001;
                double conditionTarget = 0;
                int conditionWindow = 20;

                if(stopCondition == PSO.StopConditionType.ACCEPTABLE_ERROR) {
                    conditionTarget = Utils.parseDouble("target", stopConditionTypeJson.get("target").getAsDouble(), -10e9, 10e9);
                    conditionError = Utils.parseDouble("error", stopConditionTypeJson.get("error").getAsDouble(), -10e9, 10e9);

                } else if(stopCondition == PSO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == PSO.StopConditionType.FUNCTION_SLOPE) {
                    conditionWindow = Utils.parseInt("iteration_window", stopConditionTypeJson.get("iteration_window").getAsInt(),Integer.MIN_VALUE, Integer.MAX_VALUE);
                    conditionError = Utils.parseDouble("error", stopConditionTypeJson.get("error").getAsDouble(), -10e9, 10e9);

                } else if(stopCondition == PSO.StopConditionType.NORMALIZED_RADIUS) {
                    conditionError = Utils.parseDouble("error", stopConditionTypeJson.get("error").getAsDouble(), -10e9, 10e9);
                }

                System.out.println("");
                log.append(LOGGER.white("---------------------------CASE: "+(caseIndex++)+"---------------------------\n"));
                log.append("\n");
                log.append(LOGGER.info("PSO Type: "+((psoType.equals("1")) ? "Gbest PSO" : "Lbest PSO")+"\n"));
                log.append(LOGGER.info("Function: "+function.getStringFunction()+"\n"));
                log.append(LOGGER.info("Number of particles: "+particleNumber+"\n"));
                log.append(LOGGER.info("Iteration Limit: "+iterationLimit+"\n"));

                if(psoType.equals("2")) 
                    log.append(LOGGER.info("Neighborhood Size: "+neighborhoodSize+"\n"));

                log.append(LOGGER.info("Inertia Weight: "+inertiaWeight+"\n"));
                log.append(LOGGER.info("Cognitive Weight: "+cognitiveWeight+"\n"));
                log.append(LOGGER.info("Social Weight: "+socialWeight+"\n"));
                log.append(LOGGER.info("Stop Condition: "+stopCondition+"\n"));

                if(stopCondition == PSO.StopConditionType.ACCEPTABLE_ERROR) {
                    log.append(LOGGER.info("Target: "+conditionTarget+"\n"));
                    log.append(LOGGER.info("Error: "+conditionError+"\n"));

                } else if(stopCondition == PSO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == PSO.StopConditionType.FUNCTION_SLOPE) {
                    log.append(LOGGER.info("Iteration Window: "+conditionWindow+"\n"));
                    log.append(LOGGER.info("Error: "+conditionError+"\n"));

                } else if(stopCondition == PSO.StopConditionType.NORMALIZED_RADIUS) {
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
                    PSO pso = new PSO(particleNumber, iterationLimit, inertiaWeight, cognitiveWeight, socialWeight, function, sep, param);
                    pso.setStopConditionType(stopCondition);
                    pso.setRandom(random);

                    if(stopCondition == PSO.StopConditionType.ACCEPTABLE_ERROR) {
                        pso.setConditionTarget(conditionTarget);
                        pso.setConditionError(conditionError);

                    } else if(stopCondition == PSO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == PSO.StopConditionType.FUNCTION_SLOPE) {
                        pso.setConditionWindow(conditionWindow);
                        pso.setConditionError(conditionError);

                    } else if(stopCondition == PSO.StopConditionType.NORMALIZED_RADIUS) {
                        pso.setConditionError(conditionError);
                    }

                    if(psoType.equals("1")) {
                        log.append(pso.runGBestPSOFile(i));
                    } else {
                        pso.setNeighborhoodSize(neighborhoodSize);
                        log.append(pso.runLBestPSOFile(i));
                    }
                    
                    evalList.add(pso.getGlobalBestEval());
                    if(pso.getGlobalBestEval() < bestEval) {
                        bestTest = i;
                        bestEval = pso.getGlobalBestEval();
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
    
    public static String getPSOType() {
        String psoType = "-1";
        Scanner in = new Scanner(System.in);
        System.out.println("\nEscolha um o algoritmo PSO");
        System.out.println("1 - Global Best PSO (gbest PSO)");
        System.out.println("2 - Local Best PSO (lbest PSO)");
        System.out.print("[ENTRADA]: ");
        
        while(true) {
            psoType = in.nextLine();
            psoType = psoType.trim();
            if(!psoType.equals("1") && !psoType.equals("2")) {
                LOGGER.error("Opção inválida\n");
                System.out.print("[ENTRADA]: ");
            } else {
                break;
            }
        }
        
        return psoType;
    }
    
    public static String parsePSOType(String psoType) {
        switch(psoType) {
            case "GBEST":
                return "1";
            case "LBEST":
                return "2";
            default:
                throw new RuntimeException("PSO Type Not Found");
        }
    }
    
    public static PSO.StopConditionType getStopCondition() {
        String option;
        Scanner in = new Scanner(System.in);
        System.out.println("\nEscolha a condição de parada");
        System.out.println("1 - Somente número de iterações");
        System.out.println("2 - Parar em um erro aceitável");
        System.out.println("3 - Parar quando não houver melhorias");
        System.out.println("4 - Parar quando o raio do enxame for menor que um erro");
        System.out.println("5 - Parar quando a inclinação da função objetiva for menor que um erro");
        System.out.print("[ENTRADA]: ");
        
        List<String> options = Arrays.asList("1","2","3","4","5");
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
                return PSO.StopConditionType.ONLY_ITERATION;
            case "2":
                return PSO.StopConditionType.ACCEPTABLE_ERROR;
            case "3":
                return PSO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT;
            case "4":
                return PSO.StopConditionType.NORMALIZED_RADIUS;
            case "5":
                return PSO.StopConditionType.FUNCTION_SLOPE;
            default:
                return PSO.StopConditionType.ONLY_ITERATION;
        }
    }
    
    public static PSO.StopConditionType parseStopCondition(String stopCondition) {
        switch(stopCondition) {
            case "ONLY_ITERATION":
                return PSO.StopConditionType.ONLY_ITERATION;
            case "ACCEPTABLE_ERROR":
                return PSO.StopConditionType.ACCEPTABLE_ERROR;
            case "NUMBER_OF_ITERATION_IMPROVEMENT":
                return PSO.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT;
            case "NORMALIZED_RADIUS":
                return PSO.StopConditionType.NORMALIZED_RADIUS;
            case "FUNCTION_SLOPE":
                return PSO.StopConditionType.FUNCTION_SLOPE;
            default:
                throw new RuntimeException("Stop Condition Type Not Found");
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
