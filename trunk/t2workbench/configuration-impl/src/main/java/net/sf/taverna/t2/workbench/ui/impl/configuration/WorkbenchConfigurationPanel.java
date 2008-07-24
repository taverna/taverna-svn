package net.sf.taverna.t2.workbench.ui.impl.configuration;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

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
		result.setLayout(new BoxLayout(result,BoxLayout.Y_AXIS));
		JPanel dotLocationPanel = new JPanel();
		dotLocationPanel.setLayout(new FlowLayout());
		dotLocationPanel.add(new JLabel("Dot Location"));
		dotLocation.setText((String) (configurable
				.getProperty("taverna.dotlocation")));
		dotLocationPanel.add(dotLocation);
		
		JButton browseButton=new JButton();
		dotLocationPanel.add(browseButton);
		browseButton.setAction(new AbstractAction() {
			
			public void actionPerformed(ActionEvent e) {
				System.setProperty("com.apple.macos.use-file-dialog-packages", "false");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.putClientProperty("JFileChooser.appBundleIsTraversable", "always");
				fileChooser.putClientProperty("JFileChooser.packageIsTraversable", "always");
				Preferences prefs = Preferences.userNodeForPackage(getClass());
				
				fileChooser.setDialogTitle("Browse for dot");

				fileChooser.resetChoosableFileFilters();
				fileChooser.setAcceptAllFileFilterUsed(false);
				
				fileChooser.setMultiSelectionEnabled(false);
				
				int returnVal = fileChooser.showOpenDialog(WorkbenchConfigurationPanel.this);
				if (returnVal==JFileChooser.APPROVE_OPTION) {
					dotLocation.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		browseButton.setIcon(WorkbenchIcons.openIcon);
		
		result.add(dotLocationPanel);
		result.add(new JPanel());
		
		
		
		return result;
	}

}
