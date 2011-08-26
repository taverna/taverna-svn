package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.emboss.jemboss.gui.startup.ProgList;

import uk.ac.mrc.hgmp.taverna.retsina.ProgramJList;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraphPanel;
import java.lang.String;
import java.lang.System;



public class ProgramSelectionPanel extends JPanel 
{
 
  private ScuflGraphPanel graphPanel;
  private ProgList progs;

  public ProgramSelectionPanel(String wossname,
              ScuflGraphPanel graphPanel, ProgList progs,
              JMenuBar progMenuBar)
  {
    super(new BorderLayout());

    this.graphPanel = graphPanel;
    this.progs = progs;

    int npG = progs.getNumPrimaryGroups();
    final int numProgs = progs.getNumProgs();
    final String allAcd[] = progs.getProgsList();
    progMenuBar.setLayout(new  GridLayout(npG,1));

// alphabetical listing 
    final ProgramJList progList = new ProgramJList(allAcd);

//  final JList progList = new JList(allAcd);
    JScrollPane scrollPane = new JScrollPane(progList);

    Box alphaPane = new Box(BoxLayout.Y_AXIS);
    Box alphaTextPane = new Box(BoxLayout.X_AXIS);
    alphaPane.add(Box.createRigidArea(new Dimension(0,10)));
    alphaTextPane.add(new JLabel("GoTo:"));
    alphaTextPane.add(Box.createRigidArea(new Dimension(5,0)));

    final JTextField alphaTextPaneEntry = new JTextField(12);
    alphaTextPaneEntry.setMaximumSize(new Dimension(100,20));
    //scroll program list on typing
    alphaTextPaneEntry.getDocument().addDocumentListener(new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e)
      {
        updateScroll();
      }
      public void removeUpdate(DocumentEvent e)
      {
        updateScroll();
      }
      public void changedUpdate(DocumentEvent e) {}
      public void updateScroll()
      {
        for(int k=0;k<numProgs;k++)
          if(allAcd[k].startsWith(alphaTextPaneEntry.getText()))
          {
            progList.ensureIndexIsVisible(k);
            progList.setSelectionBackground(Color.cyan);
            progList.setSelectedIndex(k);
            break;
          }
      }
    });

    //load program form on carriage return
    alphaTextPaneEntry.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        int index = progList.getSelectedIndex();
        
        System.out.println(allAcd[index]);
      }
    });

    alphaTextPane.add(alphaTextPaneEntry);
    alphaPane.add(alphaTextPane);
    alphaPane.add(scrollPane);

    add(progMenuBar,BorderLayout.NORTH);
    add(alphaPane,BorderLayout.CENTER);

    Dimension d = getMinimumSize();
    d = new Dimension((int)d.getWidth()-10,(int)d.getHeight());
    setPreferredSize(d);
    createProgramMenuListener(allAcd,numProgs);
    createProgramListListener(progList,allAcd);
    createTextEntryListener(progList,allAcd,alphaTextPaneEntry);
  }

  private void createTextEntryListener(final JList progList,
          final String allAcd[],final JTextField alphaTextPaneEntry)
  {
    alphaTextPaneEntry.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        int index  = progList.getSelectedIndex();
        String app = allAcd[index];
        String group = progs.getProgramGroup(app).toLowerCase().replace(':','_').replace(' ','_');
//      System.out.println(allAcd[index]+" GROUP:: "+group);
        graphPanel.insertCell(new Point(0,0),group,app);
      }
    });
  }

  private void createProgramListListener(final JList progList,
                                         final String allAcd[])
  {
// create listener to build Jemboss program forms
    MouseListener mouseListener = new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        JList source = (JList)e.getSource();
        source.setSelectionBackground(Color.cyan);
        int index = source.getSelectedIndex();

        String app = allAcd[index];
        String group = progs.getProgramGroup(app).toLowerCase().replace(':','_').replace(' ','_');
        graphPanel.insertCell(new Point(0,0),group,app);
      }
    };
    progList.addMouseListener(mouseListener);
  }
 
  private void createProgramMenuListener(final String allAcd[],
                                         final int numProgs)
  {
    JMenuItem mi[] = new JMenuItem[numProgs];
    mi = progs.getMenuItems();
    int nm = progs.getNumberMenuItems();
// create action listeners into menu to build Jemboss program forms
    for(int i=0; i<nm;i++)
    {
      mi[i].addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          JMenuItem source = (JMenuItem)(e.getSource());
          String p = source.getText();
          int ind = p.indexOf(" ");
          p = p.substring(0,ind).trim();

          for(int k=0;k<numProgs;k++)
          {
            if(p.equalsIgnoreCase(allAcd[k]))
            {
              String app = allAcd[k];
              String group = 
                 progs.getProgramGroup(app).toLowerCase().replace(':','_').replace(' ','_');

               
              graphPanel.insertCell(new Point(0,0),group,app);
              System.out.println();
              break;
            }
          }
        }
      });
    }
  }

}

