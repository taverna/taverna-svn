package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerParameterDescription;

@SuppressWarnings("serial")
public class RapidMinerConfigurationPanel
		extends
		ActivityConfigurationPanel<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean> {

	private RapidMinerExampleActivity activity;
	private RapidMinerActivityConfigurationBean configBean;
	
	private JTextField operatorNamefieldString;
	private JRadioButton isExplicitFieldButton;
	
	private JRadioButton implicitButton;
	private JRadioButton explicitButton;
	
	private JLabel operatorNameLabel;
	private JLabel cardDescription;
	
    private JPanel titlePanel, contentPanel, buttonPanel, page1, page2;
	
	String first = new String("Explicit");
	String second = new String("Implicit");

	public RapidMinerConfigurationPanel(RapidMinerExampleActivity activity) {
		this.activity = activity;
		initGui();
	}

	protected void initGui() {
		removeAll();
		setLayout(new FlowLayout());
		setPreferredSize(new Dimension(600,400));
		
		// FIXME: Create GUI depending on activity configuration bean
		
		// title Panel 
		 operatorNameLabel = new JLabel(" Operator");
		 cardDescription = new JLabel(" Please select whether you want to explicitly set the Output Location");
			 
		 titlePanel = new JPanel(new BorderLayout());
         titlePanel.setBackground(Color.WHITE);	
         titlePanel.setPreferredSize(new Dimension(600, 50));
         
         titlePanel.add(operatorNameLabel, BorderLayout.NORTH);
         titlePanel.add(cardDescription, BorderLayout.EAST);
         
         add(titlePanel);
         		
		// Data-Location 
		
	    explicitButton = new JRadioButton(first);
		explicitButton.setActionCommand(first);
		//explicitButton.setSelected(true);
		
		implicitButton = new JRadioButton(second);
		implicitButton.setActionCommand(second);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(explicitButton);
		buttonGroup.add(implicitButton);
		
		RadioButtonListener radioListener = new RadioButtonListener();
		explicitButton.addActionListener(radioListener);
		explicitButton.addChangeListener(radioListener);
		explicitButton.addItemListener(radioListener);
		implicitButton.addActionListener(radioListener);
		implicitButton.addChangeListener(radioListener);
		implicitButton.addItemListener(radioListener);
		
		
		
		// End of Data-Location
		// Populate fields from activity configuration bean
		refreshConfiguration();
	}

	/**
	 * Check that user values in UI are valid
	 */
	@Override
	public boolean checkValues() {
		
		// All valid, return true
		return true;
	}

	/**
	 * Return configuration bean generated from user interface last time
	 * noteConfiguration() was called.
	 */
	@Override
	public RapidMinerActivityConfigurationBean getConfiguration() {
		// Should already have been made by noteConfiguration()
		return configBean;
	}

	/**
	 * Check if the user has changed the configuration from the original
	 */
	@Override
	public boolean isConfigurationChanged() {
		String originalString = configBean.getOperatorName();
		
		boolean originalLocation = configBean.getIsExplicit();
		boolean currentLocation = explicitButton.isSelected();
		// true (changed) unless all fields match the originals
		return ! (originalLocation == currentLocation);
	}

	/**
	 * Prepare a new configuration bean from the UI, to be returned with
	 * getConfiguration()
	 */
	@Override
	public void noteConfiguration() {
		//configBean = new ExampleActivityConfigurationBean();
		
		// FIXME: Update bean fields from your UI elements

		configBean.setIsExplicit(explicitButton.isSelected());
	}

	/**
	 * Update GUI from a changed configuration bean (perhaps by undo/redo).
	 * 
	 */
	@Override
	public void refreshConfiguration() {
		
		configBean = activity.getConfiguration();
		
		// FIXME: Update UI elements from your bean fields
		explicitButton.setSelected(configBean.getIsExplicit());
		implicitButton.setSelected(!configBean.getIsExplicit());
		
	}
	
class RadioButtonListener implements ActionListener, ChangeListener, ItemListener {  
	public void actionPerformed(ActionEvent e) {
		//String factoryName = null;

		//System.out.print("ActionEvent received: ");
		//if (e.getActionCommand() == first) {
		//	System.out.println(first + " pressed.");
		//} else {
		//	System.out.println(second + " pressed.");
		//}
	}

	public void itemStateChanged(ItemEvent e) {
		//System.out.println("ItemEvent received: " 
		//	+ e.getItem()
		//	+ " is now "
  	 	//	+ ((e.getStateChange() == ItemEvent.SELECTED)?
  	 	//			"selected.":"unselected"));
	}

	public void stateChanged(ChangeEvent e) {
		//System.out.println("ChangeEvent received from: "
		//	+ e.getSource());
	}
}

}