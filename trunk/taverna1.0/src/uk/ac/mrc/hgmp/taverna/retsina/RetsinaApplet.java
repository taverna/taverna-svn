package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.Dimension;
import javax.swing.JApplet;
import javax.swing.JFrame;
import org.embl.ebi.escience.scufl.ScuflModel;

import uk.ac.mrc.hgmp.taverna.retsina.Retsina;
import java.lang.String;



/**
*
* GUI front editor for a ScuflModel
*
*/
public class RetsinaApplet extends JApplet
{
    
  private static Retsina retsinaPane;

  public static void main(String[] args) 
  {
    retsinaPane = new Retsina(false);
    retsinaPane.attachToModel(new ScuflModel());
    JFrame frame = new JFrame("Retsina");

    Dimension d = frame.getToolkit().getScreenSize();
    if(d.getWidth()<1024)
      d = new Dimension(795,600);
    else
      d = new Dimension(840,640);

    frame.setJMenuBar(retsinaPane.createMenuBar(false));
    frame.getContentPane().add(retsinaPane);
    frame.setSize(d);
    frame.show();
  }
    
  public RetsinaApplet()
  {
  }

  public void init() 
  {
  } 

  /**
   * Create the menubar
   */
//public static JMenuBar createMenuBar()
//{
//   JMenuBar menuBar = new JMenuBar();

//file menu
//   JMenu fileMenu = new JMenu("File");
//   fileMenu.setMnemonic(KeyEvent.VK_F);

//   JMenuItem newWorkFlow = new JMenuItem("New Workflow");
//   newWorkFlow.addActionListener(new ActionListener()
//   {
//     public void actionPerformed(ActionEvent e)
//     {
//       retsinaPane.newWorkFlow();
//     }
//   });
//   fileMenu.add(newWorkFlow);
//   fileMenu.add(new JSeparator());

//   JMenuItem loadExampleXScufl = new JMenuItem("Load XScufl Example");
//   loadExampleXScufl.addActionListener(new ActionListener()
//   {
//     public void actionPerformed(ActionEvent e)
//     {
// 
//       try
//       {
//         BufferedInputStream workflow = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/XScufl_example.xml"));
//         StringWriter sWriter = new StringWriter();
//         while(workflow.available()>0) 
//           sWriter.write(workflow.read());
//         String xscufl = sWriter.toString();
//         retsinaPane.loadXScufl(xscufl);
//       }
//       catch(Exception exp){}
//     }
//   });
//   fileMenu.add(loadExampleXScufl);

//   JMenuItem loadXScufl = new JMenuItem("Load Workflow");
//   loadXScufl.addActionListener(new ActionListener()
//   {
//     public void actionPerformed(ActionEvent e)
//     {
//       try
//       {
//         SecurityManager sm = System.getSecurityManager();
//         System.setSecurityManager(null);
//         JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
//         System.setSecurityManager(sm);
//         int returnVal = fc.showOpenDialog(fc);

//         if(returnVal == JFileChooser.APPROVE_OPTION)
//         {
//           File file = fc.getSelectedFile();
//           
//           BufferedInputStream workflow = new BufferedInputStream(new FileInputStream(file));
//           StringWriter sWriter = new StringWriter();
//           while(workflow.available()>0)
//             sWriter.write(workflow.read());
//           String xscufl = sWriter.toString();
//           retsinaPane.getScuflGraph();
//         }
//       }
//       catch(Exception exp){}
//     }
//   });
//   fileMenu.add(loadXScufl);

//   JMenuItem saveXScufl = new JMenuItem("Save Workflow");
//   saveXScufl.addActionListener(new ActionListener()
//   {
//     public void actionPerformed(ActionEvent e)
//     {
//       SecurityManager sm = System.getSecurityManager();
//       System.setSecurityManager(null);
//       JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
//       System.setSecurityManager(sm);
//       int returnVal = fc.showSaveDialog(fc);

//       if (returnVal == JFileChooser.APPROVE_OPTION)
//       {
//         File file = fc.getSelectedFile();
//         try
//         {
//           FileWriter out = new FileWriter(file);
//           out.write(retsinaPane.getScuflGraph().getXScufl());
//           out.close();
//         }
//         catch(IOException ioe)
//         {}
//       }
//     }
 //  });
//   fileMenu.add(saveXScufl);

//   fileMenu.add(new JSeparator());
//   JMenuItem fileRunScufl = new JMenuItem("Run workflow");
//   fileRunScufl.addActionListener(new ActionListener()
//   {
//     public void actionPerformed(ActionEvent e)
//     {
//       submitWorkFlow();
//     }
//   });
//   fileMenu.add(fileRunScufl);
//   fileRunScufl.setEnabled(false);

//   fileMenu.add(new JSeparator());
//   JMenuItem fileMenuExit = new JMenuItem("Exit");
//   fileMenuExit.addActionListener(new ActionListener()
//   {
 //    public void actionPerformed(ActionEvent e)
 //    {
 //      System.exit(0);
//     }
 //  });
 //  fileMenu.add(fileMenuExit);
//   menuBar.add(fileMenu);

//view menu
//   JMenu viewMenu = new JMenu("View");
//   viewMenu.setMnemonic(KeyEvent.VK_V);
//   JMenuItem viewXScufl = new JMenuItem("Display XScufl");
//   viewXScufl.addActionListener(new ActionListener()
//   {
//     public void actionPerformed(ActionEvent e)
//     {
//       JFrame f = new JFrame("XScufl");
//       JTextPane xscufl = new JTextPane();
//       JPanel pscroll = new JPanel(new BorderLayout());
//       JScrollPane rscroll = new JScrollPane(pscroll);
//       rscroll.getViewport().setBackground(Color.white);
//       xscufl.setText(retsinaPane.getScuflGraph().getXScufl());
//       xscufl.setEditable(false);
 //      pscroll.add(xscufl);
//       JOptionPane jop = new JOptionPane();
//       rscroll.setPreferredSize(new Dimension(400,180));

//       xscufl.setCaretPosition(0);
//       f.getContentPane().add(rscroll);
//       f.pack();
//       f.setVisible(true);
//       jop.showMessageDialog(null,rscroll,"XScufl",
//                           JOptionPane.PLAIN_MESSAGE);
//     }
 //  });
//   viewMenu.add(viewXScufl);
//   menuBar.add(viewMenu);

//help menu
//   JMenu helpMenu = new JMenu("Help");
//   helpMenu.setMnemonic(KeyEvent.VK_H);
//   JMenuItem aboutMenu = new JMenuItem("About");
//   aboutMenu.addActionListener(new ActionListener()
 //  {
 //    public void actionPerformed(ActionEvent e)
//     {
//       URL inURL = ((Object)this).getClass().getClassLoader().getResource("resources/retsina.html");
//       JTextPane about = new JTextPane();
//       JPanel pscroll = new JPanel(new BorderLayout());
//       JScrollPane rscroll = new JScrollPane(pscroll);
//       rscroll.getViewport().setBackground(Color.white);
 
//       try
//       {
//         about.setPage(inURL);
//       }
//       catch(IOException ioe){}
//       about.setEditable(false);
//       pscroll.add(about);
//       JOptionPane jop = new JOptionPane();
//       rscroll.setPreferredSize(new Dimension(400,180));

//       jop.showMessageDialog(null,rscroll,"About ",
//                           JOptionPane.PLAIN_MESSAGE);
//     }
//   });
//   helpMenu.add(aboutMenu);
//   menuBar.add(helpMenu);

//   return menuBar;
// }

// private static void submitWorkFlow()
// {
//   ScuflGraph graph = retsinaPane.getScuflGraph();

//   try
//   {
//     TavernaFlowReceipt receipt = null;
//     try
//     {
//       BufferedInputStream workflowspec = new BufferedInputStream(new FileInputStream("./scufl_test.xml"));
//       StringWriter sWriter = new StringWriter();
//       while(workflowspec.available()>0) 
//         sWriter.write(workflowspec.read());
//       String scuflDefn = sWriter.toString();

//       String scuflDefn = graph.getXScufl();
//       String input = graph.getDataSet().getDataSetString();
//       System.out.println(input);
//       TavernaStringifiedWorkflowSubmission submit = 
 //                 new TavernaStringifiedWorkflowSubmission(
 //                            scuflDefn,input,
//                             "TestTavernaFlowBroker",
//                             "http://www.it-innovation.soton.ac.uk/users");

//       FlowBroker broker = FlowBrokerFactory.createFlowBroker(
//                             "uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");
//       receipt = (TavernaFlowReceipt) broker.submitFlow(submit);
//     }
 //    catch(InvalidFlowBrokerRequestException invalid){}
//     catch(WorkflowCommandException wfException){} 

//     //poll for status every 500ms
 //    boolean stop = false;
//     String status ="UNKNOWN";
//     while(!stop) 
//     {
//       //retrieve the status
//       status = receipt.getStatusString();
//       if(status.equals("COMPLETE") || status.equals("FAILED"))
//         stop = true;
//       try 
//       {
//         Thread.sleep(500);
//       }
//       catch(InterruptedException ex)
//       {

 //      }
 //    }
 //  
///    if(status.equals("FAILED"))
 //          System.out.println("Error message: " + receipt.getErrorMessage());
 //    System.out.println("Emboss Workflow has finished with status: " + status);
 //    System.out.println("Output:\n\n" + receipt.getOutputString());

//   }
//   catch(Exception ex) 
//   {
//     ex.printStackTrace();
//   }
//
// }


}

