package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.emboss.jemboss.JembossJarUtil;

import uk.ac.mrc.hgmp.taverna.retsina.ProgramSelectionPanel;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraphPanel;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
*
* GUI front editor for a ScuflModel
*
*/
public class Retsina extends JApplet 
{
    
  public static void main(String[] args) 
  {
    Retsina retsinaPane = new Retsina();
    retsinaPane.init();

    JFrame frame = new JFrame("Retsina");
    frame.getContentPane().add(retsinaPane);
    frame.setSize(520, 450);
    frame.show();
  }
    
  public Retsina() 
  {
    super();
  }
  
  public void init() 
  {
    String wossname = null;
    ScuflGraphPanel graphPanel = new ScuflGraphPanel(null);

    setJMenuBar(createMenuBar());
    try
    {
      JembossJarUtil jwoss = new JembossJarUtil("resources/wossname.jar");
      wossname = new String((byte[])jwoss.getElement("wossname.out"));
    }
    catch(Exception ex)
    {
      System.out.println("Failed to read wossname for menu construction");
    }
    
    JPanel westPanel = new ProgramSelectionPanel(wossname,graphPanel);
    // Put the components in the content pane
    Container contentpane = getContentPane();
    contentpane.add(graphPanel,BorderLayout.CENTER);
    contentpane.add(westPanel, BorderLayout.WEST);
  } 

  /**
   * Create the menubar
   */
  public JMenuBar createMenuBar()
  {
     JMenuBar menuBar = new JMenuBar();
     JMenu fileMenu = new JMenu("File");
     fileMenu.setMnemonic(KeyEvent.VK_F);
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
     return menuBar;
   }

}

