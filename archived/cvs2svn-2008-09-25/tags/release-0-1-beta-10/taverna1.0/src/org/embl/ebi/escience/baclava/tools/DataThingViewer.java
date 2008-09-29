/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.tools;

import org.jdom.*;
import org.jdom.input.*;
import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.baclava.factory.*;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * A simple data thing viewer tool that can be launched
 * from the command line or LSID launchpad to view
 * the XML form of data thing object. The main method
 * takes a single filename as its command lines argument
 * and creates a JFrame with the data renderer in.
 * @author Tom Oinn
 */
public class DataThingViewer extends JFrame {
    
    public static void main(String[] args) throws Exception {
	try {
	    String dataThingFileName = args[0];
	    System.out.println("Opening file : "+dataThingFileName);
	    File f = new File(dataThingFileName);
	    InputStream is = new FileInputStream(f);
	    SAXBuilder builder = new SAXBuilder();
	    Document doc = builder.build(is);
	    Element e = doc.getRootElement();
	    DataThing theThing = new DataThing(e);
	    new DataThingViewer(theThing);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    Thread.sleep(10000);
	}
    }

    public DataThingViewer(DataThing theThing) {
	super("DataThing Viewer");
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} 
	catch (Exception e) {
	    //
	}
	setBounds(100,100,500,500);
	//Quit this app when the big window closes.
        addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
	ResultItemPanel panel = new ResultItemPanel(theThing);
	getContentPane().add(panel);
	setJMenuBar(createMenuBar(theThing));
	pack();
	show();
    }
    
    private JMenuBar createMenuBar(DataThing theThing) {
	JMenuBar menuBar = new JMenuBar();
	// File menu to allow saving of the data thing to disk
	JMenu fileMenu = new JMenu("File");
	JMenuItem save = new JMenuItem("Save contents to disk", Workbench.saveIcon);
	final DataThing dataThing = theThing;
	save.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JFileChooser chooser = new JFileChooser();
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    int returnVal = chooser.showSaveDialog(DataThingViewer.this);
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			String name = "datathing";
			try {
			    dataThing.writeToFileSystem(f, name);
			}
			catch (IOException ioe) {
			    //
			}
		    }
		}
	    });
	fileMenu.add(save);
	
	JMenuItem exit = new JMenuItem("Exit");
	exit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    System.exit(0);
		}
	    });
	fileMenu.add(exit);
	
	menuBar.add(fileMenu);
	return menuBar;

    }
    
    
}
