/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scuflui.workbench.GenericUIComponentFrame;
import org.embl.ebi.escience.scuflui.workbench.Workbench;

// Utility Imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// IO Imports
import java.io.File;
import java.io.InputStream;

// JDOM Imports
import org.jdom.Document;
import org.jdom.output.XMLOutputter;

// Network Imports
import java.net.URL;




/**
 * A JPanel subclass that contains the appropriate input fields
 * and controls to launch the enactment engine from the given input
 * @author Tom Oinn
 */
public class EnactorLaunchPanel extends JPanel 
    implements ScuflModelEventListener, ScuflUIComponent {

    ScuflModel model = null;
    private JPanel inputPanel = null;
    
    /**
     * An array list of the ports that this particular view thinks
     * the model contains. This is used when receiving model events
     * to determine whether the panel should be regenerated. Keys in the
     * map are the string names of the workflow input ports. Values are
     * instances of the WorkflowInputPane class.
     */
    private Map currentInputs = new HashMap();

    /**
     * Create a new launch panel with default layout as
     * BoxLayout.
     */
    public EnactorLaunchPanel() {
	super(new BorderLayout());
	inputPanel = new JPanel();
	inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.PAGE_AXIS));
	add(inputPanel, BorderLayout.NORTH);
	final JTextArea xmlText = new JTextArea();
	JScrollPane xmlPane = new JScrollPane(xmlText);
	xmlPane.setPreferredSize(new Dimension(100,100));
	JPanel xmlPanel = new JPanel(new BorderLayout());
	xmlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
							    "Preview input.xml and run"));
	xmlPanel.add(xmlPane, BorderLayout.CENTER);
	JPanel actionPanel = new JPanel();
	actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.PAGE_AXIS));
	xmlPanel.add(actionPanel, BorderLayout.EAST);
	JButton xmlRefresh = new JButton(ScuflIcons.refreshIcon);
	xmlRefresh.setPreferredSize(new Dimension(32,32));
	actionPanel.add(xmlRefresh);
	xmlRefresh.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    XMLOutputter xo = new XMLOutputter();
		    xo.setIndent(" ");
		    xo.setNewlines(true);
		    xmlText.setText(xo.outputString(EnactorLaunchPanel.this.getInputDocument()));
		}
	    });
	add(xmlPanel, BorderLayout.CENTER);
	JButton runButton = new JButton(ScuflIcons.runIcon);
	actionPanel.add(runButton);
	runButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    
		    Map inputObject = EnactorLaunchPanel.this.getDataThingMap();
		    System.out.println("Created the Input object.."+inputObject.toString());
		    try {
			if (Workbench.workbench != null) {
			    GenericUIComponentFrame thing = new GenericUIComponentFrame(Workbench.workbench.model,
											new EnactorInvocation(null,
													      EnactorLaunchPanel.this.model,
													      inputObject,
													      null));
			    thing.setSize(600,400);
			    thing.setLocation(100,100);
			    Workbench.workbench.desktop.add(thing);
			    thing.moveToFront();
			}
		    }
		    catch (Exception e) {
			e.printStackTrace();
		    }
		    
		}
	    });
	
	runButton.setPreferredSize(new Dimension(32,32));
	show();
    }

    public String getName() {
	return "Enactor launch";
    }

    /**
     * Return true if the model's input ports have changed since the
     * last time we checked.
     */
    public boolean modelHasChanged() {
	if (this.model == null) {
	    return false;
	}
	else {
	    // Get all the workflow input ports
	    Port[] inputs = this.model.getWorkflowSourcePorts();
	    int numberOfInputs = inputs.length;
	    // If there are a different number of inputs then there was certainly
	    // a change so return true.
	    if (numberOfInputs != this.currentInputs.size()) {
		return true;
	    }
	    // Check that all the Port objects in the array of workflow sources
	    // are present in the map. If so, as there are the same number of 
	    // objects in each then presumably the array and map are consistant
	    // and we should return false. If not, there must have been a change
	    // and we return true.
	    for (int i = 0; i < inputs.length; i++) {
		String portName = inputs[i].getName();
		if (!currentInputs.containsKey(portName)) {
		    return true;
		}
	    }
	    return false;
	}
    }

    /**
     * Regenerate the map of WorkflowInputPanel objects
     */
    public synchronized void updatePanel() {
	// If the model hasn't changed from the point of view of the overall inputs
	// then exit immediately. As the modelHasChanged method always returns false
	// if the model is null then we can assume there is a model present for the
	// remainder of this method.
	if (!modelHasChanged()) {
	    return;
	}
	// Get the array of workflow inputs and create a new map with keys
	// being port names and values being ports
	Port[] inputs = model.getWorkflowSourcePorts();
	Map inputMap = new HashMap();
	for (int i = 0; i < inputs.length; i++) {
	    inputMap.put(inputs[i].getName(), inputs[i]);
	}
	// For each entry in the current map, check it exists in the new
	// one and remove it otherwise
	for (Iterator i = this.currentInputs.keySet().iterator(); i.hasNext();) {
	    String key = (String)i.next();
	    if (!inputMap.containsKey(key)) {
		WorkflowInputPanel wip = (WorkflowInputPanel)(currentInputs.get(key));
		// Remove from the panel
		inputPanel.remove(wip);
		// Remove from the cached map
		this.currentInputs.remove(key);
		
	    }
	}
	// Iterate over the new map, adding entries to the cached one if
	// they don't already exist.
	for (Iterator i = inputMap.keySet().iterator(); i.hasNext();) {
	    String key = (String)i.next();
	    if (!this.currentInputs.containsKey(key)) {
		// create a new WorkflowInputPanel and add it to the cache.
		WorkflowInputPanel wip = new WorkflowInputPanel((Port)(inputMap.get(key)));
		this.currentInputs.put(key, wip);
		// also add it to this JPanel
		inputPanel.add(wip);

	    }
	}


    }


    public void attachToModel(ScuflModel model) {
	if (this.model == null) {
	    this.model = model;
	    model.addListener(this);
	    updatePanel();
	}
    }
    
    public void detachFromModel() {
	if (this.model != null) {
	    model.removeListener(this);
	    this.model = null;
	    //    clearPanel();
	}
    }
    
    /**
     * Return the JDOM Document object that represents this
     * set of workflow inputs.
     */
    public Document getInputDocument() {
	// Create DataThing objects
	Map dataThings = new HashMap(); 
	for (Iterator i = currentInputs.keySet().iterator(); i.hasNext(); ) {
	    String key = (String)i.next();
	    WorkflowInputPanel wip = (WorkflowInputPanel)currentInputs.get(key);
	    dataThings.put(wip.getPort().getName(), DataThingFactory.bake(wip.getText()));
	}
	return DataThingXMLFactory.getDataDocument(dataThings);
    }

    /**
     * Get the Map back to actually do the submission
     */
    public Map getDataThingMap() {
	// Create DataThing objects
	Map dataThings = new HashMap(); 
	for (Iterator i = currentInputs.keySet().iterator(); i.hasNext(); ) {
	    String key = (String)i.next();
	    WorkflowInputPanel wip = (WorkflowInputPanel)currentInputs.get(key);
	    dataThings.put(wip.getPort().getName(), DataThingFactory.bake(wip.getText()));
	}
	return dataThings;
    }

    public void receiveModelEvent(ScuflModelEvent sme) {
	updatePanel();
    }

}
class WorkflowInputPanel extends JPanel {

    Port underlyingPort;
    JTextArea textArea;


    // Each input panel corresponds to a single workflow input port
    // in the workflow, hence the constructor with the Port argument,
    // this port is one of the internal workflow source ports.
    public WorkflowInputPanel(Port p) {

	super();
	setLayout(new BorderLayout());
	Border border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
							 p.getName()+" - "+p.getSyntacticType());
	this.setBorder(border);
	final JTextArea text = new JTextArea("Enter input value here");
	this.textArea = text;
	text.setWrapStyleWord(true);
	JScrollPane pane = new JScrollPane(text);
	pane.setPreferredSize(new Dimension(100,100));

	add(pane, BorderLayout.CENTER);
	JButton loadFromFile = new JButton(ScuflIcons.openIcon);
	loadFromFile.setPreferredSize(new Dimension(32,32));
	JButton loadFromURL  = new JButton(ScuflIcons.webIcon);
	loadFromURL.setPreferredSize(new Dimension(32,32));
	JButton clear = new JButton(ScuflIcons.deleteIcon);
	clear.setPreferredSize(new Dimension(32,32));
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
	buttonPanel.add(clear);
	buttonPanel.add(loadFromFile);
	buttonPanel.add(loadFromURL);
	clear.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    text.setText("");
		}
	    });
	final JFileChooser fc = new JFileChooser();
	loadFromFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			int returnVal = fc.showOpenDialog(WorkflowInputPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    InputStream is = file.toURL().openStream();
			    java.io.DataInputStream dis = new java.io.DataInputStream(new java.io.BufferedInputStream(is));
			    StringBuffer sb = new StringBuffer();
			    String s = null;
			    while ((s = dis.readLine()) != null) { 
				sb.append(s);
				sb.append("\n");
			    }
			    text.setText(sb.toString());
			}
		    }
		    catch (Exception ex) {
			JOptionPane.showMessageDialog(null,
						      "Problem opening content from web : \n"+ex.getMessage(),
						      "Exception!",
						      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	loadFromURL.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			String name = (String)JOptionPane.showInputDialog(null,
									  "URL to open?",
									  "URL Required",
									  JOptionPane.QUESTION_MESSAGE,
									  null,
									  null,
									  "http://");
			if (name != null) {
			    InputStream is = new URL(name).openStream();
			    java.io.DataInputStream dis = new java.io.DataInputStream(new java.io.BufferedInputStream(is));
			    StringBuffer sb = new StringBuffer();
			    String s = null;
			    while ((s = dis.readLine()) != null) { 
				sb.append(s);
				sb.append("\n");
			    }
			    text.setText(sb.toString());
			}
		    }
		    catch (Exception ex) {
			JOptionPane.showMessageDialog(null,
						      "Problem opening content from web : \n"+ex.getMessage(),
						      "Exception!",
						      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	
	add(buttonPanel, BorderLayout.EAST);
	this.underlyingPort = p;
	
    }

    public String getText() {
	return this.textArea.getText();
    }

    public String getName() {
	return this.underlyingPort.getName();
    }
    
    public Port getPort() {
	return this.underlyingPort;
    }

}
    
    
    
