/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package libeps;

public class libeps implements libepsConstants {
  public static eps new_EPS(String arquivo) {
    long cPtr = libepsJNI.new_EPS(arquivo);
    return (cPtr == 0) ? null : new eps(cPtr, false);
  }

  public static void free_EPS(eps module_eps_system) {
    libepsJNI.free_EPS(eps.getCPtr(module_eps_system), module_eps_system);
  }

  public static void print_EPS(eps module_eps_system) {
    libepsJNI.print_EPS(eps.getCPtr(module_eps_system), module_eps_system);
  }

  public static void readAuctionInformation(eps module_eps_system, double baseTensao) {
    libepsJNI.readAuctionInformation(eps.getCPtr(module_eps_system), module_eps_system, baseTensao);
  }

  public static void save_state_EPS(eps module_eps_system) {
    libepsJNI.save_state_EPS(eps.getCPtr(module_eps_system), module_eps_system);
  }

  public static void load_state_EPS(eps module_eps_system) {
    libepsJNI.load_state_EPS(eps.getCPtr(module_eps_system), module_eps_system);
  }

  public static void save_state_Auction_EPS(eps module_eps_system) {
    libepsJNI.save_state_Auction_EPS(eps.getCPtr(module_eps_system), module_eps_system);
  }

  public static void load_state_Auction_EPS(eps module_eps_system) {
    libepsJNI.load_state_Auction_EPS(eps.getCPtr(module_eps_system), module_eps_system);
  }

  public static barra get_Bus(eps module_eps_system, int bus) {
    long cPtr = libepsJNI.get_Bus(eps.getCPtr(module_eps_system), module_eps_system, bus);
    return (cPtr == 0) ? null : new barra(cPtr, false);
  }

  public static infoLigacao get_Branch(eps module_eps_system, int bus_k, int bus_m) {
    long cPtr = libepsJNI.get_Branch(eps.getCPtr(module_eps_system), module_eps_system, bus_k, bus_m);
    return (cPtr == 0) ? null : new infoLigacao(cPtr, false);
  }

  public static void init_power_flow_parameters(param_fc param) {
    libepsJNI.init_power_flow_parameters(param_fc.getCPtr(param), param);
  }

  public static ret_fc power_flow(eps system, param_fc param) {
    return new ret_fc(libepsJNI.power_flow(eps.getCPtr(system), system, param_fc.getCPtr(param), param), true);
  }

  public static double losses(eps system) {
    return libepsJNI.losses(eps.getCPtr(system), system);
  }

  public static void print_state_wrt_power_flow(eps system) {
    libepsJNI.print_state_wrt_power_flow(eps.getCPtr(system), system);
  }

  public static double power_flow_equations_penalty(eps system, double tol, double penalty_exponent, double penalty_factor) {
    return libepsJNI.power_flow_equations_penalty(eps.getCPtr(system), system, tol, penalty_exponent, penalty_factor);
  }

  public static double voltage_magnitudes_limits_penalty(eps system, double penalty_exponent, double penalty_factor) {
    return libepsJNI.voltage_magnitudes_limits_penalty(eps.getCPtr(system), system, penalty_exponent, penalty_factor);
  }

  public static double reactive_generations_limits_penalty(eps system, double penalty_exponent, double penalty_factor) {
    return libepsJNI.reactive_generations_limits_penalty(eps.getCPtr(system), system, penalty_exponent, penalty_factor);
  }

  public static int number_decision_variables_optimal_reactive_dispatch(eps system) {
    return libepsJNI.number_decision_variables_optimal_reactive_dispatch(eps.getCPtr(system), system);
  }

  public static void lower_limits_decision_variables_optimal_reactive_dispatch(eps system, SWIGTYPE_p_double llimits, int n) {
    libepsJNI.lower_limits_decision_variables_optimal_reactive_dispatch(eps.getCPtr(system), system, SWIGTYPE_p_double.getCPtr(llimits), n);
  }

  public static void upper_limits_decision_variables_optimal_reactive_dispatch(eps system, SWIGTYPE_p_double ulimits, int n) {
    libepsJNI.upper_limits_decision_variables_optimal_reactive_dispatch(eps.getCPtr(system), system, SWIGTYPE_p_double.getCPtr(ulimits), n);
  }

  public static void set_decision_variables_optimal_reactive_dispatch(eps system, SWIGTYPE_p_double values, int n) {
    libepsJNI.set_decision_variables_optimal_reactive_dispatch(eps.getCPtr(system), system, SWIGTYPE_p_double.getCPtr(values), n);
  }

  public static double objective_function_optimal_reactive_dispatch(eps system, param_fc param) {
    return libepsJNI.objective_function_optimal_reactive_dispatch(eps.getCPtr(system), system, param_fc.getCPtr(param), param);
  }

  public static int sequence_path_to_optimum_by_sensitivities(eps system, param_fc param, SWIGTYPE_p_p_int sequence) {
    return libepsJNI.sequence_path_to_optimum_by_sensitivities(eps.getCPtr(system), system, param_fc.getCPtr(param), param, SWIGTYPE_p_p_int.getCPtr(sequence));
  }

  public static void verify_sequence_path(eps system, param_fc param, int nAdjusts, SWIGTYPE_p_int sequence) {
    libepsJNI.verify_sequence_path(eps.getCPtr(system), system, param_fc.getCPtr(param), param, nAdjusts, SWIGTYPE_p_int.getCPtr(sequence));
  }

}
