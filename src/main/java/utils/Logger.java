/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author meimarcel
 */
public class Logger {
    private final String OS = System.getProperty("os.name").toLowerCase();
    public final String ANSI_RESET = (OS.contains("windows")) ? "" : "\u001b[0m";
    public final String ANSI_BLACK = (OS.contains("windows")) ? "" : "\u001b[30m";
    public final String ANSI_RED = (OS.contains("windows")) ? "" : "\u001b[31m";
    public final String ANSI_GREEN = (OS.contains("windows")) ? "" : "\u001b[32m";
    public final String ANSI_YELLOW = (OS.contains("windows")) ? "" : "\u001b[33m";
    public final String ANSI_BLUE = (OS.contains("windows")) ? "" : "\u001B[34m";
    public final String ANSI_PURPLE = (OS.contains("windows")) ? "" : "\u001b[35m";
    public final String ANSI_CYAN = (OS.contains("windows")) ? "" : "\u001b[36m";
    public final String ANSI_WHITE = (OS.contains("windows")) ? "" : "\u001b[37m";
    
    public String header() {
        StringBuilder header = new StringBuilder();
        System.out.print(this.ANSI_GREEN);
        header.append("    ##          ##     #           #         ######\n");
        header.append("    # #        # #     #           #        #      #\n");
        header.append("    #  #      #  #     #           #       #        #\n");
        header.append("    #   #    #   #     #           #      #          #\n");
        header.append("    #    #  #    #     #           #     #            #\n");
        header.append("    #     ##     #     #############     ##############\n");
        header.append("    #            #     #           #     #            #\n");
        header.append("    #            #     #           #     #            #\n");
        header.append("    #            #     #           #     #            #\n");
        header.append("    #            #     #           #     #            #\n");
        header.append("    #            #     #           #     #            #\n");
        header.append("---------------------------------------------------------\n");
        header.append("Developed by Mei Marcel - Email: mei.marcel05@gmail.com\n");
        header.append("---------------------------------------------------------\n");
        header.append(" ARGUMENTS LIST\n");
        header.append("\n");
        header.append("-PlotGraph=true | To save the plotted graphs.\n");
        header.append("-SaveLog=true | To save all the logs.\n");
        header.append("-Seed=<number> | To set a seed to the random numbers generator. Replace <number> with a number of type long.\n");
        header.append("-Algorithm=<algorithm> | To set the algorithm. Replace <algorithm> with the initials of of the algorithm.\n");
        header.append("-FilePath=<filePath> | To set the json file path to make a batch of tests. Replace <filePath> with the path of the file.\n");
        System.out.print(header);
        System.out.println(this.ANSI_RESET);
        
        return header.toString();
    }
    
    public String headerPSO() {
        StringBuilder header = new StringBuilder();
        System.out.print(this.ANSI_GREEN);
        header.append("         #########       #########         ####\n");
        header.append("         #        #     #                #     #\n");
        header.append("         #         #   #                #       #\n");
        header.append("         #         #   #               #         #\n");
        header.append("         #        #     #             #           #\n");
        header.append("         ########        #######      #           #\n");
        header.append("         #                      #     #           #\n");
        header.append("         #                       #     #         #\n");
        header.append("         #                       #      #       #\n");
        header.append("         #                      #        #     #\n");
        header.append("         #             #########          ####\n");
        header.append("---------------------------------------------------------\n");
        header.append("             PARTICLE SWARM OPTIMIZATION\n");
        header.append("Developed by Mei Marcel - Email: mei.marcel05@gmail.com\n");
        header.append("---------------------------------------------------------\n");
        System.out.print(header);
        System.out.println(this.ANSI_RESET);
        
        return header.toString();
    }
    
    public String headerGA() {
        StringBuilder header = new StringBuilder();
        System.out.print(this.ANSI_GREEN);
        header.append("               ########         ######\n");
        header.append("             #                 #      #\n");
        header.append("            #                 #        #\n");
        header.append("           #                 #          #\n");
        header.append("          #                 #            #\n");
        header.append("          #     ########    ##############\n");
        header.append("          #     #      #    #            #\n");
        header.append("          #            #    #            #\n");
        header.append("           #           #    #            #\n");
        header.append("            #          #    #            #\n");
        header.append("             ###########    #            #\n");
        header.append("---------------------------------------------------------\n");
        header.append("                    GENETIC ALGORITHM\n");
        header.append("Developed by Mei Marcel - Email: mei.marcel05@gmail.com\n");
        header.append("---------------------------------------------------------\n");
        System.out.print(header);
        System.out.println(this.ANSI_RESET);
        
        return header.toString();
    }
    
    public String headerSGO() {
        StringBuilder header = new StringBuilder();
        System.out.print(this.ANSI_GREEN);
        header.append("        #########         ########           ####\n");
        header.append("       #                #                  #     #\n");
        header.append("      #                #                  #       #\n");
        header.append("      #               #                  #         #\n");
        header.append("       #             #                  #           #\n");
        header.append("        #######      #     ########     #           #\n");
        header.append("               #     #     #      #     #           #\n");
        header.append("                #    #            #      #         #\n");
        header.append("                #     #           #       #       #\n");
        header.append("               #       #          #        #     #\n");
        header.append("      #########         ###########         ####\n");
        header.append("---------------------------------------------------------\n");
        header.append("             SOCCER GAME OPTIMIZATION\n");
        header.append("Developed by Mei Marcel - Email: mei.marcel05@gmail.com\n");
        header.append("---------------------------------------------------------\n");
        System.out.print(header);
        System.out.println(this.ANSI_RESET);
        
        return header.toString();
    }
    
    public String info(String message) {
        String msg = "[INFO] "+message;
        System.out.print(this.ANSI_GREEN+msg+this.ANSI_RESET);
        return msg;
    }
    
    public String error(String message) {
        String msg = "[ERROR] "+message;
        System.out.print(this.ANSI_RED+msg+this.ANSI_RESET);
        return msg;
    }
    
    public String warning(String message) {
        String msg = "[WARNING] "+message;
        System.out.print(this.ANSI_YELLOW+msg+this.ANSI_RESET);
        return msg;
    }
    
    public String message(String message) {
        String msg = "[MESSAGE] "+message;
        System.out.print(this.ANSI_CYAN+msg+this.ANSI_RESET);
        return msg;
    }
    
    public String white(String message) {
        System.out.print(this.ANSI_WHITE+message+this.ANSI_RESET);
        return message;
    }
}
