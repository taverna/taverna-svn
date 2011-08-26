/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.embl.ebi.escience.scufl.*;
import java.util.*;
import java.util.prefs.*;
import java.io.*;
import javax.swing.filechooser.*;

/**
 * Wraps a ScuflDiagram up in a JScrollPane and
 * provides a toolbar to alter port display, type
 * display and allow save to disk of the current
 * diagram view in dot, svg or png formats.
 * @author Tom Oinn
 */
public class ScuflDiagramPanel extends JPanel 
    implements ScuflUIComponent {
    
    String[] displayPolicyStrings = { "All ports", "Bound ports", "No ports" };
    String[] alignment = {"Vertical","Horizontal"};
    JButton saveAsDot, saveAsPNG, saveAsSVG;
    JComboBox displayPolicyChooser = new JComboBox(displayPolicyStrings);
    JComboBox alignmentChooser = new JComboBox(alignment);
    ScuflDiagram diagram = new ScuflDiagram();
    JCheckBox typeDisplay = new JCheckBox("Show types",false);
    JCheckBox fitToWindow = new JCheckBox("Fit to window",true);
    static ImageIcon svgIcon,pngIcon,dotIcon;
    final JFileChooser fc;

    static {
	try {
	    Class c = Class.forName("org.embl.ebi.escience.scuflui.ScuflDiagramPanel");
	    svgIcon = new ImageIcon(c.getResource("icons/graphicalview/saveAsSVG.png"));
	    dotIcon = new ImageIcon(c.getResource("icons/graphicalview/saveAsDot.png"));
	    pngIcon = new ImageIcon(c.getResource("icons/graphicalview/saveAsPNG.png"));

	}
	catch (Exception ex) {
	    //
	}
    }
    
    public javax.swing.ImageIcon getIcon() {
	return ScuflIcons.windowDiagram;
    }

    public ScuflDiagramPanel() {
	super();
	setLayout(new BorderLayout());
	
	// Create the diagram
	JScrollPane diagramPane = new JScrollPane(diagram);
	// Prevent the diagram poping up its menu
	diagram.disableMouseListener();
	diagramPane.setPreferredSize(new Dimension(0,0));
	diagramPane.getViewport().setBackground(java.awt.Color.WHITE);
	add(diagramPane, BorderLayout.CENTER);
	// Default to no ports shown
	diagram.setPortDisplay(2);
	// Default to resize to fit
	diagram.setDisplayTypes(false);
	// Default to no types shown
	diagram.setFitToWindow(true);
	
	// Create the save buttons
	saveAsDot = new JButton(dotIcon);
	saveAsDot.setPreferredSize(new Dimension(25,25));
	saveAsSVG = new JButton(svgIcon);
	saveAsSVG.setPreferredSize(new Dimension(25,25));
	saveAsPNG = new JButton(pngIcon);
	saveAsPNG.setPreferredSize(new Dimension(25,25));
	
	// Create the tool bar
	JToolBar toolbar = new JToolBar();
	toolbar.add(new JLabel("Save as "));
	toolbar.add(saveAsDot);
	toolbar.add(saveAsPNG);
	toolbar.add(saveAsSVG);
	toolbar.addSeparator();
	toolbar.add(typeDisplay);
	toolbar.addSeparator();
	toolbar.add(displayPolicyChooser);
	toolbar.add(alignmentChooser);
	toolbar.addSeparator();
	toolbar.add(fitToWindow);
	toolbar.add(Box.createHorizontalGlue());
	
	toolbar.setFloatable(false);
	toolbar.setRollover(true);
	toolbar.setMaximumSize(new Dimension(2000,30));
	
	// Create the action listeners
	displayPolicyChooser.setSelectedIndex(2);
	displayPolicyChooser.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    diagram.setPortDisplay(displayPolicyChooser.getSelectedIndex());
		    // Read the new alignment from the diagram, some port settings
		    // may change this and we need to listen otherwise we end up
		    // with an inconsistant ui
		    alignmentChooser.setSelectedIndex(diagram.getLRAlignment()?1:0);
		    ScuflDiagramPanel.this.doLayout();
		    ScuflDiagramPanel.this.repaint();
		}
	    });
	alignmentChooser.setSelectedIndex(0);
	alignmentChooser.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    diagram.setLRAlignment(alignmentChooser.getSelectedIndex()==0?false:true);
		    ScuflDiagramPanel.this.doLayout();
		    ScuflDiagramPanel.this.repaint();
		}
	    });
	typeDisplay.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    if (e.getStateChange() == ItemEvent.DESELECTED) {
			diagram.setDisplayTypes(false);
		    }
		    else {
			diagram.setDisplayTypes(true);
		    }
		}
	    });
	fitToWindow.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    if (e.getStateChange() == ItemEvent.DESELECTED) {
			diagram.setFitToWindow(false);
		    }
		    else {
			diagram.setFitToWindow(true);
		    }
		}
	    });
	
	// And the ones for the save buttons...
	saveAsDot.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			Preferences prefs = Preferences.userNodeForPackage(ScuflDiagramPanel.class);
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			fc.setCurrentDirectory(new File(curDir));
			fc.resetChoosableFileFilters();
			fc.setFileFilter(new ExtensionFileFilter(new String[]{"dot"}));
			int returnVal = fc.showSaveDialog(ScuflDiagramPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    prefs.put("currentDir", fc.getCurrentDirectory().toString());
			    File file = fc.getSelectedFile();
			    PrintWriter out = new PrintWriter(new FileWriter(file));
			    out.println(diagram.getDot());
			    out.flush();
			    out.close();
			}
		    }
		    catch (Exception ex) {
			//
		    }
		}
	    });
	saveAsPNG.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			Preferences prefs = Preferences.userNodeForPackage(ScuflDiagramPanel.class);
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			fc.setCurrentDirectory(new File(curDir));
			fc.resetChoosableFileFilters();
			fc.setFileFilter(new ExtensionFileFilter(new String[]{"png"}));
			int returnVal = fc.showSaveDialog(ScuflDiagramPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    prefs.put("currentDir", fc.getCurrentDirectory().toString());
			    File file = fc.getSelectedFile();
			    FileOutputStream fos = new FileOutputStream(file);
			    // Invoke DOT to get the SVG document as a byte stream
			    String dotLocation = System.getProperty("taverna.dotlocation");
			    if (dotLocation == null) {
				dotLocation = "dot";
			    }
			    Process dotProcess = Runtime.getRuntime().exec(new String[]{dotLocation,"-Tpng"});
			    OutputStream dotOut = dotProcess.getOutputStream();
			    dotOut.write(diagram.getDot().getBytes());
			    dotOut.flush();
			    dotOut.close();
			    new StreamCopier(dotProcess.getInputStream(), fos).start();
			}
		    }
		    catch (Exception ex) {
			ex.printStackTrace();
		    }
		}
	    });
	saveAsSVG.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			Preferences prefs = Preferences.userNodeForPackage(ScuflDiagramPanel.class);
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			fc.setCurrentDirectory(new File(curDir));
			fc.resetChoosableFileFilters();
			fc.setFileFilter(new ExtensionFileFilter(new String[]{"svg"}));
			int returnVal = fc.showSaveDialog(ScuflDiagramPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    prefs.put("currentDir", fc.getCurrentDirectory().toString());
			    File file = fc.getSelectedFile();
			    FileOutputStream fos = new FileOutputStream(file);
			    // Invoke DOT to get the SVG document as a byte stream
			    String dotLocation = System.getProperty("taverna.dotlocation");
			    if (dotLocation == null) {
				dotLocation = "dot";
			    }
			    Process dotProcess = Runtime.getRuntime().exec(new String[]{dotLocation,"-Tsvg"});
			    OutputStream dotOut = dotProcess.getOutputStream();
			    dotOut.write(diagram.getDot().getBytes());
			    dotOut.flush();
			    dotOut.close();
			    new StreamCopier(dotProcess.getInputStream(), fos).start();
			}
		    }
		    catch (Exception ex) {
			ex.printStackTrace();
		    }
		}
	    });
	fc = new JFileChooser();
	add(toolbar, BorderLayout.PAGE_START);
    }
    
    class StreamCopier extends Thread {
	InputStream is;
	OutputStream os;
	public StreamCopier(InputStream is, OutputStream os) {
	    this.is = is;
	    this.os = os;
	}
	public void run() {
	    try {
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = is.read(buffer)) != -1) {
		    os.write(buffer,0,bytesRead);
		}
		os.flush();
		os.close();
	    }
	    catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    public void attachToModel(ScuflModel model) {
	diagram.attachToModel(model);
	diagram.setPortDisplay(displayPolicyChooser.getSelectedIndex());
	diagram.setLRAlignment(alignmentChooser.getSelectedIndex()==0?false:true);
	diagram.setFitToWindow(true);
	diagram.setDisplayTypes(false);
    }

    public void detachFromModel() {
	diagram.detachFromModel();
    }

    public String getName() {
	return "Workflow diagram";
    }

}
