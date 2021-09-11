/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import utils.Function;
import utils.Logger;
import utils.Utils;

/**
 *
 * @author meimarcel
 */
public class MainGA {
    private static Logger LOGGER = new Logger();
    
    public static void run(boolean plotGraph, boolean saveLog, long seedDefined, String header) {
        StringBuilder log = new StringBuilder();
        log.append(header);
        log.append("\n");
        log.append(LOGGER.headerGA());
        
        Random random = new Random();
        random.setSeed(seedDefined);
        
        int numberOfPopulation = Utils.getInt("Tamanho da poulação[1 - 1000]", 1, 10_000);
        int iterationLimit = Utils.getInt("Número máximo de iterações[1 - 100.000]", 1, 100_000);
        Function function = Utils.getFunction();

        double crossoverProbability = Utils.getDoubleWithDefault("Taxa de crossover[0.0 - 1.0] (Deixe em branco para manter o valor padrão "+GA.DEFAULT_CROSSOVER_PROBABILITY+")", 0, 1, GA.DEFAULT_CROSSOVER_PROBABILITY);
        double mutationProbability = Utils.getDoubleWithDefault("Taxa de mutação[0.0 - 1.0] (Deixe em branco para manter o valor padrão "+GA.DEFAULT_MUTATION_PROBABILITY+")", 0, 1, GA.DEFAULT_MUTATION_PROBABILITY);
        int elitism = Utils.getInt("Elitismo[0 - "+numberOfPopulation+"] (Número de indivíduos a serem passados para pŕoxima geração)", 0, numberOfPopulation);
        
        GA.CrossoverType crossoverType = getCrossoverType();
        GA.SelectionType selectionType = getSelectionType();

        GA.StopConditionType stopCondition = getStopCondition();
        double conditionError = 0.0001;
        double conditionTarget = 0;
        int conditionWindow = 20;
        
        eps sep = getEps(function);
        param_fc param = getParamFc();

        GA ga = new GA(numberOfPopulation, iterationLimit, crossoverProbability, mutationProbability, elitism, crossoverType, function, selectionType, sep, param);
        ga.setStopConditionType(stopCondition);
        ga.setRandom(random);

        if(stopCondition == GA.StopConditionType.ACCEPTABLE_ERROR) {
            conditionTarget = Utils.getDouble("Alvo", -10e9, 10e9);
            conditionError = Utils.getDouble("Error", -10e9, 10e9);

            ga.setConditionTarget(conditionTarget);
            ga.setConditionError(conditionError);

        } else if(stopCondition == GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == GA.StopConditionType.FUNCTION_SLOPE || stopCondition == GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION) {
            conditionWindow = Utils.getInt("Janela de interações", Integer.MIN_VALUE, Integer.MAX_VALUE);
            conditionError = Utils.getDouble("Error", -10e9, 10e9);

            ga.setConditionWindow(conditionWindow);
            ga.setConditionError(conditionError);

        }

        System.out.println("");
        log.append("\n");
        log.append(LOGGER.info("Function: "+function.getStringFunction()+"\n"));
        log.append(LOGGER.info("Number of population: "+numberOfPopulation+"\n"));
        log.append(LOGGER.info("Iteration Limit: "+iterationLimit+"\n"));
        log.append(LOGGER.info("Crossover Probability: "+crossoverProbability+"\n"));
        log.append(LOGGER.info("Mutation Probability: "+mutationProbability+"\n"));
        log.append(LOGGER.info("Elitism: "+elitism+"\n"));
        log.append(LOGGER.info("Stop Condition: "+stopCondition+"\n"));
        log.append(LOGGER.info("Crossover Type: "+crossoverType+"\n"));
        log.append(LOGGER.info("Selectio Type: "+selectionType+"\n"));

        if(stopCondition == GA.StopConditionType.ACCEPTABLE_ERROR) {
            log.append(LOGGER.info("Target: "+conditionTarget+"\n"));
            log.append(LOGGER.info("Error: "+conditionError+"\n"));

        } else if(stopCondition == GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == GA.StopConditionType.FUNCTION_SLOPE || stopCondition == GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION) {
            log.append(LOGGER.info("Iteration Window: "+conditionWindow+"\n"));
            log.append(LOGGER.info("Error: "+conditionError+"\n"));

        }
        System.out.println("");

        if(plotGraph) 
            ga.setPlotGraph(plotGraph);
        
        log.append(ga.runGA());
        
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
        log.append(LOGGER.headerGA());
        
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
                
                int numberOfTests = Utils.parseInt("number_of_tests", json.get("number_of_tests").getAsInt(), 1, 100000);
                int numberOfPopulation = Utils.parseInt("number_of_population", json.get("number_of_population").getAsInt(), 1, 10_000);
                int iterationLimit = Utils.parseInt("iteration_limit", json.get("iteration_limit").getAsInt(), 1, 100_000);
                Function function = Utils.parseFunction(json.get("function").getAsString());

                double crossoverProbability = Utils.parseDouble("crossover_probability", json.get("crossover_probability").getAsDouble(), 0, 1);
                double mutationProbability = Utils.parseDouble("mutation_probability", json.get("mutation_probability").getAsDouble(), 0, 1);
                int elitism = Utils.parseInt("elitism", json.get("elitism").getAsInt(), 0, numberOfPopulation);
                double functionMinimum = Utils.parseDouble("function_minimum", json.get("function_minimum").getAsDouble(), -10e9, 10e9);

                GA.CrossoverType crossoverType = parseCrossoverType(json.get("crossover_type").getAsString());
                GA.SelectionType selectionType = parseSelectionType(json.get("selection_type").getAsString());
                        
                JsonObject stopConditionTypeJson = json.get("stop_condition_type").getAsJsonObject();
                GA.StopConditionType stopCondition = parseStopCondition(stopConditionTypeJson.get("type").getAsString());
                double conditionError = 0.0001;
                double conditionTarget = 0;
                int conditionWindow = 20;

                if(stopCondition == GA.StopConditionType.ACCEPTABLE_ERROR) {
                    conditionTarget = Utils.parseDouble("target", stopConditionTypeJson.get("target").getAsDouble(), -10e9, 10e9);
                    conditionError = Utils.parseDouble("error", stopConditionTypeJson.get("error").getAsDouble(), -10e9, 10e9);

                } else if(stopCondition == GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == GA.StopConditionType.FUNCTION_SLOPE || stopCondition == GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION) {
                    conditionWindow = Utils.parseInt("iteration_window", stopConditionTypeJson.get("iteration_window").getAsInt(),Integer.MIN_VALUE, Integer.MAX_VALUE);
                    conditionError = Utils.parseDouble("error", stopConditionTypeJson.get("error").getAsDouble(), -10e9, 10e9);

                }

                System.out.println("");
                log.append(LOGGER.white("---------------------------CASE: "+(caseIndex++)+"---------------------------\n"));
                log.append("\n");
                log.append(LOGGER.info("Function: "+function.getStringFunction()+"\n"));
                log.append(LOGGER.info("Number of population: "+numberOfPopulation+"\n"));
                log.append(LOGGER.info("Iteration Limit: "+iterationLimit+"\n"));
                log.append(LOGGER.info("Crossover Probability: "+crossoverProbability+"\n"));
                log.append(LOGGER.info("Mutation Probability: "+mutationProbability+"\n"));
                log.append(LOGGER.info("Elitism: "+elitism+"\n"));
                log.append(LOGGER.info("Stop Condition: "+stopCondition+"\n"));
                log.append(LOGGER.info("Crossover Type: "+crossoverType+"\n"));
                log.append(LOGGER.info("Selectio Type: "+selectionType+"\n"));

                if(stopCondition == GA.StopConditionType.ACCEPTABLE_ERROR) {
                    log.append(LOGGER.info("Target: "+conditionTarget+"\n"));
                    log.append(LOGGER.info("Error: "+conditionError+"\n"));

                } else if(stopCondition == GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == GA.StopConditionType.FUNCTION_SLOPE || stopCondition == GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION) {
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
                    GA ga = new GA(numberOfPopulation, iterationLimit, crossoverProbability, mutationProbability, elitism, crossoverType, function, selectionType, sep, param);
                    ga.setStopConditionType(stopCondition);
                    ga.setRandom(random);
 
                    if(stopCondition == GA.StopConditionType.ACCEPTABLE_ERROR) {
                        ga.setConditionTarget(conditionTarget);
                        ga.setConditionError(conditionError);

                    } else if(stopCondition == GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT || stopCondition == GA.StopConditionType.FUNCTION_SLOPE || stopCondition == GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION) { 
                        ga.setConditionWindow(conditionWindow);
                        ga.setConditionError(conditionError);

                    }

                    log.append(ga.runGAFile(i));
                    evalList.add(ga.getGlobalBestEval());
                    if(ga.getGlobalBestEval() < bestEval) {
                        bestTest = i;
                        bestEval = ga.getGlobalBestEval();
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
    
    public static GA.StopConditionType getStopCondition() {
        String option;
        Scanner in = new Scanner(System.in);
        System.out.println("\nEscolha a condição de parada");
        System.out.println("1 - Somente número de iterações");
        System.out.println("2 - Parar em um erro aceitável");
        System.out.println("3 - Parar quando não houver melhorias");
        System.out.println("4 - Parar quando não houver melhorias na população");
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
                return GA.StopConditionType.ONLY_ITERATION;
            case "2":
                return GA.StopConditionType.ACCEPTABLE_ERROR;
            case "3":
                return GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT;
            case "4":
                return GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION;
            case "5":
                return GA.StopConditionType.FUNCTION_SLOPE;
            default:
                return GA.StopConditionType.ONLY_ITERATION;
        }
    }
    
    public static GA.StopConditionType parseStopCondition(String stopCondition) {
        switch(stopCondition) {
            case "ONLY_ITERATION":
                return GA.StopConditionType.ONLY_ITERATION;
            case "ACCEPTABLE_ERROR":
                return GA.StopConditionType.ACCEPTABLE_ERROR;
            case "NUMBER_OF_ITERATION_IMPROVEMENT":
                return GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT;
            case "NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION":
                return GA.StopConditionType.NUMBER_OF_ITERATION_IMPROVEMENT_POPULATION;
            case "FUNCTION_SLOPE":
                return GA.StopConditionType.FUNCTION_SLOPE;
            default:
                throw new RuntimeException("Stop Condition Type Not Found");
        }
    }
    
    public static GA.CrossoverType getCrossoverType() {
        String option;
        Scanner in = new Scanner(System.in);
        System.out.println("\nEscolha o tipo de crossover");
        System.out.println("1 - Média aritmética");
        System.out.println("2 - Média ponderada");
        System.out.println("3 - Um ponto de corte");
        System.out.println("4 - Dois pontos de corte");
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
                return GA.CrossoverType.ARITHMETIC_MEAN;
            case "2":
                return GA.CrossoverType.WEIGHTED_MEAN;
            case "3":
                return GA.CrossoverType.ONE_POINT;
            case "4":
                return GA.CrossoverType.TWO_POINT;
            default:
                return GA.CrossoverType.ONE_POINT;
        }
    }
    
    public static GA.CrossoverType parseCrossoverType(String crossoverType) {
        switch(crossoverType) {
            case "ARITHMETIC_MEAN":
                return GA.CrossoverType.ARITHMETIC_MEAN;
            case "WEIGHTED_MEAN":
                return GA.CrossoverType.WEIGHTED_MEAN;
            case "ONE_POINT":
                return GA.CrossoverType.ONE_POINT;
            case "TWO_POINT":
                return GA.CrossoverType.TWO_POINT;
            default:
                throw new RuntimeException("Crossover Type Not Found");
        }
    }
    
    public static GA.SelectionType getSelectionType() {
        String option;
        Scanner in = new Scanner(System.in);
        System.out.println("\nEscolha o tipo de seleção");
        System.out.println("1 - Método da roleta");
        System.out.println("2 - Método do torneio");
        System.out.print("[ENTRADA]: ");
        
        List<String> options = Arrays.asList("1","2");
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
                return GA.SelectionType.ROULETTE_WHEEL;
            case "2":
                return GA.SelectionType.TOURNAMENT;
            default:
                return GA.SelectionType.TOURNAMENT;
        }
    }
    
    public static GA.SelectionType parseSelectionType(String selectionType) {
        switch(selectionType) {
            case "ROULETTE_WHEEL":
                return GA.SelectionType.ROULETTE_WHEEL;
            case "TOURNAMENT":
                return GA.SelectionType.TOURNAMENT;
            default:
                throw new RuntimeException("Selection Type Not Found");
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
