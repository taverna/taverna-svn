package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Dimension;
import java.awt.Color;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileWriter;
import javax.swing.*;

import org.emboss.jemboss.JembossJarUtil;
import org.emboss.jemboss.gui.startup.ProgList;

import uk.ac.mrc.hgmp.taverna.retsina.ProgramSelectionPanel;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraphPanel;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraph;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.net.URL;


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

     JMenuItem loadXScufl = new JMenuItem("Load XScufl Example");
     loadXScufl.addActionListener(new ActionListener()
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

     JMenuItem fileRunScufl = new JMenuItem("Run workflow");
     fileRunScufl.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
         submitWorkFlow(graphPanel.getCurrentJGraph());
       }
     });
     fileMenu.add(fileRunScufl);


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
         jop.showMessageDialog(null,rscroll,"XScufl",
                             JOptionPane.PLAIN_MESSAGE);
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
     String xscufl = graph.getXScufl();
     System.out.println(xscufl);
//   TavernaWorkflowSubmission submit = new TavernaWorkflowSubmission(
//              xscufl,inputData,"TestTavernaFlowBroker",
//              "http://www.hgmp.mrc.ac.uk/users");
//   FlowBroker broker = FlowBrokerFactory.createFlowBroker(
//              "uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");
//   TavernaFlowReceipt receipt = (TavernaFlowReceipt)broker.submitFlow(submit);

   }

}

