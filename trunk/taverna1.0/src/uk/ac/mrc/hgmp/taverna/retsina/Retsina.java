package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.emboss.jemboss.JembossJarUtil;

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
}

