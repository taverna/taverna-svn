package net.sf.taverna.t2.workbench.ui.impl.configuration;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;

import org.apache.log4j.Logger;

public class WorkbenchConfigurationUIFactory implements ConfigurationUIFactory {
	private static Logger logger = Logger
			.getLogger(WorkbenchConfigurationUIFactory.class);

	private JTextField dotLocation = new JTextField();
	public boolean canHandle(String uuid) {
		return uuid.equals(WorkbenchConfiguration.uuid);
	}

	public JPanel getConfigurationPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		
		result.add(getPropertiesPanel(getConfigurable()), BorderLayout.CENTER);
		result.add(getButtons(),BorderLayout.SOUTH);
		
		return result;
	}

	@SuppressWarnings("serial")
	private Component getButtons() {
		JPanel panel = new JPanel();
		
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton button = new JButton();
		button.setAction(new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				Configurable conf = getConfigurable();
				String dotlocation = dotLocation.getText();
				conf.setProperty("taverna.dotlocation",dotlocation);
				try {
					ConfigurationManager.getInstance().store(conf);
				} catch (Exception e) {
					logger.error("Error storing updated configuration");
				}
			}
		});
		button.setText("Apply");
		panel.add(button);
		return panel;
	}

	private Component getPropertiesPanel(Configurable configurable) {
		JPanel propertiesPanel = new JPanel();
		propertiesPanel.setLayout(new BorderLayout());
		propertiesPanel.add(new JLabel("Dot Location"),BorderLayout.WEST);
		dotLocation.setText((String)(configurable.getProperty("taverna.dotlocation")));
		propertiesPanel.add(dotLocation,BorderLayout.CENTER);
		return propertiesPanel;
	}

	public Configurable getConfigurable() {
		return WorkbenchConfiguration.getInstance();
	}
	
}
