package net.sf.taverna.t2.workbench.reference.config;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

public class ReferenceConfigurationPanel extends JPanel {

	private ReferenceConfiguration configuration = ReferenceConfiguration.getInstance();
	
	public ReferenceConfigurationPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel storageConfig = new JLabel("Store data from workflow runs using:");
		storageConfig.setAlignmentX(LEFT_ALIGNMENT);
		JRadioButton inMemory = new JRadioButton("In-memory storage");
		inMemory.setAlignmentX(LEFT_ALIGNMENT);
		JRadioButton hibernate = new JRadioButton("On-disk storage");
		hibernate.setAlignmentX(LEFT_ALIGNMENT);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(inMemory);
		buttonGroup.add(hibernate);
		
		String context = configuration.getProperty(ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT);
		if (context.equals(ReferenceConfiguration.IN_MEMORY_CONTEXT)) {
			inMemory.setSelected(true);
		} else if (context.equals(ReferenceConfiguration.HIBERNATE_CONTEXT)) {
			hibernate.setSelected(true);
		}
		
		inMemory.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setProperty(ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT,
							ReferenceConfiguration.IN_MEMORY_CONTEXT);
				}
			}
		});
		hibernate.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setProperty(ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT,
							ReferenceConfiguration.HIBERNATE_CONTEXT);
				}
			}
		});
		
		add(storageConfig);
		add(Box.createVerticalStrut(5));
		add(inMemory);
		add(Box.createVerticalStrut(5));
		add(hibernate);
		add(Box.createVerticalGlue());
	}
}
