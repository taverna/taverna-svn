/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import org.embl.ebi.escience.scufl.*;
import java.awt.*;
import javax.swing.border.*;
import org.jdom.*;
import org.jdom.output.*;

/**
 * A JPanel subclass that contains the appropriate input fields
 * and controls to launch the enactment engine from the given input
 * @author Tom Oinn
 */
public class EnactorLaunchPanel extends JPanel 
    implements ScuflModelEventListener, ScuflUIComponent {

    private ScuflModel model = null;
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
	xmlPanel.setBorder(BorderFactory.createEtchedBorder());
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
	Element rootElement = new Element("dataset");
	Document doc = new Document(rootElement);
	for (Iterator i = this.currentInputs.keySet().iterator(); i.hasNext(); ) {
	    String key = (String)i.next();
	    WorkflowInputPanel wip = (WorkflowInputPanel)(currentInputs.get(key));
	    Element dataElement = new Element("data");
	    rootElement.addContent(dataElement);
	    Element id = new Element("ID");
	    Element name = new Element("name");
	    Element type = new Element("type");
	    Element value = new Element("value");
	    dataElement.addContent(id);
	    dataElement.addContent(name);
	    dataElement.addContent(type);
	    dataElement.addContent(value);
	    id.setText("-1");
	    name.setText(wip.getPort().getName());
	    type.setText(wip.getPort().getSyntacticType());
	    value.addContent(new CDATA(wip.getText()));
	}
	return doc;
    }

    public void receiveModelEvent(ScuflModelEvent sme) {
	updatePanel();
    }

}
class WorkflowInputPanel extends JPanel {

    Port underlyingPort;
    JTextArea textArea;

    public WorkflowInputPanel(Port p) {

	super();
	setLayout(new BorderLayout());
	System.out.println("Creating new workflow input panel "+p.getName()+" - "+p.getSyntacticType());
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
	loadFromFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // TODO - Implement load from file
		}
	    });
	loadFromURL.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // TODO - Implement load from URL
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
    
    
    
