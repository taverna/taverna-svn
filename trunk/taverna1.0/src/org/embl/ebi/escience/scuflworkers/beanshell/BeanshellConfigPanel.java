/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.beanshell;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Point;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;

import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import java.lang.Object;
import java.lang.String;



/**
 * A JPanel that can configure the beanshell processor type
 * @author Tom Oinn, Chris Greenhalgh
 */
public class BeanshellConfigPanel extends JPanel implements ScuflUIComponent {
    
    private BeanshellProcessor processor = null;
    
    /**
     * Create a new beanshell configuration panel
     * applying to the processor specified in the 
     * constructor
     */
    public BeanshellConfigPanel(BeanshellProcessor bp) {
	super(new BorderLayout());
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	this.processor = bp;
	JTabbedPane tabbedPane = new JTabbedPane();
	add(tabbedPane);
	
	// Panel to edit the script
	JPanel scriptEditPanel = new JPanel(new BorderLayout());
	scriptEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								 "Beanshell Script"));
	final JTextArea scriptText = new JTextArea();
	JScrollPane scriptPane = new JScrollPane(scriptText);
	scriptText.setText(processor.getScript());
	scriptEditPanel.add(scriptPane, BorderLayout.CENTER);
	JButton scriptUpdateButton = new JButton("Update");
	scriptUpdateButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    processor.setScript(scriptText.getText());
		}
	    });
	scriptEditPanel.add(scriptUpdateButton, BorderLayout.SOUTH);
	tabbedPane.addTab("Script", scriptEditPanel);

	// Panel to edit the input and output ports
	JPanel portEditPanel = new JPanel(new GridLayout(0,2));
	
	// A list model for the inputs
	final DefaultListModel inputModel = new DefaultListModel();
	updateInputListModel(inputModel);
	// ...and for the outputs
	final DefaultListModel outputModel = new DefaultListModel();
	updateOutputListModel(outputModel);
	
	// Input edit panel
	JPanel inputEditPanel = new JPanel(new BorderLayout());
	inputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								  "Inputs"));
	JList inputList = new JList(inputModel);
	inputList.setPrototypeCellValue("aaaaaaaaaaaaaaaaaaa");

	inputList.setCellRenderer(new DefaultListCellRenderer() {
		public Component getListCellRendererComponent(JList list,
							      Object value,
							      int index,
							      boolean isSelected, 
							      boolean hasFocus) {
		    super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
		    setIcon(ScuflIcons.inputPortIcon);
		    return this;
		}
	    });
	inputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   	new PortListMouseListener(inputList, inputModel, true);
	JScrollPane inputPane = new JScrollPane(inputList);
	inputEditPanel.add(inputPane, BorderLayout.CENTER);
	// Add a text button to create a new input
	final JTextField addInputField = new JTextField();
	addInputField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    // Add a port to the input model!
		    try {
			InputPort ip = new InputPort(processor, addInputField.getText());
			ip.setSyntacticType("'text/plain'");
			processor.addPort(ip);
			BeanshellConfigPanel.this.updateInputListModel(inputModel);
		    }
		    catch (PortCreationException pce) {
			//
		    }
		    catch (DuplicatePortNameException dpne) {
			//
		    }
		}
	    });
	inputEditPanel.add(addInputField, BorderLayout.SOUTH);
	portEditPanel.add(inputEditPanel);
	
	// Output edit panel
	JPanel outputEditPanel = new JPanel(new BorderLayout());
	outputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								  "Outputs"));
	JList outputList = new JList(outputModel);
	outputList.setPrototypeCellValue("aaaaaaaaaaaaaaaaaaa");
	outputList.setCellRenderer(new DefaultListCellRenderer() {
		public Component getListCellRendererComponent(JList list,
							      Object value,
							      int index,
							      boolean isSelected, 
							      boolean hasFocus) {
		    super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
		    setIcon(ScuflIcons.outputPortIcon);
		    return this;
		}
	    });
	outputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	new PortListMouseListener(outputList, outputModel, false);
	JScrollPane outputPane = new JScrollPane(outputList);
	outputEditPanel.add(outputPane, BorderLayout.CENTER);
	// Add a text button to create a new input
	final JTextField addOutputField = new JTextField();
	addOutputField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    // Add a port to the input model!
		    try {
			OutputPort op = new OutputPort(processor, addOutputField.getText());
			op.setSyntacticType("'text/plain'");
			processor.addPort(op);
			BeanshellConfigPanel.this.updateOutputListModel(outputModel);
		    }
		    catch (PortCreationException pce) {
			//
		    }
		    catch (DuplicatePortNameException dpne) {
			//
		    }
		    BeanshellConfigPanel.this.updateInputListModel(inputModel);
		}
	    });
	outputEditPanel.add(addOutputField, BorderLayout.SOUTH);
	portEditPanel.add(outputEditPanel);
	
	tabbedPane.addTab("Ports", portEditPanel);
	setVisible(true);
    }
   
	protected class PortListMouseListener extends MouseAdapter 
	{
		PortListMouseListener(JList list, DefaultListModel listModel, boolean inputFlag) 
		{
			this.list = list;
			this.listModel = listModel;
			this.inputFlag = inputFlag;
			list.addMouseListener(this);
		}
		protected JList list;
		protected DefaultListModel listModel;
		protected boolean inputFlag;

		public void mousePressed(MouseEvent me) 
		{
			popup(me);
		}
		public void mouseReleased(MouseEvent me) 
		{
			popup(me);
		}
		protected void popup(MouseEvent me) 
		{
			if (me.isPopupTrigger()) 
			{
				int index = list.locationToIndex(new Point(me.getX(), me.getY()));
				if (index<0 || index>=list.getModel().getSize())
					return;
				list.setSelectedIndex(index);
				final Port p = (Port)list.getModel().getElementAt(index);
				if (p!=null) 
				{
					JPopupMenu menu = new JPopupMenu();
					menu.add(new JMenuItem(new AbstractAction("Remove port") 
					{
						public void actionPerformed(ActionEvent ae) 
						{
							processor.removePort(p);
							if (inputFlag)
								BeanshellConfigPanel.this.updateInputListModel(listModel);
							else
								BeanshellConfigPanel.this.updateOutputListModel(listModel);
						}
					}));
					menu.add(new JMenuItem(new AbstractAction("Edit syntactic type") 
					{
						public void actionPerformed(ActionEvent ae) 
						{
							//System.out.println("Edit syntactic type of "+p.getName()+" ("+p.getSyntacticType()+")");
							final JTextField field = new JTextField(40);
							if (p.getSyntacticType()!=null)
								field.setText(p.getSyntacticType());
							final JDialog dialog = new JDialog();
							dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
							dialog.setTitle("Edit syntactic type of "+p.getName());
							dialog.getContentPane().setLayout(new BorderLayout());
							dialog.getContentPane().add(field);
							field.addActionListener(new ActionListener() 
							{
								public void actionPerformed(ActionEvent ae) 
								{
									p.setSyntacticType(field.getText());
									dialog.dispose();
								}
							});
							dialog.pack();
							dialog.setVisible(true);
						}
					}));
					menu.show(list, me.getX(), me.getY());
				}
			}
		}
	}
 
    private void updateInputListModel(DefaultListModel model) {
	synchronized(model) {
	    model.clear();
	    InputPort[] ports = processor.getInputPorts();
	    for (int i = 0; i < ports.length; i++) {
		model.addElement(ports[i]);
	    }
	}
    }
    
    private void updateOutputListModel(DefaultListModel model) {
	synchronized(model) {
	    model.clear();
	    OutputPort[] ports = processor.getOutputPorts();
	    for (int i = 0; i < ports.length; i++) {
		model.addElement(ports[i]);
	    }
	}
    }

    public void attachToModel(ScuflModel theModel) {
	//
    }
    
    public void detachFromModel() {
	//
    }
    
    public String getName() {
	return "Configuring beanshell for "+processor.getName();
    }

    public javax.swing.ImageIcon getIcon() {
	return null;
    }
}
