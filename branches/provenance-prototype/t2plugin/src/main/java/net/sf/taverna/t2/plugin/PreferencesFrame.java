package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 * @author David Withers
 * 
 */
public class PreferencesFrame extends JFrame {

	private static final long serialVersionUID = 6720157564914136334L;

	public PreferencesFrame() {
		super("T2 Plugin Preferences");
		setPreferredSize(new Dimension(500, 300));

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		JPanel dataManagerPanel = createDataManagerPreferences();

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Data Manager", dataManagerPanel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		add(mainPanel);

	}

	private JPanel createDataManagerPreferences() {
		final Preferences prefs = Preferences
				.userNodeForPackage(T2Component.class);
		final String dataManagerStore = prefs.get(
				T2Component.DATA_STORE_PROPERTY,
				T2Component.defaultDataManagerDir.getAbsolutePath());

		JPanel dataManagerPanel = new JPanel(new BorderLayout());
		
		JPanel storageDirPanel = new JPanel(new BorderLayout());
		storageDirPanel.setBorder(new TitledBorder("Data Storage Directory"));
		dataManagerPanel.add(storageDirPanel, BorderLayout.NORTH);

		final JLabel dataManagerStoreLabel = new JLabel(dataManagerStore);
		dataManagerStoreLabel.setBorder(new CompoundBorder(new EmptyBorder(5,5,10,5), LineBorder.createGrayLineBorder()));
		storageDirPanel.add(dataManagerStoreLabel, BorderLayout.CENTER);

		JButton chooseDirButton = new JButton("Choose directory...");
		chooseDirButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(dataManagerStore);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				if (fileChooser.showOpenDialog(PreferencesFrame.this) == JFileChooser.APPROVE_OPTION) {
					File newDir = fileChooser.getSelectedFile();
					if (newDir.canWrite()) {
						prefs.put(T2Component.DATA_STORE_PROPERTY, newDir
								.getAbsolutePath());
						dataManagerStoreLabel.setText(newDir.getAbsolutePath());
					} else {
						JOptionPane.showMessageDialog(PreferencesFrame.this,
								"Directory must be writable",
								"Invalid Directory", JOptionPane.ERROR_MESSAGE);
					}
				}
			}

		});

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(chooseDirButton, BorderLayout.EAST);
		
		storageDirPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dataManagerPanel;
	}

}
