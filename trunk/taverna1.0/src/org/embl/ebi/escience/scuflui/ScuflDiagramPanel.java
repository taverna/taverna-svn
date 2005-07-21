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
import java.net.*;

/**
 * Wraps a ScuflDiagram up in a JScrollPane and
 * provides a toolbar to alter port display, type
 * display and allow save to disk of the current
 * diagram view in dot, svg or png formats.
 * @author Tom Oinn
 */
public class ScuflDiagramPanel extends JPanel 
    implements ScuflUIComponent {
    
    String[] displayPolicyStrings = { "All ports", "Bound ports", "No ports", "Blobs" };
    String[] saveTypes = { "dot", "png", "svg", "ps", "ps2" };
    String[] saveExtensions = { "dot", "png", "svg", "ps", "ps" };
    String[] saveTypeNames = { "dot text", "PNG bitmap", "scalable vector graphics", "postscript", "postscript for PDF"};
    String[] alignment = { "Top to bottom", "Left to right" };

    JComboBox displayPolicyChooser = new JComboBox(displayPolicyStrings);
    JComboBox alignmentChooser = new JComboBox(alignment);
    ScuflDiagram diagram = new ScuflDiagram();
    JCheckBox typeDisplay = new JCheckBox("Show types",false);
    JCheckBox showBoring = new JCheckBox("Boring?",true);
    JCheckBox fitToWindow = new JCheckBox("Fit to window",true);

    final JFileChooser fc;

    public javax.swing.ImageIcon getIcon() {
	return ScuflIcons.windowDiagram;
    }

    JPopupMenu createMenu() {
	JPopupMenu menu = new JPopupMenu();
	menu.add(new ShadedLabel("Port detail", ShadedLabel.TAVERNA_BLUE));
	menu.addSeparator();
	ButtonGroup portButtonGroup = new ButtonGroup();
	for (int i = 0; i < displayPolicyStrings.length; i++) {
	    JRadioButtonMenuItem item = 
		new JRadioButtonMenuItem(displayPolicyStrings[i]);
	    item.setSelected(diagram.getDotView().getPortDisplay()==i);
	    menu.add(item);
	    portButtonGroup.add(item);
	    final int j = i;
	    item.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			diagram.getDotView().setPortDisplay(j);
			updateDiagram();
		    }
		});
	}	
	menu.addSeparator();
	menu.add(new ShadedLabel("Alignment", ShadedLabel.TAVERNA_GREEN));
	menu.addSeparator();
	ButtonGroup alignButtonGroup = new ButtonGroup();
	for (int i = 0; i < alignment.length; i++) {
	    JRadioButtonMenuItem item = 
		new JRadioButtonMenuItem(alignment[i]);
	    item.setSelected((diagram.getDotView().getAlignment()?1:0)==i);
	    menu.add(item);
	    alignButtonGroup.add(item);
	    final boolean b = (i == 1);
	    item.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			diagram.getDotView().setAlignment(b);
			updateDiagram();
		    }
		});
	}	
	menu.addSeparator();
	menu.add(new ShadedLabel("Features", ShadedLabel.TAVERNA_ORANGE));
	menu.addSeparator();
	JCheckBoxMenuItem types = new JCheckBoxMenuItem("Show types");
	types.setSelected(diagram.getDotView().getTypeLabelDisplay());
	types.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    diagram.getDotView().setTypeLabelDisplay(e.getStateChange() == ItemEvent.SELECTED);
		    updateDiagram();
		}
	    });
	menu.add(types);
	JCheckBoxMenuItem boring = new JCheckBoxMenuItem("Show boring entities");
	boring.setSelected(diagram.getDotView().getShowBoring());
	boring.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    diagram.getDotView().setBoring(e.getStateChange() == ItemEvent.SELECTED);
		    updateDiagram();
		}
	    });
	menu.add(boring);
	JCheckBoxMenuItem inline = new JCheckBoxMenuItem("Expand nested workflows");
	inline.setSelected(diagram.getDotView().getExpandWorkflow());
	inline.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    diagram.getDotView().setExpandWorkflow(e.getStateChange() == ItemEvent.SELECTED);
		    updateDiagram();
		}
	    });
	menu.add(inline);
	JCheckBoxMenuItem scale = new JCheckBoxMenuItem("Fit to window");
	scale.setSelected(diagram.getFitToWindow());
	scale.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    diagram.setFitToWindow(e.getStateChange() == ItemEvent.SELECTED);
		}
	    });
	menu.add(scale);
	return menu;
    }
    public void updateDiagram() {
	diagram.updateGraphic();
	doLayout();
	repaint();
    }

    public ScuflDiagramPanel() {
	super();
	setLayout(new BorderLayout());
	
	// Create the diagram
	JScrollPane diagramPane = new JScrollPane(diagram);
	diagramPane.setPreferredSize(new Dimension(0,0));
	diagramPane.getViewport().setBackground(java.awt.Color.WHITE);
	add(diagramPane, BorderLayout.CENTER);
	diagram.setFitToWindow(true);
	
	JToolBar toolbar = new JToolBar();
	toolbar.add(new JLabel("Save as "));
	// Create the save buttons
	for (int i = 0; i < saveTypes.length; i++) {
	    String type = saveTypes[i];
	    String extension = saveExtensions[i];
	    ImageIcon icon = new ImageIcon(ScuflDiagramPanel.class.getResource("icons/graphicalview/saveAs"+type.toUpperCase()+".png"));
	    JButton saveButton = new JButton(icon);
	    saveButton.setPreferredSize(new Dimension(25,25));   
	    saveButton.addActionListener(new DotInvoker(type, extension));
	    saveButton.setToolTipText("Save as "+saveTypeNames[i]);
	    toolbar.add(saveButton);
	}
	
	toolbar.addSeparator();

	final JButton configure = new JButton("Configure diagram", ScuflIcons.editIcon);
	configure.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    JPopupMenu menu = createMenu();
		    menu.show(configure, 0, configure.getHeight());
		}
	    });
	toolbar.add(configure);
	toolbar.add(Box.createHorizontalGlue());
	
	toolbar.setFloatable(false);
	toolbar.setRollover(true);
	toolbar.setMaximumSize(new Dimension(2000,30));
	fc = new JFileChooser();
	add(toolbar, BorderLayout.PAGE_START);
    }
    
    class DotInvoker implements ActionListener {
	String type = "dot";
	String extension = "dot";
	public DotInvoker() {
	    //
	}
	public DotInvoker(String type, String extension) {
	    this.type = type;
	    this.extension = extension;
	}
	public void actionPerformed(ActionEvent e) {
	    try {
		Preferences prefs = Preferences.userNodeForPackage(ScuflDiagramPanel.class);
		String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		fc.setCurrentDirectory(new File(curDir));
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[]{extension}));
		int returnVal = fc.showSaveDialog(ScuflDiagramPanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    prefs.put("currentDir", fc.getCurrentDirectory().toString());
		    File file = fc.getSelectedFile();
		    // Rewrite the file name if it doesn't end with the specified extension
		    if (file.getName().endsWith("."+extension) == false) {
			file = new File(file.toURI().resolve(file.getName()+"."+extension));
		    }
		    if (type.equals("dot")) {
			// Just write out the dot text, no processing required
			PrintWriter out = new PrintWriter(new FileWriter(file));
			out.println(diagram.getDot());
			out.flush();
			out.close();
		    }
		    else {
			FileOutputStream fos = new FileOutputStream(file);
			// Invoke DOT to get the SVG document as a byte stream
			String dotLocation = System.getProperty("taverna.dotlocation");
			if (dotLocation == null) {
			    dotLocation = "dot";
			}
			Process dotProcess = Runtime.getRuntime().exec(new String[]{dotLocation,"-T"+type});
			OutputStream dotOut = dotProcess.getOutputStream();
			dotOut.write(diagram.getDot().getBytes());
			dotOut.flush();
			dotOut.close();
			new StreamCopier(dotProcess.getInputStream(), fos).start();
		    }	
		}
	    }
	    catch (Exception ex) {
		ex.printStackTrace();
		JOptionPane.showMessageDialog(null,
					      "Problem saving diagram : \n"+ex.getMessage(),
					      "Error!",
					      JOptionPane.ERROR_MESSAGE);
	    }
	}
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
	diagram.getDotView().setPortDisplay(2);
	diagram.getDotView().setTypeLabelDisplay(false);
    }

    public void detachFromModel() {
	diagram.detachFromModel();
    }

    public String getName() {
	return "Workflow diagram";
    }

}
