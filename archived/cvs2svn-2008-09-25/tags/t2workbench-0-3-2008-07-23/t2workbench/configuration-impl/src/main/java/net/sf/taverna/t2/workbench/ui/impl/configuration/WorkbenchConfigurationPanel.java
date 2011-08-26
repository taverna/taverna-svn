package net.sf.taverna.t2.workbench.ui.impl.configuration;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class WorkbenchConfigurationPanel extends JPanel {

	private static Logger logger = Logger
			.getLogger(WorkbenchConfigurationUIFactory.class);

	private JTextField dotLocation = new JTextField();

	public WorkbenchConfigurationPanel() {
		super();
		setLayout(new BorderLayout());

		add(getPropertiesPanel(WorkbenchConfiguration.getInstance()),
				BorderLayout.CENTER);
		add(getButtons(), BorderLayout.SOUTH);
	}

	@SuppressWarnings("serial")
	private Component getButtons() {
		JPanel panel = new JPanel();

		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton button = new JButton();
		button.setAction(new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				Configurable conf = WorkbenchConfiguration.getInstance();
				String dotlocation = dotLocation.getText();
				conf.setProperty("taverna.dotlocation", dotlocation);
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
		JPanel result = new JPanel();
		result.setLayout(new GridLayout(10,1,0,0));
		JPanel dotLocationPanel = new JPanel();
		dotLocationPanel.setLayout(new FlowLayout());
		dotLocationPanel.add(new JLabel("Dot Location"));
		dotLocation.setText((String) (configurable
				.getProperty("taverna.dotlocation")));
		dotLocationPanel.add(dotLocation);
		result.add(dotLocationPanel);
		return result;
	}

}
