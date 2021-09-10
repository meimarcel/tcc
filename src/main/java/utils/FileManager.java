/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
/**
 *
 * @author marce
 */
public class FileManager {
    
    private static Logger LOGGER = new Logger();
    
    public static String[] Read(String path){
        String[] conteudo;
        String aux = "";
        
        try {
            FileReader arq = new FileReader(path);
            BufferedReader lerArq = new BufferedReader(arq);
            String linha="";
            try {
                linha = lerArq.readLine();
                int i = 0;
                while(linha!=null){
                    aux += linha+";";
                    linha = lerArq.readLine();
                    i++;
                }
                arq.close();
                conteudo = aux.split(";");
                return conteudo;
            } catch (IOException ex) {
                LOGGER.error("Não foi possível ler o arquivo!\n");
                return null;
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error("Erro: Arquivo não encontrado!\n");
            return null;
        }
    }
    
    public static boolean Write(String path, String fileName, String Texto){
        try {
            new File(path).mkdirs();
            FileWriter arq = new FileWriter(path+"/"+fileName);
            PrintWriter gravarArq = new PrintWriter(arq);
            gravarArq.println(Texto);
            gravarArq.close();
            LOGGER.info("Log salvo em '"+path+"'\n");
            return true;
        }catch(IOException e){
            LOGGER.error(e.getMessage()+" '"+path+"'\n");
            return false;
        }
    }
    
    
}
