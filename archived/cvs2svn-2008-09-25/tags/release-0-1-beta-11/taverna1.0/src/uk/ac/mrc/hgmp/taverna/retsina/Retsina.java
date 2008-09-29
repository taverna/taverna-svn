package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.emboss.jemboss.JembossJarUtil;
import org.emboss.jemboss.gui.startup.ProgList;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowState;

// IO Imports
import java.io.*;

// Network Imports
import java.net.URL;

import java.util.Map;

import uk.ac.mrc.hgmp.taverna.retsina.ProgramSelectionPanel;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraph;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraphCell;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraphPanel;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.lang.Object;
import java.lang.SecurityManager;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;



/**
*
* GUI front editor for a ScuflModel
*
*/
public class Retsina extends JPanel  
                          implements ScuflUIComponent
{
    public javax.swing.ImageIcon getIcon() {
	return null;
    }
  private ScuflGraphPanel graphPanel;
  private ScuflModel scuflModel;

  public Retsina() 
  {
    this(true);
  }

  public Retsina(boolean addMenuBar)
  {
    super(new BorderLayout());
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
    ProgList progs = new ProgList(wossname,progMenuBar);
    graphPanel = new ScuflGraphPanel(null,progs);
    JPanel westPanel = new ProgramSelectionPanel(wossname,graphPanel,
                                                 progs,progMenuBar);

    // Put the components in the content pane
    add(graphPanel,BorderLayout.CENTER);
    add(westPanel, BorderLayout.WEST);

    if(addMenuBar)
      add(createMenuBar(true), BorderLayout.NORTH);
  } 

  public ScuflGraph getScuflGraph()
  {
    return graphPanel.getCurrentJGraph();
  }

  /**
   * Remove existing processors and children and
   * create a new ScuflModel.
   */
  public void newWorkFlow()
  {
    ScuflGraph graph = getScuflGraph();
    // clear graph of old workflow
    Object[] cells = graph.getRoots();
    for(int i=0;i<cells.length;i++)
    {
      if(cells[i] instanceof ScuflGraphCell)
      {
        ScuflGraphCell cell = (ScuflGraphCell)cells[i];
        cell.removeAllChildren();
        graph.destroyProcessor(cell.getScuflProcessor());
      }
    }
                                                                                                              
    graph.getModel().remove(cells);
    graph.clearScuflModel();
  }

  /**
   *
   * Load in a new XScufl workflow.
   * @param xscufl The workflow definition to load in XScufl format
   *
   */
  public void loadXScufl(String xscufl)
  {
    newWorkFlow();
    
    // load in new workflow
    getScuflGraph().loadXScufl(xscufl);
  }


  /**
   * Create the menubar
   */
  public JMenuBar createMenuBar(final boolean closeMenu)
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
         newWorkFlow();
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
           loadXScufl(xscufl);
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
             getScuflGraph();
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
             out.write(getScuflGraph().getXScufl());
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
         submitWorkFlow();
       }
     });
     fileMenu.add(fileRunScufl);
//   fileRunScufl.setEnabled(false);

     fileMenu.add(new JSeparator());
     JMenuItem fileMenuExit = null;
     if(closeMenu)
       fileMenuExit = new JMenuItem("Close");
     else
       fileMenuExit = new JMenuItem("Exit");

     fileMenuExit.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
         if(closeMenu)
         {
           if(getParent().getParent().getParent().getParent().getParent().getParent() instanceof JInternalFrame)
             ((JInternalFrame)(getParent().getParent().getParent().getParent().getParent().getParent())).dispose();
           else
             setVisible(false);
         }
         else
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
         xscufl.setText(getScuflGraph().getXScufl());
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
         URL inURL = ((Object)this).getClass().getClassLoader().getResource("resources/retsina.html");
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

   private void submitWorkFlow()
   {
     ScuflGraph graph = getScuflGraph();

     try
     {
       EnactorProxy enactorProxy = new FreefluoEnactorProxy();
       WorkflowInstance workflowInstance = null;
       try
       {
//       BufferedInputStream workflowspec = new BufferedInputStream(new FileInputStream("./scufl_test.xml"));
//       StringWriter sWriter = new StringWriter();
//       while(workflowspec.available()>0) 
//         sWriter.write(workflowspec.read());
//       String scuflDefn = sWriter.toString();

//       String scuflDefn = graph.getXScufl();
         String input = graph.getDataSet().getDataSetString();
         System.out.println(input);
         Map inputMap = graph.getDataSet().getData();
         
         workflowInstance = enactorProxy.compileWorkflow(graph.getScuflModel(), inputMap,null);
       }
       catch(WorkflowSubmissionException e) {
         e.printStackTrace();
         // throw an exception, popup a dialog?
         return;
       }

       //poll for status every 500ms
       boolean stop = false;
       String status ="UNKNOWN";
       while(!stop) 
       {
         //retrieve the status
         status = workflowInstance.getStatus();
         if(status.equals(WorkflowState.COMPLETE_STATE) || status.equals(WorkflowState.FAILED_STATE))
           stop = true;
         try 
         {
           Thread.sleep(500);
         }
         catch(InterruptedException ex)
         {

         }
       }
     
       if(status.equals(WorkflowState.FAILED_STATE))
             System.out.println("Error message: " + workflowInstance.getErrorMessage());
       System.out.println("Emboss Workflow has finished with status: " + status);
       System.out.println("Output:\n\n" + workflowInstance.getOutputXMLString());

     }
     catch(Exception ex) 
     {
       ex.printStackTrace();
     }
  
   }

  // ScuflUIComponent
  /**
   * Directs the implementing component to bind to the
   * specified ScuflModel instance, refresh its internal
   * state from the model and commence listening to events,
   * maintaining its state as these events dictate.
   */
  public void attachToModel(ScuflModel scuflModel)
  {
    this.scuflModel = scuflModel;
    graphPanel.attachToModel(scuflModel);
  }
                                                                                                       
  /**
   * Directs the implementing component to detach from the
   * model, set its internal state to some suitable blank
   * (i.e. blank image, no text in a text field etc) and
   * desist from listening to model events.
   */
  public void detachFromModel(){}
                                                                                                       
  /**
   * Get the preferred name of this component, for titles
   * in windows etc.
   */
  public String getName(){ return "Retsina"; }

}

