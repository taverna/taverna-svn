package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Dimension;
import java.awt.Color;
import java.io.IOException;
import javax.swing.*;
import org.emboss.jemboss.JembossJarUtil;

import uk.ac.mrc.hgmp.taverna.retsina.ProgramSelectionPanel;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraphPanel;
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

}

