/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.Label;
import de.erichseifert.gral.graphics.Orientation;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.ui.InteractivePanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

/**
 *
 * @author meimarcel
 */
public class Plotter extends Thread {
    
    List<Double> dataFit;
    List<double[]> dataMean;
    List<double[]> dataStandartDeviation;
    
    public Plotter(List<Double> dataFit, List<double[]> dataMean, List<double[]> dataStandartDeviation) {
        this.dataFit = dataFit;
        this.dataMean = dataMean;
        this.dataStandartDeviation = dataStandartDeviation;
    }
    
    @Override
    public void run() {
        PlotterGraph pltg1 = new PlotterGraph();
        pltg1.plot(this.dataFit, this.dataMean, this.dataStandartDeviation);
    }
        
    
    class PlotterGraph extends JFrame{
        private int width;
        private int height;
        
        public PlotterGraph() {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.width = (int) screenSize.getWidth();
            this.height = (int) screenSize.getHeight();
            setSize(width, height);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
        }

        public void plot(List<Double> dataFit, List<double[]> dataMean, List<double[]> dataStandartDeviation) {
            DataTable dataTable1 = new DataTable(Integer.class, Double.class);
            for(int i = 0; i < dataFit.size(); ++i) {
                dataTable1.add(i, dataFit.get(i));
            }
            DataSeries dataPlot1 = new DataSeries("Fit", dataTable1, 0, 1); 
            
            int dataMeanSize = dataMean.get(0).length;
            DataTable dataTable2[] = new DataTable[dataMeanSize];
            DataSeries dataPlot2[] = new DataSeries[dataMeanSize];
            for(int i = 0; i < dataMeanSize; ++i) {
                dataTable2[i] = new DataTable(Integer.class, Double.class);
            }
            for(int i = 0; i < dataMean.size(); ++i) {
                for(int j = 0; j < dataMeanSize; ++j) {
                    dataTable2[j].add(i, dataMean.get(i)[j]);
                }
            }
            for(int i = 0; i < dataMeanSize; ++i) {
                dataPlot2[i] = new DataSeries("x"+(i+1), dataTable2[i], 0, 1);
            }
            
            int dataStandardSize = dataStandartDeviation.get(0).length;
            DataTable dataTable3[] = new DataTable[dataStandardSize];
            DataSeries dataPlot3[] = new DataSeries[dataStandardSize];
            for(int i = 0; i < dataStandardSize; ++i) {
                dataTable3[i] = new DataTable(Integer.class, Double.class);
            }
            for(int i = 0; i < dataStandartDeviation.size(); ++i) {
                for(int j = 0; j < dataStandardSize; ++j) {
                    dataTable3[j].add(i, dataStandartDeviation.get(i)[j]);
                }
            }
            for(int i = 0; i < dataStandardSize; ++i) {
                dataPlot3[i] = new DataSeries("x"+(i+1), dataTable3[i], 0, 1);
            }
            
            
            JSplitPane splitPane1 = new JSplitPane();
            JSplitPane splitPane2 = new JSplitPane();
            JSplitPane splitPane3 = new JSplitPane();
            

            XYPlot plt1 = new XYPlot(dataPlot1);
            XYPlot plt2 = new XYPlot();
            XYPlot plt3 = new XYPlot();
            
            JPanel infoPanel = new JPanel();
            GridLayout grid = new GridLayout();
            grid.setColumns(3);
            grid.setRows(3);
            infoPanel.setLayout(grid);
            JTextPane infoLabel = new JTextPane();
            infoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            infoLabel.setEditable(false);
            infoLabel.setBackground(null);
            infoLabel.setFont(new Font(Font.SERIF, Font.PLAIN, 12));
            infoLabel.setText("DEVELOPED BY: Mei Marcel\nE-MAIL: mei.marcel05@gmail.com\nGITHUB: https://github.com/meimarcel \n\n- Use o mouse para interagir com os gráficos.\n- Para salvar clique como botão direito em um dos gráficos");
            infoPanel.add(new JLabel());
            infoPanel.add(new JLabel());
            infoPanel.add(new JLabel());
            infoPanel.add(new JLabel());            
            infoPanel.add(infoLabel);
            infoPanel.add(new JLabel());
            infoPanel.add(new JLabel());
            infoPanel.add(new JLabel());
            infoPanel.add(new JLabel());
            
            for(int i = 0; i < dataMeanSize; ++i) {
                plt2.add(dataPlot2[i]);
            }
            
            for(int i = 0; i < dataStandardSize; ++i) {
                plt3.add(dataPlot3[i]);
            }
            
            getContentPane().setLayout(new GridLayout());
            getContentPane().add(splitPane1);
            
            splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitPane1.setDividerLocation(this.height/2);
            splitPane1.setTopComponent(splitPane2);
            splitPane1.setBottomComponent(splitPane3);
            
            splitPane2.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            splitPane2.setDividerLocation(this.width/2);
            splitPane2.setLeftComponent(new InteractivePanel(plt1));
            splitPane2.setRightComponent(new InteractivePanel(plt2));
            
            splitPane3.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            splitPane3.setDividerLocation(this.width/2);
            splitPane3.setLeftComponent(new InteractivePanel(plt3));
            splitPane3.setRightComponent(infoPanel);
            
            Label plt1X = new Label("Iteration");
            Label plt1Y = new Label("Fit"); 
            plt1X.setColor(new Color(255,102,102));
            plt1Y.setColor(new Color(255,102,102));
            plt1.getTitle().setText("FIT OVER ITERATION");
            if(!System.getProperty("os.name").toLowerCase().contains("windows"))
                plt1.getTitle().setAlignmentY(20);
            plt1.setBackground(Color.WHITE);
            plt1.setLineRenderers(dataPlot1, new DefaultLineRenderer2D());
            plt1.getPointRenderers(dataPlot1).get(0).setColor(new Color(0.0f, 0.3f, 1.0f));
            plt1.getLineRenderers(dataPlot1).get(0).setColor(new Color(0.0f, 0.3f, 1.0f));
            plt1.getPointRenderers(dataPlot1).get(0).setShape(new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
            plt1.getLineRenderers(dataPlot1).get(0).setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, new float[] {3f, 3f}, 0.0f));
            plt1.setLegendVisible(true);
            plt1.getLegend().setOrientation(Orientation.HORIZONTAL);
            plt1.getAxisRenderer(XYPlot.AXIS_X).setLabel(plt1X);
            plt1.getAxisRenderer(XYPlot.AXIS_Y).setLabel(plt1Y);
            
            Label plt2X = new Label("Iteration");
            Label plt2Y = new Label("Mean"); 
            plt2X.setColor(new Color(255,102,102));
            plt2Y.setColor(new Color(255,102,102));
            plt2.getTitle().setText("MEAN OVER ITERATION");
            if(!System.getProperty("os.name").toLowerCase().contains("windows"))
                plt2.getTitle().setAlignmentY(20);
            plt2.setBackground(Color.WHITE);
            for(int i = 0; i < dataMeanSize; ++i) {
                plt2.setLineRenderers(dataPlot2[i], new DefaultLineRenderer2D());
                plt2.getPointRenderers(dataPlot2[i]).get(0).setColor(getColors(i));
                plt2.getLineRenderers(dataPlot2[i]).get(0).setColor(getColors(i));
                plt2.getLineRenderers(dataPlot2[i]).get(0).setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, new float[] {3f, 3f}, 0.0f));
                plt2.getPointRenderers(dataPlot2[i]).get(0).setShape(new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
            }
            plt2.setLegendVisible(true);
            plt2.getLegend().setOrientation(Orientation.HORIZONTAL);
            plt2.getAxisRenderer(XYPlot.AXIS_X).setLabel(plt2X);
            plt2.getAxisRenderer(XYPlot.AXIS_Y).setLabel(plt2Y);
            
            Label plt3X = new Label("Iteration");
            Label plt3Y = new Label("Standar Deviation"); 
            plt3X.setColor(new Color(255,102,102));
            plt3Y.setColor(new Color(255,102,102));
            plt3.getTitle().setText("STANDAR DEVIATION OVER ITERATION");
            if(!System.getProperty("os.name").toLowerCase().contains("windows"))
                plt3.getTitle().setAlignmentY(20);
            plt3.setBackground(Color.WHITE);
            for(int i = 0; i < dataStandardSize; ++i) {
                plt3.setLineRenderers(dataPlot3[i], new DefaultLineRenderer2D());
                plt3.getPointRenderers(dataPlot3[i]).get(0).setColor(getColors(i));
                plt3.getLineRenderers(dataPlot3[i]).get(0).setColor(getColors(i));
                plt3.getLineRenderers(dataPlot3[i]).get(0).setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, new float[] {3f, 3f}, 0.0f));
                plt3.getPointRenderers(dataPlot3[i]).get(0).setShape(new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
            }
            plt3.setLegendVisible(true);
            plt3.getLegend().setOrientation(Orientation.HORIZONTAL);
            plt3.getAxisRenderer(XYPlot.AXIS_X).setLabel(plt3X);
            plt3.getAxisRenderer(XYPlot.AXIS_Y).setLabel(plt3Y);
            
            setVisible(true);
        }
        
        private Color getColors(int i) {
            switch(i) {
                case 0:
                    return Color.BLUE;
                case 1:
                    return Color.ORANGE;
                case 2:
                    return Color.CYAN;
                case 3:
                    return Color.DARK_GRAY;
                case 4:
                    return Color.GRAY;
                case 5:
                    return Color.LIGHT_GRAY;
                case 6:
                    return Color.MAGENTA;
                case 7:
                    return Color.BLACK;
                case 8:
                    return Color.PINK;
                case 9:
                    return Color.RED;
                case 10:
                    return Color.YELLOW;
                default:
                    Random rand = new Random();
                    return new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
            }
        }
        
        @Override
        public String getTitle() {
            return "Plots";
        }
    }
}
