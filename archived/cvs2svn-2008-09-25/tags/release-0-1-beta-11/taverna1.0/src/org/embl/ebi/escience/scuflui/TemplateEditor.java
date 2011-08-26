/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;
import org.embl.ebi.escience.scufl.*;

/**
 * An editor for the AnnotationTemplate objects
 * associated with a given Processor instance
 * @author Tom Oinn
 */
public class TemplateEditor extends JComponent implements ScuflUIComponent {
    
    Processor theProcessor;
    JPanel existingTemplates;
    JPanel defaultTemplates;

    static final PortComboBoxRenderer renderer = new PortComboBoxRenderer();
    
    public TemplateEditor(Processor theProcessor) {
	super();
	this.theProcessor = theProcessor;
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	setPreferredSize(new Dimension(100,100));
	
	// Create the new template panel
	final JPanel addNewTemplate = new JPanel();

	addNewTemplate.setLayout(new GridLayout(3,3));
	addNewTemplate.setMaximumSize(new Dimension(1000,100));
	addNewTemplate.setPreferredSize(new Dimension(500,100));

	// Get all the port names in this processor
	Port[] allPorts = theProcessor.getPorts();
	final JComboBox subjects = new JComboBox(allPorts);
	subjects.setRenderer(renderer);
	final JComboBox objects = new JComboBox(allPorts);
	objects.setRenderer(renderer);
	final JTextField predicate = new JTextField();
	
	JButton createTemplate = new JButton("Create");
	
	addNewTemplate.add(new JLabel("Subject"));
	addNewTemplate.add(new JLabel("Predicate"));
	addNewTemplate.add(new JLabel("Object"));
	addNewTemplate.add(subjects);
	addNewTemplate.add(predicate);
	addNewTemplate.add(objects);
	addNewTemplate.add(createTemplate);
	//addNewTemplate.add(new JLabel(""));
	//addNewTemplate.add(new JLabel(""));
	createTemplate.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    Port subject = (Port)subjects.getSelectedItem();
		    Port object = (Port)objects.getSelectedItem();
		    String verb = predicate.getText();
		    if (verb.equals("")==false) {
			TemplateEditor.this.theProcessor.addAnnotationTemplate(AnnotationTemplate.standardTemplate(subject,
														   verb,
														   object));
			TemplateEditor.this.populate();
		    }
		}
	    });
	
	addNewTemplate.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								  "Create new template"));
	add(addNewTemplate);
	
	existingTemplates = new JPanel();
	existingTemplates.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								  "Current templates"));
	existingTemplates.setLayout(new BoxLayout(existingTemplates, BoxLayout.PAGE_AXIS));
	
	defaultTemplates = new JPanel();

	defaultTemplates.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								  "Default templates"));
	defaultTemplates.setLayout(new BoxLayout(defaultTemplates, BoxLayout.PAGE_AXIS));
	
	populate();
	add(existingTemplates);
	add(defaultTemplates);
	setVisible(true);
    }
    private JComponent makeSmall(JComponent c) {
	c.setPreferredSize(new Dimension(70,20));
	c.setMaximumSize(c.getPreferredSize());
	return c;
    }
    
    void populate() {
	existingTemplates.removeAll();
	defaultTemplates.removeAll();
	// Put JLable instances into the existingTemplates panel
	AnnotationTemplate[] templates = theProcessor.getAnnotationTemplates();
	for (int i = 0; i < templates.length; i++) {
	    existingTemplates.add(new JEditorPane("text/html",templates[i].getDescription()));
	}
	if (templates.length == 0) {
	    existingTemplates.add(new JLabel("No existing templates"));
	}
	AnnotationTemplate[] defaults = theProcessor.defaultAnnotationTemplates();
	for (int i = 0; i < defaults.length; i++) {
	    defaultTemplates.add(new JEditorPane("text/html",defaults[i].getDescription()));
	}
	if (defaults.length == 0) {
	    JLabel l = new JLabel("No default templates");
	    l.setPreferredSize(new Dimension(1000,25));
	    defaultTemplates.add(l);
	}
    }

    public void attachToModel(ScuflModel model) {
	//
    }

    public void detachFromModel() {
	//
    }

    public void  receiveModelEvent(ScuflModelEvent event) {
	//
    }

    public String getName() {
	return "Template editor for "+theProcessor.getName();
    }

    public ImageIcon getIcon() {
	return null;
    }
    
}
class PortComboBoxRenderer extends JLabel implements ListCellRenderer {
    public PortComboBoxRenderer() {
	setOpaque(true);
	setHorizontalAlignment(LEFT);
	setVerticalAlignment(CENTER);
    }
    public Component getListCellRendererComponent(JList list,
						  Object value,
						  int index,
						  boolean isSelected,
						  boolean cellHasFocus) {
	if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } 
	else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
	if (value instanceof OutputPort) {
	    setIcon(ScuflIcons.outputPortIcon);
	}
	else {
	    setIcon(ScuflIcons.inputPortIcon);
	}
	setText(((Port)value).getName());
	return this;
    }
}
