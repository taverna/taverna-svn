package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

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
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.ExampleActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ExampleActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerParameterDescription;

@SuppressWarnings("serial")
public class ExampleConfigurationPanel
		extends
		ActivityConfigurationPanel<ExampleActivity, ExampleActivityConfigurationBean> {

	private ExampleActivity activity;
	private ExampleActivityConfigurationBean configBean;
	
	private JTextField operatorNamefieldString;
	private JRadioButton isExplicitFieldButton;
	
	private JRadioButton implicitButton;
	private JRadioButton explicitButton;
	
	String first = new String("Explicit");
	String second = new String("Implicit");

	public ExampleConfigurationPanel(ExampleActivity activity) {
		this.activity = activity;
		initGui();
	}

	protected void initGui() {
		removeAll();
		setLayout(new GridLayout(0, 1));

		// FIXME: Create GUI depending on activity configuration bean
		JLabel labelString = new JLabel("Operator Name");
		add(labelString);
		operatorNamefieldString = new JTextField(20);
		operatorNamefieldString.setEditable(false);
		add(operatorNamefieldString);
		labelString.setLabelFor(operatorNamefieldString);

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
		
		add(explicitButton);
		add(implicitButton);
		// End of Data-Location
		
		ParameterTableModel parameterTableModel = new ParameterTableModel();
		Object rowData[][] = { { "Row1-Column1", "Row1-Column2", "Row1-Column3"},
                { "Row2-Column1", "Row2-Column2", "Row2-Column3"} };
		Object columnNames[] = { "Column One", "Column Two", "Column Three"};
		JTable table = new JTable(parameterTableModel);
		
		add(table.getTableHeader());

		add(table);

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
	public ExampleActivityConfigurationBean getConfiguration() {
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
		
		// true (changed) unless all fields match the originals
		return ! (originalString.equals(operatorNamefieldString.getText())
		);
	}

	/**
	 * Prepare a new configuration bean from the UI, to be returned with
	 * getConfiguration()
	 */
	@Override
	public void noteConfiguration() {
		//configBean = new ExampleActivityConfigurationBean();
		
		// FIXME: Update bean fields from your UI elements
		configBean.setOperatorName(operatorNamefieldString.getText());
		//configBean.setIsExplicit(explicitButton.isSelected());
	}

	/**
	 * Update GUI from a changed configuration bean (perhaps by undo/redo).
	 * 
	 */
	@Override
	public void refreshConfiguration() {
		configBean = activity.getConfiguration();
		
		// FIXME: Update UI elements from your bean fields
		operatorNamefieldString.setText(configBean.getOperatorName());
		//explicitButton.setSelected(configBean.getIsExplicit());
		//implicitButton.setSelected(!configBean.getIsExplicit());
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