/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import libeps.*;

/**
 *
 * @author meimarcel
 */
public class Function {
    private FunctionType functionType;
    
    public Function(FunctionType functionType) {
        this.functionType = functionType;
    }
    
    public double fit(double[] x, param_fc param, eps sep) {
        switch(this.functionType) {
            case ARQ_STAGG_5:
                return this.ARQ_STAGG_5(x, param, sep);
            case ARQ_6_BUS:
                return this.ARQ_6_BUS(x, param, sep);
            case ARQ_IEEE_14_BUS:
                return this.ARQ_IEEE_14_BUS(x, param, sep);
            case ARQ_IEEE_30_BUS:
                return this.ARQ_IEEE_30_BUS(x, param, sep);
            default:
                return this.ARQ_STAGG_5(x, param, sep);
        }
    }
    
    public int getNumberOfVariables() {
        switch(this.functionType) {
            case ARQ_STAGG_5:
                return this.ARQ_STAGG_5_VARIABLES;
            case ARQ_6_BUS:
                return this.ARQ_6_BUS_VARIABLES;
            case ARQ_IEEE_14_BUS:
                return this.ARQ_IEEE_14_BUS_VARIABLES;
            case ARQ_IEEE_30_BUS:
                return this.ARQ_IEEE_30_BUS_VARIABLES;
            default:
                return this.ARQ_STAGG_5_VARIABLES;
        }
    }
    
    public String getStringFunction() {
        switch(this.functionType) {
            case ARQ_STAGG_5:
                return ARQ_STAGG_5_STRING;
            case ARQ_6_BUS:
                return ARQ_6_BUS_STRING;
            case ARQ_IEEE_14_BUS:
                return ARQ_IEEE_14_BUS_STRING;
            case ARQ_IEEE_30_BUS:
                return ARQ_IEEE_30_BUS_STRING;
            default:
                return ARQ_STAGG_5_STRING;
        }
    }
    
    public FunctionType getFunctionType() {
        return this.functionType;
    }
            
            
            
    
    private final int ARQ_STAGG_5_VARIABLES = 2;
    public static final String ARQ_STAGG_5_STRING = "ARQ_STAGG_5";
    private double ARQ_STAGG_5(double[] x, param_fc param, eps sep) {
        
        doubleArray decision = new doubleArray(ARQ_STAGG_5_VARIABLES);
        for(int i = 0; i < ARQ_STAGG_5_VARIABLES; ++i) {
            decision.setitem(i, x[i]);
        }
        
        //SETANDO OS VALORES DAS VARIAVEIS DE DECISAO, DE FATO
        libeps.set_decision_variables_optimal_reactive_dispatch(sep, decision.cast(), ARQ_STAGG_5_VARIABLES);
        
        return calculate(param, sep);
    }
    
    private final int ARQ_6_BUS_VARIABLES = 3;
    public static final String ARQ_6_BUS_STRING = "ARQ_6_BUS";
    private double ARQ_6_BUS(double[] x, param_fc param, eps sep) {
        
        doubleArray decision = new doubleArray(ARQ_6_BUS_VARIABLES);
        for(int i = 0; i < ARQ_6_BUS_VARIABLES; ++i) {
            decision.setitem(i, x[i]);
        }
        
        //SETANDO OS VALORES DAS VARIAVEIS DE DECISAO, DE FATO
        libeps.set_decision_variables_optimal_reactive_dispatch(sep, decision.cast(), ARQ_6_BUS_VARIABLES);
        
        return calculate(param, sep);
    }
    
    private final int ARQ_IEEE_14_BUS_VARIABLES = 9;
    public static final String ARQ_IEEE_14_BUS_STRING = "ARQ_IEEE_14_BUS";
    private double ARQ_IEEE_14_BUS(double[] x, param_fc param, eps sep) {
        
        doubleArray decision = new doubleArray(ARQ_IEEE_14_BUS_VARIABLES);
        for(int i = 0; i < ARQ_IEEE_14_BUS_VARIABLES; ++i) {
            decision.setitem(i, x[i]);
        }
        
        //SETANDO OS VALORES DAS VARIAVEIS DE DECISAO, DE FATO
        libeps.set_decision_variables_optimal_reactive_dispatch(sep, decision.cast(), ARQ_IEEE_14_BUS_VARIABLES);
        
        return calculate(param, sep);
    }
    
    private final int ARQ_IEEE_30_BUS_VARIABLES = 12;
    public static final String ARQ_IEEE_30_BUS_STRING = "ARQ_IEEE_30_BUS";
    private double ARQ_IEEE_30_BUS(double[] x, param_fc param, eps sep) {
        
        doubleArray decision = new doubleArray(ARQ_IEEE_30_BUS_VARIABLES);
        for(int i = 0; i < ARQ_IEEE_30_BUS_VARIABLES; ++i) {
            decision.setitem(i, x[i]);
        }
        
        //SETANDO OS VALORES DAS VARIAVEIS DE DECISAO, DE FATO
        libeps.set_decision_variables_optimal_reactive_dispatch(sep, decision.cast(), ARQ_IEEE_30_BUS_VARIABLES);
        
        return calculate(param, sep);
    }
    
    public double calculate(param_fc param, eps sep) {
        //AQUI ESTAMOS CALCULANDO A FUNCAO OBJETIVO DO PROBLEMA
        double function_with_penalties = libeps.objective_function_optimal_reactive_dispatch(sep,param);
        //ADICIONANDO PENALIDADES REFERENTES AS RESTRICOES DE IGUALDADE DO FLUXO DE CARGA
        function_with_penalties += libeps.power_flow_equations_penalty(sep,param.getTol(),2,1E3);
        //ADICIONANDO PENALIDADES REFERENTES AS RESTRICOES DE DESIGUALDADE DOS LIMITES DE Vs
        function_with_penalties += libeps.voltage_magnitudes_limits_penalty(sep,2,1E3);
        //ADICIONANDO PENALIDADES REFERENTES AS RESTRICOES DE DESIGUALDADE DOS LIMITES DE Qgs
        function_with_penalties += libeps.reactive_generations_limits_penalty(sep,2,1E3);
        
        return function_with_penalties;
    }
    
    public enum FunctionType {
        ARQ_STAGG_5,
        ARQ_6_BUS,
        ARQ_IEEE_14_BUS,
        ARQ_IEEE_30_BUS,
        ;
    }
}
