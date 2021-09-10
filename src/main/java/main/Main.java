/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import ga.MainGA;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import pso.MainPSO;
import sgo.MainSGO;
import utils.Logger;

/**
 *
 * @author meimarcel
 */
public class Main {
    
    private static boolean plotGraph = false;
    private static boolean saveLog = false;
    private static boolean error = false;
    private static String algorithm = "";
    private static String filePath = "";
    private static long seed;
    private static Logger LOGGER = new Logger();
    
    static {
      try {
        //Load the OPF lib
        System.load("/home/meimarcel/Documentos/Facul/TCC/FPO/libeps/libeps.so");
      } catch (UnsatisfiedLinkError e) {
        System.err.println("Native code library failed to load.\n" + e);
        System.exit(1);
      }
    }
      
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        
        seed = (new Random()).nextLong();
        StringBuilder log = new StringBuilder();
        log.append(LOGGER.header());
        
        if(args.length > 0) {
            for(String var : args) {
                setVariables(var);
            }
        }
        
        if(error) System.exit(1);
        
        if(!filePath.equals("")) {
            switch(algorithm) {
                case "GA":
                    MainGA.runFile(plotGraph, saveLog, seed, log.toString(), filePath);
                    break;
                case "PSO":
                    MainPSO.runFile(plotGraph, saveLog, seed, log.toString(), filePath);
                    break;
                case "SGO":
                    MainSGO.runFile(plotGraph, saveLog, seed, log.toString(), filePath);
            }
        } else {
            if(algorithm.equals("")) {
                List<String> algorithms = Arrays.asList("0","1","2", "3");
                while(true) {
                    System.out.println("\nEscolha um algoritmo");
                    System.out.println("1 - GENETIC ALGORITHM (GA)");
                    System.out.println("2 - PARTICLE SWARM OPTIMIZATION (PSO)");
                    System.out.println("3 - SOCCER GAME OPTIMIZATION (SGO)");
                    System.out.println("0 - CANCELAR");
                    System.out.print("[ENTRADA]: ");
                    algorithm = in.nextLine();
                    algorithm = algorithm.trim();
                    if(!algorithms.contains(algorithm)) {
                        LOGGER.error("Opção inválida\n");
                        System.out.print("[ENTRADA]: ");
                    } else {
                        switch(algorithm) {
                            case "1":
                                algorithm = "GA";
                                break;
                            case "2":
                                algorithm = "PSO";
                                break;
                            case "3":
                                algorithm = "SGO";
                                break;
                        }
                        break;
                    }
                }
            }
            
            switch(algorithm) {
                case "GA":
                    MainGA.run(plotGraph, saveLog, seed, log.toString());
                    break;
                case "PSO":
                    MainPSO.run(plotGraph, saveLog, seed, log.toString());
                    break;
                case "SGO":
                    MainSGO.run(plotGraph, saveLog, seed, log.toString());
            }
        }
    }
    
    public static void setVariables(String var) {
        String values[] = var.split("=");
        
        if(values.length != 2) {
            LOGGER.error("Bad Format in '"+var+"'\n");
            error = true;
            return;
        }
        
        if(values[0].equals("-PlotGraph")) {
            if(values[1].toLowerCase().equals("true") || values[1].equals("1") || 
                    values[1].toLowerCase().equals("false") || values[1].equals("0")) {
                plotGraph = (values[1].toLowerCase().equals("true") || values[1].equals("1"));
                LOGGER.info("Plot Graph Activated\n");
            } else {
                LOGGER.error("Invalid value in '"+var+"'\n");
                error = true;
            }
            
        } else if(values[0].equals("-SaveLog")) {
            if(values[1].toLowerCase().equals("true") || values[1].equals("1") || 
                    values[1].toLowerCase().equals("false") || values[1].equals("0")) {
                saveLog = (values[1].toLowerCase().equals("true") || values[1].equals("1")); 
                LOGGER.info("Logs will be saved\n");
            } else {
                LOGGER.error("Invalid value in '"+var+"'\n");
                error = true;
            }
            
        } else if(values[0].equals("-Seed")) {
            try {
                seed = Long.parseLong(values[1]);
                LOGGER.info("Seed setted as "+seed+"\n");
            } catch(NumberFormatException e) {
                LOGGER.error("Invalid value in '"+var+"'\n");
                error = true;
            }
        } else if(values[0].equals("-Algorithm")) {
            if(values[1].toUpperCase().equals("GA") || values[1].toUpperCase().equals("PSO") || 
                    values[1].toUpperCase().equals("SGO")) {
                algorithm = values[1]; 
                LOGGER.info(values[1].toUpperCase()+" selected\n");
            } else {
                LOGGER.error("Invalid value in '"+var+"'\n");
                error = true;
            }
            
        } else if(values[0].equals("-FilePath")) {
            if(!values[1].trim().equals("") && values[1].trim().endsWith(".json")) {
                filePath = values[1]; 
                LOGGER.info("Path setted: "+filePath+"\n");
            } else {
                LOGGER.error("Invalid value in '"+var+"'\n");
                error = true;
            }
            
        } else {
            LOGGER.error("Invalid argument '"+var+"'\n");
            error = true;
        }
    }
    
}
