package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.emboss.jemboss.JembossJarUtil;
import org.emboss.jemboss.gui.startup.ProgList;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBrokerFactory;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaWorkflowSubmission;

// IO Imports
import java.io.*;

// Network Imports
import java.net.URL;

import uk.ac.mrc.hgmp.taverna.retsina.ProgramSelectionPanel;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraph;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraphPanel;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.lang.SecurityManager;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;



/**
*
* GUI front editor for a ScuflModel
*
*/
public class Retsina extends JApplet 
{
    
  private ScuflGraphPanel graphPanel;

  public static void main(String[] args) 
  {
    Retsina retsinaPane = new Retsina();
    retsinaPane.init();
 
    JFrame frame = new JFrame("Retsina");
    Dimension d = frame.getToolkit().getScreenSize();
    if(d.getWidth()<1024)
      d = new Dimension(795,600);
    else
      d = new Dimension(840,640);

    frame.getContentPane().add(retsinaPane);
    frame.setSize(d);
    frame.show();
  }
    
  public Retsina() 
  {
    super();
  }
  
  public void init() 
  {
    String wossname = null;

    try
    {
      JembossJarUtil jwoss = new JembossJarUtil("resources/wossname.jar");
      wossname = new String((byte[])jwoss.getElement("wossname.out"));
    }
    catch(Exception ex)
    {
      System.out.println("Failed to read wossname for menu construction");
    }
    
    JMenuBar progMenuBar = new JMenuBar();
    ProgList progs = new ProgList(wossname,null,progMenuBar);
    graphPanel = new ScuflGraphPanel(null,progs);
    JPanel westPanel = new ProgramSelectionPanel(wossname,graphPanel,
                                                 progs,progMenuBar);

    setJMenuBar(createMenuBar(graphPanel));
    // Put the components in the content pane
    Container contentpane = getContentPane();
    contentpane.add(graphPanel,BorderLayout.CENTER);
    contentpane.add(westPanel, BorderLayout.WEST);
  } 

  /**
   * Create the menubar
   */
  public JMenuBar createMenuBar(final ScuflGraphPanel graphPanel)
  {
     JMenuBar menuBar = new JMenuBar();

//file menu
     JMenu fileMenu = new JMenu("File");
     fileMenu.setMnemonic(KeyEvent.VK_F);

     JMenuItem newWorkFlow = new JMenuItem("New Workflow");
     newWorkFlow.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
         graphPanel.newWorkFlow();
       }
     });
     fileMenu.add(newWorkFlow);
     fileMenu.add(new JSeparator());

     JMenuItem loadExampleXScufl = new JMenuItem("Load XScufl Example");
     loadExampleXScufl.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
   
         try
         {
           BufferedInputStream workflow = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/XScufl_example.xml"));
           StringWriter sWriter = new StringWriter();
           while(workflow.available()>0) 
             sWriter.write(workflow.read());
           String xscufl = sWriter.toString();
           graphPanel.loadXScufl(xscufl);
         }
         catch(Exception exp){}
       }
     });
     fileMenu.add(loadExampleXScufl);

     JMenuItem loadXScufl = new JMenuItem("Load Workflow");
     loadXScufl.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
         try
         {
           SecurityManager sm = System.getSecurityManager();
           System.setSecurityManager(null);
           JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
           System.setSecurityManager(sm);
           int returnVal = fc.showOpenDialog(fc);

           if(returnVal == JFileChooser.APPROVE_OPTION)
           {
             File file = fc.getSelectedFile();
             
             BufferedInputStream workflow = new BufferedInputStream(new FileInputStream(file));
             StringWriter sWriter = new StringWriter();
             while(workflow.available()>0)
               sWriter.write(workflow.read());
             String xscufl = sWriter.toString();
             graphPanel.loadXScufl(xscufl);
           }
         }
         catch(Exception exp){}
       }
     });
     fileMenu.add(loadXScufl);

     JMenuItem saveXScufl = new JMenuItem("Save Workflow");
     saveXScufl.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
         SecurityManager sm = System.getSecurityManager();
         System.setSecurityManager(null);
         JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
         System.setSecurityManager(sm);
         int returnVal = fc.showSaveDialog(fc);

         if (returnVal == JFileChooser.APPROVE_OPTION)
         {
           File file = fc.getSelectedFile();
           try
           {
             FileWriter out = new FileWriter(file);
             out.write(graphPanel.getXScufl());
             out.close();
           }
           catch(IOException ioe)
           {}
         }
       }
     });
     fileMenu.add(saveXScufl);

     fileMenu.add(new JSeparator());
     JMenuItem fileRunScufl = new JMenuItem("Run workflow");
     fileRunScufl.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
         submitWorkFlow(graphPanel.getCurrentJGraph());
       }
     });
     fileMenu.add(fileRunScufl);
     fileRunScufl.setEnabled(false);

     fileMenu.add(new JSeparator());
     JMenuItem fileMenuExit = new JMenuItem("Exit");
     fileMenuExit.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
         System.exit(0);
       }
     });
     fileMenu.add(fileMenuExit);
     menuBar.add(fileMenu);

//view menu
     JMenu viewMenu = new JMenu("View");
     viewMenu.setMnemonic(KeyEvent.VK_V);
     JMenuItem viewXScufl = new JMenuItem("Display XScufl");
     viewXScufl.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
         JFrame f = new JFrame("XScufl");
         JTextPane xscufl = new JTextPane();
         JPanel pscroll = new JPanel(new BorderLayout());
         JScrollPane rscroll = new JScrollPane(pscroll);
         rscroll.getViewport().setBackground(Color.white);
         xscufl.setText(graphPanel.getXScufl());
         xscufl.setEditable(false);
         pscroll.add(xscufl);
         JOptionPane jop = new JOptionPane();
         rscroll.setPreferredSize(new Dimension(400,180));

         xscufl.setCaretPosition(0);
         f.getContentPane().add(rscroll);
         f.pack();
         f.setVisible(true);
//       jop.showMessageDialog(null,rscroll,"XScufl",
//                           JOptionPane.PLAIN_MESSAGE);
       }
     });
     viewMenu.add(viewXScufl);
     menuBar.add(viewMenu);

//help menu
     JMenu helpMenu = new JMenu("Help");
     helpMenu.setMnemonic(KeyEvent.VK_H);
     JMenuItem aboutMenu = new JMenuItem("About");
     aboutMenu.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
         URL inURL = getClass().getClassLoader().getResource("resources/retsina.html");
         JTextPane about = new JTextPane();
         JPanel pscroll = new JPanel(new BorderLayout());
         JScrollPane rscroll = new JScrollPane(pscroll);
         rscroll.getViewport().setBackground(Color.white);
 
         try
         {
           about.setPage(inURL);
         }
         catch(IOException ioe){}
         about.setEditable(false);
         pscroll.add(about);
         JOptionPane jop = new JOptionPane();
         rscroll.setPreferredSize(new Dimension(400,180));

         jop.showMessageDialog(null,rscroll,"About ",
                             JOptionPane.PLAIN_MESSAGE);
       }
     });
     helpMenu.add(aboutMenu);
     menuBar.add(helpMenu);

     return menuBar;
   }

   private void submitWorkFlow(ScuflGraph graph)
   {

     try{
          BufferedInputStream workflowspec = new BufferedInputStream(new FileInputStream("/people/tcarver/xx.scufl"));
           BufferedInputStream inData = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/input.xml"));

           StringWriter sWriter = new StringWriter();
       while(workflowspec.available()>0) {
         sWriter.write(workflowspec.read());
       }
       String wsflDefn = sWriter.toString();
       sWriter = new StringWriter();
       while(inData.available()>0) {
         sWriter.write(inData.read());
       }
       String input = sWriter.toString();

       System.out.println(" **************** HERE ****************");
       System.out.println("\n\n"+graph.getXScufl());
       TavernaWorkflowSubmission submit = new TavernaWorkflowSubmission(wsflDefn,input,"TestTavernaFlowBroker","http://www.it-innovation.soton.ac.uk/users");

       FlowBroker broker = FlowBrokerFactory.createFlowBroker("uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");
       TavernaFlowReceipt receipt = (TavernaFlowReceipt) broker.submitFlow(submit);
       //poll for status every 500ms
       boolean stop = false;
       String status ="UNKNOWN";
       while(!stop) {
         //retrieve the status
         status = receipt.getStatusString();
         if(status.equals("COMPLETE") || status.equals("FAILED"))
           stop = true;
         try {
           Thread.sleep(500);
         }
         catch(InterruptedException ex) {
         }
       }
       if(status.equals("FAILED"))
           System.out.println("Error message: " + receipt.getErrorMessage());
       System.out.println("Emboss Workflow has finished with status: " + status); 
        }
        catch(Exception ex) {
                ex.printStackTrace();
        }
   //try
   //{
       // As per:
       // uk.ac.soton.itinnovation.taverna.enactor.broker.test.TestTavernaFlowBroker
   //  String xscufl = graph.getXScufl();
//     System.out.println(xscufl);
   //  BufferedInputStream inData = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/input.xml"));

   //  StringWriter sWriter = new StringWriter();
  //   while(inData.available()>0) 
   //    sWriter.write(inData.read());
      
   //  String inputData = sWriter.toString();
   //  TavernaWorkflowSubmission submit = new TavernaWorkflowSubmission(
  //                    xscufl,inputData,"TestTavernaFlowBroker",
   //                   "http://www.it-innovation.soton.ac.uk/users");
//                      "http://www.hgmp.mrc.ac.uk/users");
  //   FlowBroker broker = FlowBrokerFactory.createFlowBroker(
  //            "uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");
  //   TavernaFlowReceipt receipt = (TavernaFlowReceipt)broker.submitFlow(submit);

       //poll for status every 500ms
  //   boolean stop = false;
  //   String status ="UNKNOWN";
  //   while(!stop) {
  //     //retrieve the status
  //     status = receipt.getStatusString();
  //     if(status.equals("COMPLETE") || status.equals("FAILED"))
  //       stop = true;
  //     try {
  //       Thread.sleep(500);
  //     }
  //     catch(InterruptedException ex) {
  //     }
  //   }
  // }
  // catch(Exception ex) 
  // {
  //   ex.printStackTrace();
  // }   
   }

}

