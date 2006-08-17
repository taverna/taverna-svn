/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.embl.ebi.escience.scufl.Processor;

/**
 * A JDialog that shows information about a specific Processor object in the
 * model, allowing editing of description and logging level.
 * 
 * @author Tom Oinn
 */
public class ScuflProcessorInfo extends JDialog {

	/**
	 * The various log levels currently supported by the enactor
	 */
	public static LogLevelHolder[] logLevels = new LogLevelHolder[] {
			new LogLevelHolder(-1, "log : Inherit from model"),
			new LogLevelHolder(0, "log : No logging"),
			new LogLevelHolder(1, "log : Low logging"),
			new LogLevelHolder(2, "log : Medium logging"),
			new LogLevelHolder(3, "log : High logging") };

	private String[] columnNames = { "Property", "Value" };

	private ArrayList propertyNames = new ArrayList();

	private ArrayList propertyValues = new ArrayList();

	/**
	 * Build a new information panel attached to the specific processor
	 */
	public ScuflProcessorInfo(Processor p) {

		super((JFrame) null, "Processor information for " + p.getName(), true);

		// Prevent the user closing the window using the close box
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		final Processor theProcessor = p;

		getContentPane().setLayout(new BorderLayout());

		// Make a JTable with the processor information in
		String[] columnNames = { "Property", "Value" };
		addRow("Name", theProcessor.getName());
		addRow("Class", theProcessor.getClass().toString());
		// Iterate over the arbitrary additional properties for this processor
		Properties props = theProcessor.getProperties();
		for (Iterator i = props.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = (String) props.get(key);
			addRow(key, value);
		}
		Object[][] tableData = new Object[propertyValues.size()][2];
		for (int i = 0; i < propertyValues.size(); i++) {
			tableData[i][0] = propertyNames.get(i);
			tableData[i][1] = propertyValues.get(i);
		}
		final JTable infoTable = new JTable(tableData, columnNames);
		infoTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		infoTable.getColumnModel().getColumn(1).setPreferredWidth(400);

		JScrollPane infoTablePane = new JScrollPane(infoTable);
		infoTablePane.setPreferredSize(new Dimension(500, 100));

		// Create a JComboBox to show the different provenance levels
		final JComboBox provenanceLevel = new JComboBox(logLevels);
		provenanceLevel.setSelectedItem(logLevelHolderForLevel(theProcessor
				.getRealLogLevel()));
		// Attach a listener to it to allow selection changes to change
		// the logging level of the processor
		provenanceLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// Set the processor log level to whatever is selected
				LogLevelHolder level = (LogLevelHolder) (provenanceLevel
						.getSelectedItem());
				theProcessor.setLogLevel(level.logLevel);
			}
		});

		// Create a JTextArea for the description
		final JTextArea description = new JTextArea(theProcessor
				.getDescription());
		description.setEditable(true);
		// Create a JButton to update the description text
		final JButton updateDescription = new JButton(
				"Update description and close");
		updateDescription.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// Write the description text to the processor
				theProcessor.setDescription(description.getText());
				ScuflProcessorInfo.this.setVisible(false);
			}

		});
		// Create a JButton to cancel the update
		final JButton cancelUpdate = new JButton("Cancel");
		cancelUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ScuflProcessorInfo.this.setVisible(false);
			}
		});

		// Put all the components into the ScuflProcessorInfo panel
		JScrollPane descriptionPane = new JScrollPane(description);
		descriptionPane.setPreferredSize(new Dimension(500, 150));
		// Container contentPane = getContentPane();
		JPanel dp = new JPanel(new BorderLayout());
		dp.add(descriptionPane, BorderLayout.CENTER);
		Border dpBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Description");
		dp.setBorder(dpBorder);

		Container contentPane = getContentPane();

		if (p.getIterationStrategy() != null) {
			contentPane.add(new IterationStrategyEditorControl(p
					.getIterationStrategy()), BorderLayout.CENTER);
		}

		contentPane.add(dp, BorderLayout.NORTH);
		contentPane.add(updateDescription, BorderLayout.WEST);
		// contentPane.add(provenanceLevel, BorderLayout.CENTER);
		contentPane.add(cancelUpdate, BorderLayout.EAST);
		contentPane.add(infoTablePane, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	private void addRow(String key, String value) {
		this.propertyNames.add(key);
		this.propertyValues.add(value);
	}

	/**
	 * Get the canonical LogLevelHolder object for the specified level. If the
	 * level doesn't exist, it returns the 'inherit' log level by default.
	 */
	public LogLevelHolder logLevelHolderForLevel(int level) {
		for (int i = 0; i < logLevels.length; i++) {
			if (level == logLevels[i].logLevel) {
				return logLevels[i];
			}
		}
		return logLevels[0];
	}

}

class LogLevelHolder {
	public int logLevel;

	public String description;

	public LogLevelHolder(int logLevel, String description) {
		this.logLevel = logLevel;
		this.description = description;
	}

	public String toString() {
		return description;
	}
}
