package uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * 
 * @author alperp
 */

public class ErrorDialog extends JDialog implements ActionListener {

	private JEditorPane htmlPane;

	private URL noHelpURL;

	private String cantLoadFileMessage;

	public ErrorDialog() {

		// addWindowListener(new WindowDisposer());
		setTitle("Error");

		// establish No help file
		try {
			StringBuffer noHelpBuffer = new StringBuffer();
			noHelpBuffer.append("help");
			noHelpBuffer.append(File.separator);
			noHelpBuffer.append("NoHelp.html");
			File file = new File(noHelpBuffer.toString());
			noHelpURL = file.toURL();
		} catch (Exception err) {
			// SystemLog.addError(err);
		} // end try-catch

		cantLoadFileMessage = "ca not load page";
		// = FetaResources.getMessage("globalConstants.cantLoadPage");

		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints mainPanelGC = new GridBagConstraints();
		mainPanelGC.anchor = GridBagConstraints.NORTHWEST;
		mainPanelGC.fill = GridBagConstraints.BOTH;
		mainPanelGC.gridx = 0;
		mainPanelGC.gridy = 0;
		mainPanelGC.weightx = 100;
		mainPanelGC.weighty = 100;

		htmlPane = new JEditorPane();

		JScrollPane scrollPane = new JScrollPane(htmlPane);
		mainPanel.add(scrollPane, mainPanelGC);

		htmlPane.setEditable(false);

		String closeText = "close";
		// = FetaResources.getMessage("buttons.close");
		JButton close = new JButton(closeText);
		close.addActionListener(this);

		String detailsText = "details>>";
		// = FetaResources.getMessage("buttons.details");
		JButton details = new JButton(detailsText);

		mainPanelGC.anchor = GridBagConstraints.SOUTHEAST;
		mainPanelGC.fill = GridBagConstraints.NONE;
		mainPanelGC.gridy = 1;
		mainPanelGC.weightx = 0;
		mainPanelGC.weighty = 0;
		mainPanel.add(close, mainPanelGC);

		getContentPane().add(mainPanel);

		setSize(600, 400);
		// pack();
	}

	/**
	 * the only button HelpDialog listens to is the close button. Close hides
	 * the dialog
	 */
	public void actionPerformed(ActionEvent event) {
		// dispose();
	}

}
